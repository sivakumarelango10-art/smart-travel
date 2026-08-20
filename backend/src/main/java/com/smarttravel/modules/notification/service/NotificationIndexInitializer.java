package com.smarttravel.modules.notification.service;

import com.smarttravel.modules.notification.model.Notification;
import jakarta.annotation.PostConstruct;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Enterprise Self-Healing MongoDB Index Initializer for Notifications.
 * Ensures the unique constraint on idempotencyKey is reliably established even when
 * historical test/concurrency duplicates exist in the collection, guaranteeing zero deployment
 * failures on Render while strictly preserving the uniqueness invariant.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NotificationIndexInitializer {

    private static final Logger log = LoggerFactory.getLogger(NotificationIndexInitializer.class);
    private static final String COLLECTION_NAME = "notifications";
    private static final String IDEMPOTENCY_KEY_FIELD = "idempotencyKey";

    private final MongoTemplate mongoTemplate;

    @Autowired
    public NotificationIndexInitializer(@Autowired(required = false) MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initIndexes() {
        if (mongoTemplate == null) {
            log.warn("MongoTemplate is not available. Skipping Notification index initialization.");
            return;
        }

        try {
            if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
                mongoTemplate.createCollection(COLLECTION_NAME);
            }

            if (isUniqueIdempotencyIndexValid()) {
                log.info("MongoDB unique index on idempotencyKey already verified for '{}' collection", COLLECTION_NAME);
                ensureAuxiliaryIndexes();
                return;
            }

            log.info("Unique idempotencyKey index missing or unverified. Initiating self-healing index pipeline...");
            selfHealDuplicateNotifications();
            ensureUniqueIdempotencyIndex();
            ensureAuxiliaryIndexes();
            log.info("MongoDB unique index on idempotencyKey successfully established and verified.");
        } catch (Exception ex) {
            log.warn("Initial index verification encountered duplicate keys or conflict: {}. Attempting self-healing deduplication...", ex.getMessage());
            try {
                selfHealDuplicateNotifications();
                ensureUniqueIdempotencyIndex();
                ensureAuxiliaryIndexes();
                log.info("MongoDB unique index on idempotencyKey successfully established after self-healing deduplication.");
            } catch (Exception innerEx) {
                log.error("Failed to establish unique index on notifications: {}", innerEx.getMessage(), innerEx);
                throw new IllegalStateException("Critical: Unable to enforce idempotencyKey uniqueness on notifications collection: " + innerEx.getMessage(), innerEx);
            }
        }
    }

    /**
     * Checks if a unique index covering idempotencyKey currently exists on the notifications collection.
     */
    public boolean isUniqueIdempotencyIndexValid() {
        try {
            List<IndexInfo> indexInfoList = mongoTemplate.indexOps(COLLECTION_NAME).getIndexInfo();
            for (IndexInfo info : indexInfoList) {
                if (info.isUnique() && info.getIndexFields().stream()
                        .anyMatch(field -> IDEMPOTENCY_KEY_FIELD.equals(field.getKey()))) {
                    return true;
                }
            }
        } catch (Exception ex) {
            log.debug("Could not inspect existing index info: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Finds and deduplicates historical duplicate notifications with identical idempotencyKey.
     * Preserves exactly ONE authoritative document per key (oldest or delivered/sent) and removes orphaned duplicates.
     *
     * @return count of duplicate documents removed
     */
    public synchronized int selfHealDuplicateNotifications() {
        if (!mongoTemplate.collectionExists(COLLECTION_NAME)) {
            return 0;
        }

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(IDEMPOTENCY_KEY_FIELD).exists(true).ne(null)),
                Aggregation.group(IDEMPOTENCY_KEY_FIELD).count().as("count").push("$$ROOT").as("notifications"),
                Aggregation.match(Criteria.where("count").gt(1))
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Document.class);
        List<Document> duplicateGroups = results.getMappedResults();

        if (duplicateGroups.isEmpty()) {
            log.debug("No duplicate idempotencyKey entries found in '{}' collection.", COLLECTION_NAME);
            return 0;
        }

        log.info("Self-healing detected {} duplicate idempotencyKey group(s) in '{}' collection",
                duplicateGroups.size(), COLLECTION_NAME);

        int totalRemoved = 0;

        for (Document group : duplicateGroups) {
            String idempotencyKey = group.getString("_id");
            List<Document> notifDocs = group.getList("notifications", Document.class);
            if (notifDocs == null || notifDocs.size() <= 1) {
                continue;
            }

            Document authoritativeDoc = selectAuthoritativeDocument(notifDocs);
            Object authoritativeId = authoritativeDoc.get("_id");

            List<Object> toDeleteIds = new ArrayList<>();
            for (Document doc : notifDocs) {
                Object docId = doc.get("_id");
                if (!Objects.equals(docId, authoritativeId)) {
                    toDeleteIds.add(docId);
                }
            }

            log.info("Deduplication: Preserving authoritative notification (ID: {}, Status: {}) for idempotencyKey: '{}'",
                    authoritativeId, authoritativeDoc.getString("status"), idempotencyKey);

            for (Object delId : toDeleteIds) {
                log.info("Deduplication: Removing orphaned duplicate notification (ID: {}) for idempotencyKey: '{}'",
                        delId, idempotencyKey);
                mongoTemplate.remove(Query.query(Criteria.where("_id").is(delId)), COLLECTION_NAME);
                totalRemoved++;
            }
        }

        log.info("Self-healing deduplication completed: removed {} orphaned duplicate notification document(s).", totalRemoved);
        return totalRemoved;
    }

    /**
     * Selects the most authoritative document from a set of duplicates:
     * 1. Prefers documents with status 'SENT' or 'DELIVERED'.
     * 2. Prefers documents with non-null sentAt or providerMessageId.
     * 3. Falls back to the oldest record by createdAt or ObjectId timestamp.
     */
    private Document selectAuthoritativeDocument(List<Document> documents) {
        Document best = documents.get(0);

        for (int i = 1; i < documents.size(); i++) {
            Document candidate = documents.get(i);

            int scoreBest = calculateAuthorityScore(best);
            int scoreCandidate = calculateAuthorityScore(candidate);

            if (scoreCandidate > scoreBest) {
                best = candidate;
            } else if (scoreCandidate == scoreBest) {
                // Tie-breaker: oldest timestamp
                Date dateBest = best.getDate("createdAt");
                Date dateCand = candidate.getDate("createdAt");

                if (dateCand != null && dateBest != null) {
                    if (dateCand.before(dateBest)) {
                        best = candidate;
                    }
                } else {
                    // Fallback to ObjectId generation time if available
                    Object idBest = best.get("_id");
                    Object idCand = candidate.get("_id");
                    if (idBest instanceof ObjectId && idCand instanceof ObjectId) {
                        if (((ObjectId) idCand).getTimestamp() < ((ObjectId) idBest).getTimestamp()) {
                            best = candidate;
                        }
                    }
                }
            }
        }

        return best;
    }

    private int calculateAuthorityScore(Document doc) {
        int score = 0;
        String status = doc.getString("status");
        if ("DELIVERED".equalsIgnoreCase(status)) score += 30;
        else if ("SENT".equalsIgnoreCase(status)) score += 20;
        else if ("PENDING".equalsIgnoreCase(status)) score += 10;

        if (doc.getString("providerMessageId") != null && !doc.getString("providerMessageId").isBlank()) {
            score += 15;
        }
        if (doc.getDate("sentAt") != null) {
            score += 10;
        }
        if (Boolean.TRUE.equals(doc.getBoolean("read"))) {
            score += 5;
        }
        return score;
    }

    private void ensureUniqueIdempotencyIndex() {
        mongoTemplate.indexOps(COLLECTION_NAME).ensureIndex(
                new Index().on(IDEMPOTENCY_KEY_FIELD, Sort.Direction.ASC).unique()
        );
    }

    private void ensureAuxiliaryIndexes() {
        var notifOps = mongoTemplate.indexOps(COLLECTION_NAME);
        notifOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .named("notification_user_created_idx"));
        notifOps.ensureIndex(new Index().on("userId", Sort.Direction.ASC)
                .on("read", Sort.Direction.ASC)
                .named("notification_user_read_idx"));
        notifOps.ensureIndex(new Index().on("status", Sort.Direction.ASC)
                .on("retryCount", Sort.Direction.ASC)
                .named("notification_status_retry_idx"));
    }
}
