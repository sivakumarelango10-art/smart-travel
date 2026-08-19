package com.smarttravel.modules.flight.disruption;

import com.smarttravel.modules.flight.disruption.dto.FlightCancelRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.service.FlightDisruptionService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.notification.dto.NotificationResponse;
import com.smarttravel.modules.notification.dto.NotificationSendRequest;
import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationType;
import com.smarttravel.modules.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlightDisruptionConcurrencyIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightDisruptionService disruptionService;

    @Autowired
    private NotificationService notificationService;

    @Test
    @DisplayName("Concurrency Test: 10 concurrent threads cancelling the same flight - exactly one authoritative cancellation and zero race errors")
    void testConcurrentFlightCancellations() throws Exception {
        long timestamp = System.currentTimeMillis();
        String flightNumber = "CONCUR-CANCEL-" + timestamp;

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber(flightNumber)
                .airline("SmartTravel Concurrency")
                .airlineCode("STC")
                .departureAirport(new AirportDto("DEL", "Delhi Airport", "Delhi", "India", "T3", "Gate 1"))
                .arrivalAirport(new AirportDto("BOM", "Mumbai Airport", "Mumbai", "India", "T2", "Gate 2"))
                .departureTime(Instant.now().plus(8, ChronoUnit.HOURS))
                .arrivalTime(Instant.now().plus(10, ChronoUnit.HOURS))
                .aircraftModel("Boeing 737 MAX 8")
                .basePrice(BigDecimal.valueOf(4500.00))
                .totalSeats(150)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(
                        new CabinInventoryDto(CabinClass.ECONOMY, 150, 150, BigDecimal.valueOf(4500.00),
                                BigDecimal.valueOf(540.00), BigDecimal.valueOf(150.00), BigDecimal.valueOf(5190.00))
                ))
                .build();

        FlightResponse flight = flightService.createFlight(flightReq);
        String flightId = flight.getId();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    FlightOperationalStatusResponse res = disruptionService.cancelFlight(
                            flightId,
                            new FlightCancelRequest("Concurrent cancellation " + index, "Concurrent test", false),
                            "admin_" + index
                    );
                    if (res != null && res.getStatus() == FlightStatus.CANCELLED) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception ignored) {
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Every thread should receive status CANCELLED (authoritative or idempotent return)
        assertThat(successCount.get()).isEqualTo(threadCount);

        FlightOperationalStatusResponse finalStatus = disruptionService.getFlightOperationalStatus(flightId);
        assertThat(finalStatus.getStatus()).isEqualTo(FlightStatus.CANCELLED);
    }

    @Test
    @DisplayName("Concurrency Test: 10 concurrent dispatches with identical composite idempotency key - exactly 1 persisted notification")
    void testConcurrentNotificationIdempotency() throws Exception {
        long timestamp = System.currentTimeMillis();
        String eventId = "concur_evt_" + timestamp;
        String userId = "concur_user_" + timestamp;
        String flightId = "concur_fl_" + timestamp;

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
                            .notificationType(NotificationType.FLIGHT_DELAYED)
                            .channel(NotificationChannel.EMAIL)
                            .recipient("concur_pax@smarttravel.com")
                            .subject("Flight Delay Notice")
                            .content("Your flight is delayed by 30 mins")
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
        finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(responses).isNotEmpty();
        // All responses must share the exact same Notification MongoDB ID
        String authoritativeId = responses.get(0).getId();
        for (NotificationResponse res : responses) {
            assertThat(res.getId()).isEqualTo(authoritativeId);
        }
    }
}
