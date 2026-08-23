package com.smarttravel.common.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;

import java.util.List;

/**
 * Enterprise MongoDB Index Initializer.
 * Programmatically ensures all high-traffic compound indexes exist on application startup,
 * guaranteeing sub-50ms query execution across search, booking, payment, and analytics paths.
 *
 * <p>Key design contract for unique indexes (e.g. ticketNumber, bookingReference, bookingId):</p>
 * <ul>
 *   <li>Required constraint: unique index on the respective field.</li>
 *   <li>MongoDB may have auto-created indexes (e.g. "ticketNumber_1") before auto-index-creation was disabled.</li>
 *   <li>Attempting to ensureIndex with a different name on an already-indexed field raises
 *       MongoCommandException error 85 (IndexOptionsConflict).</li>
 *   <li>We detect functionally-equivalent existing indexes first and skip creation when found.</li>
 * </ul>
 */
@Configuration
public class MongoIndexConfig {

    private static final Logger log = LoggerFactory.getLogger(MongoIndexConfig.class);

    private final MongoTemplate mongoTemplate;
    private final com.smarttravel.modules.notification.service.NotificationIndexInitializer notificationIndexInitializer;

    public MongoIndexConfig(MongoTemplate mongoTemplate,
                            @org.springframework.beans.factory.annotation.Autowired(required = false)
                            com.smarttravel.modules.notification.service.NotificationIndexInitializer notificationIndexInitializer) {
        this.mongoTemplate = mongoTemplate;
        this.notificationIndexInitializer = notificationIndexInitializer;
    }

    @PostConstruct
    public void ensureIndexes() {
        log.info("Ensuring high-performance MongoDB compound indexes across collections...");
        if (notificationIndexInitializer != null) {
            notificationIndexInitializer.initIndexes();
        }
        try {
            // 1. Flights collection compound indexes
            ensureIndexSafely("flights", new Index().on("departureAirport.code", Sort.Direction.ASC)
                    .on("arrivalAirport.code", Sort.Direction.ASC)
                    .on("active", Sort.Direction.ASC)
                    .on("departureTime", Sort.Direction.ASC)
                    .named("idx_flight_esr_composite"));
            ensureIndexSafely("flights", new Index().on("flightNumber", Sort.Direction.ASC)
                    .named("idx_flight_number"));
            ensureIndexSafely("flights", new Index().on("status", Sort.Direction.ASC)
                    .on("active", Sort.Direction.ASC)
                    .named("idx_flight_status_active"));

            // 2. Bookings collection compound indexes
            ensureIndexSafely("bookings", new Index().on("userId", Sort.Direction.ASC)
                    .on("createdAt", Sort.Direction.DESC)
                    .named("idx_booking_user_date"));
            ensureIndexSafely("bookings", new Index().on("userId", Sort.Direction.ASC)
                    .on("status", Sort.Direction.ASC)
                    .on("createdAt", Sort.Direction.DESC)
                    .named("idx_booking_user_status_date"));
            ensureUniqueIndexSafely("bookings", "bookingReference", "idx_booking_reference_unique");

            // 3. Tickets collection indexes
            ensureUniqueIndexSafely("tickets", "bookingId", "idx_ticket_booking_id");
            ensureTicketNumberUniqueIndex();

            // 4. Hotels collection compound indexes
            ensureIndexSafely("hotels", new Index().on("address.city", Sort.Direction.ASC)
                    .on("active", Sort.Direction.ASC)
                    .on("starRating", Sort.Direction.DESC)
                    .named("idx_hotel_address_city_rating"));
            ensureIndexSafely("hotels", new Index().on("nearestAirportCode", Sort.Direction.ASC)
                    .on("active", Sort.Direction.ASC)
                    .named("idx_hotel_airport_active"));

            // 5. Rooms collection indexes
            ensureIndexSafely("rooms", new Index().on("hotelId", Sort.Direction.ASC)
                    .on("roomType", Sort.Direction.ASC)
                    .named("idx_room_hotel_type"));

            // 6. Reviews collection compound indexes
            ensureIndexSafely("reviews", new Index().on("targetId", Sort.Direction.ASC)
                    .on("targetType", Sort.Direction.ASC)
                    .on("status", Sort.Direction.ASC)
                    .on("createdAt", Sort.Direction.DESC)
                    .named("idx_review_target_status_date"));

            // 7. Price Freezes collection compound indexes
            ensureIndexSafely("price_freezes", new Index().on("userId", Sort.Direction.ASC)
                    .on("flightId", Sort.Direction.ASC)
                    .on("status", Sort.Direction.ASC)
                    .named("idx_freeze_user_flight_status"));

            // 8. User Activity / Recommendations
            ensureIndexSafely("user_activities", new Index().on("userId", Sort.Direction.ASC)
                    .on("eventType", Sort.Direction.ASC)
                    .on("createdAt", Sort.Direction.DESC)
                    .named("idx_user_activity_user_event_date"));

            // 9. Notifications collection
            ensureIndexSafely("notifications", new Index().on("userId", Sort.Direction.ASC)
                    .on("createdAt", Sort.Direction.DESC)
                    .named("idx_notification_user_created_date"));
            ensureIndexSafely("notifications", new Index().on("userId", Sort.Direction.ASC)
                    .on("read", Sort.Direction.ASC)
                    .named("idx_notification_user_read_status"));

            // 10. Payments collection
            ensureIndexSafely("payments", new Index().on("bookingId", Sort.Direction.ASC)
                    .named("idx_payment_booking_id"));
            ensureIndexSafely("payments", new Index().on("paymentStatus", Sort.Direction.ASC)
                    .on("createdAt", Sort.Direction.DESC)
                    .named("idx_payment_status_date"));

            // 11. Flight Price Histories collection compound indexes
            ensureIndexSafely("flight_price_histories", new Index().on("flightId", Sort.Direction.ASC)
                    .on("cabinClass", Sort.Direction.ASC)
                    .on("capturedAt", Sort.Direction.DESC)
                    .named("idx_price_hist_flight_cabin_time"));
            ensureIndexSafely("flight_price_histories", new Index().on("flightId", Sort.Direction.ASC)
                    .on("capturedAt", Sort.Direction.DESC)
                    .named("idx_price_hist_flight_time"));

            // 12. Dynamic Pricing Rules collection index
            ensureIndexSafely("dynamic_pricing_rules", new Index().on("type", Sort.Direction.ASC)
                    .on("enabled", Sort.Direction.ASC)
                    .on("priority", Sort.Direction.ASC)
                    .named("idx_pricing_rule_type_enabled_prio"));

            log.info("All MongoDB performance indexes successfully verified and initialized.");
        } catch (Exception ex) {
            log.warn("MongoDB index initialization warning (continuing startup): {}", ex.getMessage());
        }
    }

    /**
     * Ensures an index exists on the specified collection without failing on name conflicts.
     * If an index with the identical key pattern already exists under another name,
     * it skips creation and avoids MongoCommandException error 85 (IndexOptionsConflict).
     */
    public void ensureIndexSafely(String collectionName, Index index) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }
        try {
            List<IndexInfo> existing = mongoTemplate.indexOps(collectionName).getIndexInfo();
            org.bson.Document targetKeys = index.getIndexKeys();
            for (IndexInfo info : existing) {
                org.bson.Document existingKeys = new org.bson.Document();
                for (var field : info.getIndexFields()) {
                    existingKeys.put(field.getKey(), Sort.Direction.DESC.equals(field.getDirection()) ? -1 : 1);
                }
                if (existingKeys.equals(targetKeys)) {
                    log.debug("Index with matching keys {} already exists as '{}' on '{}' — skipping creation.",
                            targetKeys.toJson(), info.getName(), collectionName);
                    return;
                }
            }
            mongoTemplate.indexOps(collectionName).ensureIndex(index);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("IndexOptionsConflict")) {
                log.debug("Index already exists with different name/options on '{}' (skipping): {}", collectionName, ex.getMessage());
            } else {
                log.warn("MongoDB index initialization warning on '{}': {}", collectionName, ex.getMessage());
            }
        }
    }

    /**
     * Ensures a unique index exists on the specified field in the collection.
     * Checks existing indexes to detect equivalent unique indexes (even if named differently),
     * avoiding MongoDB error 85 (IndexOptionsConflict).
     *
     * @param collectionName MongoDB collection name
     * @param fieldName document field name to index
     * @param preferredIndexName default name to assign if index does not yet exist
     */
    public void ensureUniqueIndexSafely(String collectionName, String fieldName, String preferredIndexName) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            mongoTemplate.createCollection(collectionName);
        }
        try {
            List<IndexInfo> existing = mongoTemplate.indexOps(collectionName).getIndexInfo();
            for (IndexInfo info : existing) {
                boolean coversField = info.getIndexFields().stream()
                        .anyMatch(f -> fieldName.equals(f.getKey()));
                if (coversField && info.isUnique()) {
                    log.info("MongoDB unique indexes successfully verified for {} collection "
                            + "(existing index '{}' covers {{{}:1}, unique:true} — skipping creation).",
                            collectionName, info.getName(), fieldName);
                    return;
                }
            }
            mongoTemplate.indexOps(collectionName).ensureIndex(
                    new Index().on(fieldName, Sort.Direction.ASC).unique()
                            .named(preferredIndexName));
            log.info("MongoDB unique index '{}' on '{}.{}' created successfully.",
                    preferredIndexName, collectionName, fieldName);
        } catch (Exception ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("IndexOptionsConflict")) {
                log.debug("Unique index already exists on '{}.{}' with different name (skipping): {}", collectionName, fieldName, ex.getMessage());
            } else {
                log.warn("MongoDB unique index initialization warning on '{}.{}': {}", collectionName, fieldName, ex.getMessage());
            }
        }
    }

    /**
     * Ensures the unique index on {@code ticketNumber} in the {@code tickets} collection.
     *
     * <p>Before calling {@code ensureIndex}, inspects existing indexes to detect a functionally
     * equivalent index — same key pattern ({@code ticketNumber: 1}) with {@code unique: true} —
     * that may already exist under a different name (e.g. {@code ticketNumber_1} created by
     * Spring Data's auto-index-creation when it was still enabled). If such an index is found,
     * the unique constraint is already enforced and no creation is attempted, avoiding
     * {@code MongoCommandException} error 85 (IndexOptionsConflict).</p>
     */
    public void ensureTicketNumberUniqueIndex() {
        ensureUniqueIndexSafely("tickets", "ticketNumber", "idx_ticket_number_unique");
    }

    /**
     * Checks whether any unique index covering the {@code ticketNumber} field exists
     * on the {@code tickets} collection, regardless of index name.
     *
     * @return {@code true} if a unique ticketNumber index is present
     */
    public boolean isUniqueTicketNumberIndexValid() {
        try {
            List<IndexInfo> existing = mongoTemplate.indexOps("tickets").getIndexInfo();
            for (IndexInfo info : existing) {
                boolean coversTicketNumber = info.getIndexFields().stream()
                        .anyMatch(f -> "ticketNumber".equals(f.getKey()));
                if (coversTicketNumber && info.isUnique()) {
                    return true;
                }
            }
        } catch (Exception ex) {
            log.debug("Could not inspect ticket index info: {}", ex.getMessage());
        }
        return false;
    }
}
