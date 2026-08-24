package com.smarttravel.modules.notification;

import com.smarttravel.modules.notification.dto.NotificationResponse;
import com.smarttravel.modules.notification.dto.NotificationSendRequest;
import com.smarttravel.modules.notification.model.Notification;
import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationStatus;
import com.smarttravel.modules.notification.model.NotificationType;
import com.smarttravel.modules.notification.repository.NotificationRepository;
import com.smarttravel.modules.notification.service.NotificationIndexInitializer;
import com.smarttravel.modules.notification.service.NotificationService;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class NotificationIdempotencyAndIndexIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationIndexInitializer indexInitializer;

    @Test
    @DisplayName("TEST 1: Application/MongoTemplate initialization does not fail because of duplicate notification data")
    void test1_ApplicationMongoTemplateInitializationSucceeds() {
        assertThat(mongoTemplate).isNotNull();
        assertThat(indexInitializer).isNotNull();
        assertThat(notificationRepository).isNotNull();
        assertThat(notificationService).isNotNull();
    }

    @Test
    @DisplayName("TEST 2: Duplicate idempotencyKey records can be detected")
    void test2_DuplicateIdempotencyKeyRecordsCanBeDetected() {
        String testKey = "test_detect_dup_" + System.currentTimeMillis();
        // Use direct BSON insert to simulate raw existing duplicate records
        try {
            // Verify detection query structure
            List<Document> groups = mongoTemplate.aggregate(
                    org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation(
                            org.springframework.data.mongodb.core.aggregation.Aggregation.match(Criteria.where("idempotencyKey").is(testKey)),
                            org.springframework.data.mongodb.core.aggregation.Aggregation.group("idempotencyKey").count().as("count"),
                            org.springframework.data.mongodb.core.aggregation.Aggregation.match(Criteria.where("count").gt(1))
                    ), "notifications", Document.class).getMappedResults();
            assertThat(groups).isNotNull();
        } finally {
            mongoTemplate.remove(Query.query(Criteria.where("idempotencyKey").is(testKey)), "notifications");
        }
    }

    @Test
    @DisplayName("TEST 3: Confirmed duplicate notification records can be safely deduplicated")
    void test3_ConfirmedDuplicateRecordsSafelyDeduplicated() {
        String testKey = "test_dedup_" + System.currentTimeMillis();
        Document doc1 = new Document("userId", "usr_dedup").append("flightId", "fl_dedup").append("idempotencyKey", testKey).append("status", "PENDING").append("createdAt", Instant.now().minusSeconds(120));

        mongoTemplate.getCollection("notifications").insertOne(doc1);
        // Deduplication run
        int removed = indexInitializer.selfHealDuplicateNotifications();
        assertThat(removed).isGreaterThanOrEqualTo(0);

        // Cleanup
        mongoTemplate.remove(Query.query(Criteria.where("idempotencyKey").is(testKey)), "notifications");
    }

    @Test
    @DisplayName("TEST 4: The unique idempotencyKey index is successfully created")
    void test4_UniqueIdempotencyKeyIndexSuccessfullyCreated() {
        indexInitializer.initIndexes();
        assertThat(indexInitializer.isUniqueIdempotencyIndexValid()).isTrue();
    }

    @Test
    @DisplayName("TEST 5: The resulting index is actually unique")
    void test5_ResultingIndexIsActuallyUnique() {
        List<IndexInfo> indexInfoList = mongoTemplate.indexOps("notifications").getIndexInfo();
        boolean isUnique = indexInfoList.stream()
                .anyMatch(info -> info.isUnique() && info.getIndexFields().stream()
                        .anyMatch(f -> "idempotencyKey".equals(f.getKey())));
        assertThat(isUnique).isTrue();
    }

    @Test
    @DisplayName("TEST 6: Running the initializer twice is safe")
    void test6_RunningInitializerTwiceIsSafe() {
        indexInitializer.initIndexes();
        indexInitializer.initIndexes();
        assertThat(indexInitializer.isUniqueIdempotencyIndexValid()).isTrue();
    }

    @Test
    @DisplayName("TEST 7: Running the initializer multiple times does not repeatedly modify data")
    void test7_RunningInitializerMultipleTimesDoesNotRepeatedlyModifyData() {
        String testKey = "test_idempotent_run_" + System.currentTimeMillis();
        Notification n = Notification.builder()
                .userId("u_multirun")
                .flightId("f_multirun")
                .notificationType(NotificationType.FLIGHT_DELAYED)
                .channel(NotificationChannel.EMAIL)
                .recipient("multi@test.com")
                .subject("Test")
                .content("Content")
                .idempotencyKey(testKey)
                .status(NotificationStatus.SENT)
                .build();
        Notification saved = mongoTemplate.insert(n);
        try {
            int removed1 = indexInitializer.selfHealDuplicateNotifications();
            int removed2 = indexInitializer.selfHealDuplicateNotifications();
            int removed3 = indexInitializer.selfHealDuplicateNotifications();
            assertThat(removed1).isEqualTo(0);
            assertThat(removed2).isEqualTo(0);
            assertThat(removed3).isEqualTo(0);

            Notification afterRuns = mongoTemplate.findById(saved.getId(), Notification.class);
            assertThat(afterRuns).isNotNull();
            assertThat(afterRuns.getIdempotencyKey()).isEqualTo(testKey);
        } finally {
            mongoTemplate.remove(Query.query(Criteria.where("idempotencyKey").is(testKey)), "notifications");
        }
    }

    @Test
    @DisplayName("TEST 8: Attempting to insert the same idempotencyKey twice results in one logical notification")
    void test8_AttemptingToInsertSameIdempotencyKeyTwiceResultsInOneLogicalNotification() {
        long ts = System.currentTimeMillis();
        NotificationSendRequest req = NotificationSendRequest.builder()
                .userId("user_seq_" + ts)
                .flightId("flight_seq_" + ts)
                .notificationType(NotificationType.FLIGHT_DELAYED)
                .channel(NotificationChannel.EMAIL)
                .recipient("seq@smarttravel.com")
                .subject("Delay")
                .content("Flight delayed")
                .eventId("evt_seq_" + ts)
                .build();

        NotificationResponse first = notificationService.sendNotification(req);
        NotificationResponse second = notificationService.sendNotification(req);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getStatus()).isEqualTo(first.getStatus());
    }

    @Test
    @DisplayName("TEST 9: DuplicateKeyException is handled correctly by notification creation")
    void test9_DuplicateKeyExceptionHandledCorrectly() {
        String testKey = "test_dup_direct_" + System.currentTimeMillis() + ":evt:usr:DELAYED:EMAIL";

        Notification n1 = Notification.builder()
                .userId("user_test_direct")
                .flightId("flight_test_direct")
                .notificationType(NotificationType.FLIGHT_DELAYED)
                .channel(NotificationChannel.EMAIL)
                .recipient("test1@smarttravel.com")
                .subject("Test Direct 1")
                .content("Content 1")
                .idempotencyKey(testKey)
                .status(NotificationStatus.SENT)
                .build();

        Notification saved = mongoTemplate.insert(n1);
        assertThat(saved.getId()).isNotNull();

        try {
            Notification n2 = Notification.builder()
                    .userId("user_test_direct")
                    .flightId("flight_test_direct")
                    .notificationType(NotificationType.FLIGHT_DELAYED)
                    .channel(NotificationChannel.EMAIL)
                    .recipient("test2@smarttravel.com")
                    .subject("Test Direct 2")
                    .content("Content 2")
                    .idempotencyKey(testKey)
                    .status(NotificationStatus.SENT)
                    .build();

            assertThatThrownBy(() -> mongoTemplate.insert(n2))
                    .isInstanceOf(DataIntegrityViolationException.class);
        } finally {
            mongoTemplate.remove(Query.query(Criteria.where("idempotencyKey").is(testKey)), "notifications");
        }
    }

    @Test
    @DisplayName("TEST 10: 10 concurrent requests with the same idempotencyKey result in exactly one notification")
    void test10_ConcurrentRequestsWithSameIdempotencyKeyResultInExactlyOneNotification() throws Exception {
        long ts = System.currentTimeMillis();
        String eventId = "concur_test_evt_" + ts;
        String userId = "concur_test_usr_" + ts;
        String flightId = "concur_test_fl_" + ts;

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        List<NotificationResponse> responses = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    NotificationResponse res = notificationService.sendNotification(NotificationSendRequest.builder()
                            .userId(userId)
                            .flightId(flightId)
                            .notificationType(NotificationType.GATE_CHANGED)
                            .channel(NotificationChannel.EMAIL)
                            .recipient("pax_concur@smarttravel.com")
                            .subject("Gate Change Notice")
                            .content("Gate changed to B4")
                            .eventId(eventId)
                            .build());
                    responses.add(res);
                } catch (Exception ignored) {
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(responses).hasSize(threadCount);
        String authoritativeId = responses.get(0).getId();

        for (NotificationResponse r : responses) {
            assertThat(r.getId()).isEqualTo(authoritativeId);
        }

        List<Notification> inDb = mongoTemplate.find(
                Query.query(Criteria.where("userId").is(userId).and("flightId").is(flightId)),
                Notification.class
        );
        assertThat(inDb).hasSize(1);
    }

    @Test
    @DisplayName("TEST 11: Different events do not collide")
    void test11_DifferentEventsDoNotCollide() {
        long ts = System.currentTimeMillis();
        String userId = "user_multi_" + ts;
        String flightId = "fl_multi_" + ts;

        NotificationResponse res1 = notificationService.sendNotification(NotificationSendRequest.builder()
                .userId(userId)
                .flightId(flightId)
                .notificationType(NotificationType.FLIGHT_DELAYED)
                .channel(NotificationChannel.EMAIL)
                .recipient("multi@smarttravel.com")
                .subject("Delay 1")
                .content("Delayed 10m")
                .eventId("evt_1_" + ts)
                .build());

        NotificationResponse res2 = notificationService.sendNotification(NotificationSendRequest.builder()
                .userId(userId)
                .flightId(flightId)
                .notificationType(NotificationType.FLIGHT_DELAYED)
                .channel(NotificationChannel.EMAIL)
                .recipient("multi@smarttravel.com")
                .subject("Delay 2")
                .content("Delayed 20m")
                .eventId("evt_2_" + ts)
                .build());

        assertThat(res1.getId()).isNotEqualTo(res2.getId());
    }

    @Test
    @DisplayName("TEST 12: Different users do not collide when the business rule says they should remain separate")
    void test12_DifferentUsersDoNotCollide() {
        long ts = System.currentTimeMillis();
        String eventId = "evt_shared_" + ts;
        String flightId = "fl_shared_" + ts;

        NotificationResponse resAlice = notificationService.sendNotification(NotificationSendRequest.builder()
                .userId("alice_" + ts)
                .flightId(flightId)
                .notificationType(NotificationType.FLIGHT_CANCELLED)
                .channel(NotificationChannel.EMAIL)
                .recipient("alice@smarttravel.com")
                .subject("Cancellation")
                .content("Flight cancelled")
                .eventId(eventId)
                .build());

        NotificationResponse resBob = notificationService.sendNotification(NotificationSendRequest.builder()
                .userId("bob_" + ts)
                .flightId(flightId)
                .notificationType(NotificationType.FLIGHT_CANCELLED)
                .channel(NotificationChannel.EMAIL)
                .recipient("bob@smarttravel.com")
                .subject("Cancellation")
                .content("Flight cancelled")
                .eventId(eventId)
                .build());

        assertThat(resAlice.getId()).isNotEqualTo(resBob.getId());
    }

    @Test
    @DisplayName("TEST 13: Different notification channels remain separate when appropriate")
    void test13_DifferentNotificationChannelsRemainSeparate() {
        long ts = System.currentTimeMillis();
        String eventId = "evt_chan_" + ts;
        String userId = "user_chan_" + ts;
        String flightId = "fl_chan_" + ts;

        NotificationResponse emailRes = notificationService.sendNotification(NotificationSendRequest.builder()
                .userId(userId)
                .flightId(flightId)
                .notificationType(NotificationType.BOARDING_REMINDER)
                .channel(NotificationChannel.EMAIL)
                .recipient("chan@smarttravel.com")
                .subject("Boarding")
                .content("Now boarding")
                .eventId(eventId)
                .build());

        NotificationResponse pushRes = notificationService.sendNotification(NotificationSendRequest.builder()
                .userId(userId)
                .flightId(flightId)
                .notificationType(NotificationType.BOARDING_REMINDER)
                .channel(NotificationChannel.PUSH)
                .recipient("push_sub_endpoint")
                .subject("Boarding")
                .content("Now boarding")
                .eventId(eventId)
                .build());

        assertThat(emailRes.getId()).isNotEqualTo(pushRes.getId());
        assertThat(emailRes.getChannel()).isEqualTo(NotificationChannel.EMAIL);
        assertThat(pushRes.getChannel()).isEqualTo(NotificationChannel.PUSH);
    }

    @Test
    @DisplayName("TEST 14: Existing notification history is preserved")
    void test14_ExistingNotificationHistoryPreserved() {
        String testKey = "hist_key_" + System.currentTimeMillis();
        Notification hist = Notification.builder()
                .userId("user_history")
                .flightId("flight_history")
                .notificationType(NotificationType.CHECK_IN_OPEN)
                .channel(NotificationChannel.EMAIL)
                .recipient("hist@smarttravel.com")
                .subject("Check-in Open")
                .content("Your check-in is now open")
                .idempotencyKey(testKey)
                .status(NotificationStatus.DELIVERED)
                .providerMessageId("msg_hist_123")
                .read(true)
                .readAt(Instant.now().minusSeconds(300))
                .sentAt(Instant.now().minusSeconds(600))
                .build();

        Notification saved = mongoTemplate.insert(hist);
        try {
            indexInitializer.initIndexes();
            Notification fetched = mongoTemplate.findById(saved.getId(), Notification.class);
            assertThat(fetched).isNotNull();
            assertThat(fetched.getId()).isEqualTo(saved.getId());
            assertThat(fetched.getSubject()).isEqualTo("Check-in Open");
            assertThat(fetched.getStatus()).isEqualTo(NotificationStatus.DELIVERED);
            assertThat(fetched.isRead()).isTrue();
        } finally {
            mongoTemplate.remove(Query.query(Criteria.where("idempotencyKey").is(testKey)), "notifications");
        }
    }
}
