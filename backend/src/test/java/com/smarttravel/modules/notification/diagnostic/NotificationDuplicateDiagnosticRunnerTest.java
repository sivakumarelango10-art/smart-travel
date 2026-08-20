package com.smarttravel.modules.notification.diagnostic;

import com.smarttravel.modules.notification.model.Notification;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Diagnostic test to safely inspect and report any duplicate idempotencyKey entries
 * in the MongoDB notifications collection without modifying or deleting data.
 */
@SpringBootTest
public class NotificationDuplicateDiagnosticRunnerTest {

    private static final Logger log = LoggerFactory.getLogger(NotificationDuplicateDiagnosticRunnerTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("Phase 2 Diagnostic: Inspect and report all duplicate idempotencyKey records in MongoDB notifications")
    void runDiagnosticInspection() {
        log.info("==========================================================================");
        log.info("STARTING NOTIFICATION IDEMPOTENCY KEY DIAGNOSTIC INSPECTION");
        log.info("==========================================================================");

        if (!mongoTemplate.collectionExists("notifications")) {
            log.info("Notifications collection does not exist yet. No duplicates found.");
            return;
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("idempotencyKey").exists(true).ne(null)),
                Aggregation.group("idempotencyKey").count().as("count").push("$$ROOT").as("notifications"),
                Aggregation.match(Criteria.where("count").gt(1))
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "notifications", Document.class);
        List<Document> duplicateGroups = results.getMappedResults();

        log.info("Total duplicate idempotencyKey groups found: {}", duplicateGroups.size());

        int totalDuplicateDocuments = 0;
        int testArtifactGroups = 0;
        int productionGroups = 0;

        for (Document group : duplicateGroups) {
            String idempotencyKey = group.getString("_id");
            int count = group.getInteger("count", 0);
            totalDuplicateDocuments += count;

            boolean isTestArtifact = idempotencyKey != null &&
                    (idempotencyKey.contains("concur_") || idempotencyKey.contains("test_") || idempotencyKey.contains("mock_"));

            if (isTestArtifact) {
                testArtifactGroups++;
            } else {
                productionGroups++;
            }

            log.info("--------------------------------------------------------------------------");
            log.info("DUPLICATE GROUP: idempotencyKey='{}'", idempotencyKey);
            log.info("Duplicate Count: {}", count);
            log.info("Classification: {}", isTestArtifact ? "HISTORICAL CONCURRENCY TEST ARTIFACT" : "PRODUCTION RECORD");

            List<Document> notifDocs = group.getList("notifications", Document.class);
            if (notifDocs != null) {
                for (int i = 0; i < notifDocs.size(); i++) {
                    Document doc = notifDocs.get(i);
                    log.info("  [Doc #{}] ID: {}, UserId: {}, FlightId: {}, Type: {}, Channel: {}, Status: {}, CreatedAt: {}",
                            i + 1,
                            doc.get("_id"),
                            doc.getString("userId"),
                            doc.getString("flightId"),
                            doc.getString("notificationType"),
                            doc.getString("channel"),
                            doc.getString("status"),
                            doc.get("createdAt")
                    );
                }
            }
        }

        log.info("==========================================================================");
        log.info("DIAGNOSTIC SUMMARY:");
        log.info("Total Duplicate Groups: {}", duplicateGroups.size());
        log.info("Total Duplicate Documents: {}", totalDuplicateDocuments);
        log.info("Test Artifact Groups: {}", testArtifactGroups);
        log.info("Legitimate Production Groups: {}", productionGroups);
        log.info("==========================================================================");

        assertThat(duplicateGroups).isNotNull();
    }
}
