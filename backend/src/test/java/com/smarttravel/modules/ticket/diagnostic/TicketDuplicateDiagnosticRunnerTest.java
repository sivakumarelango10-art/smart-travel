package com.smarttravel.modules.ticket.diagnostic;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.repository.BookingRepository;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Diagnostic and safe deduplication runner for MongoDB Tickets collection.
 * Identifies any duplicate bookingId records, evaluates their integrity against associated bookings,
 * cleans up orphaned duplicates, and enforces the unique index.
 */
@SpringBootTest
class TicketDuplicateDiagnosticRunnerTest {

    private static final Logger log = LoggerFactory.getLogger(TicketDuplicateDiagnosticRunnerTest.class);

    @Autowired
    private MongoTemplate mongoTemplate;


    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Diagnose and Clean Duplicate BookingIds in Tickets Collection")
    void diagnoseAndCleanDuplicateBookingIds() {
        assertNotNull(mongoTemplate, "MongoTemplate must be available");

        log.info("=== STARTING TICKET DUPLICATE DIAGNOSTIC RUN ===");

        // 1. Run aggregation to group by bookingId and find count > 1
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("bookingId").exists(true).ne(null)),
                Aggregation.group("bookingId").count().as("count").push("$$ROOT").as("tickets"),
                Aggregation.match(Criteria.where("count").gt(1))
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "tickets", Document.class);
        List<Document> duplicateGroups = results.getMappedResults();

        log.info("Found {} duplicate bookingId groups in tickets collection.", duplicateGroups.size());

        int totalDeleted = 0;
        int totalPreserved = 0;

        for (Document group : duplicateGroups) {
            String bookingId = group.getString("_id");
            Integer count = group.getInteger("count");
            List<Document> ticketDocs = group.getList("tickets", Document.class);

            log.info("-------------------------------------------------------------------");
            log.info("Duplicate bookingId: '{}' (Total duplicates: {})", bookingId, count);

            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            String linkedTicketId = bookingOpt.map(Booking::getTicketId).orElse(null);
            String linkedTicketNumber = bookingOpt.map(Booking::getTicketNumber).orElse(null);

            log.info("Associated Booking PNR: {}, Linked Ticket ID: {}, Linked Ticket Number: {}",
                    bookingOpt.map(Booking::getBookingReference).orElse("NOT_FOUND"),
                    linkedTicketId, linkedTicketNumber);

            Document authoritativeDoc = null;
            List<Document> toDelete = new ArrayList<>();

            // Find best candidate to preserve
            for (Document doc : ticketDocs) {
                String docId = doc.get("_id") != null ? doc.get("_id").toString() : null;
                String docTicketNumber = doc.getString("ticketNumber");
                String status = doc.getString("status");
                log.info("  -> Ticket Record: _id={}, ticketNumber={}, status={}, issuedAt={}, createdAt={}",
                        docId, docTicketNumber, status, doc.get("issuedAt"), doc.get("createdAt"));

                if (authoritativeDoc == null) {
                    authoritativeDoc = doc;
                } else {
                    // Check if current doc matches booking's referenced ticket
                    if (docId != null && docId.equals(linkedTicketId)) {
                        toDelete.add(authoritativeDoc);
                        authoritativeDoc = doc;
                    } else if (docTicketNumber != null && docTicketNumber.equals(linkedTicketNumber)) {
                        toDelete.add(authoritativeDoc);
                        authoritativeDoc = doc;
                    } else if ("ISSUED".equals(status) && !"ISSUED".equals(authoritativeDoc.getString("status"))) {
                        toDelete.add(authoritativeDoc);
                        authoritativeDoc = doc;
                    } else {
                        toDelete.add(doc);
                    }
                }
            }

            if (authoritativeDoc != null) {
                log.info("  ==> PRESERVING authoritative ticket: _id={}, ticketNumber={}",
                        authoritativeDoc.get("_id"), authoritativeDoc.getString("ticketNumber"));
            }

            for (Document delDoc : toDelete) {
                Object delId = delDoc.get("_id");
                log.info("  ==> REMOVING duplicate ticket: _id={}, ticketNumber={}", delId, delDoc.getString("ticketNumber"));
                mongoTemplate.remove(new Query(Criteria.where("_id").is(delId)), "tickets");
                totalDeleted++;
            }
            totalPreserved++;
        }

        log.info("=== DIAGNOSTIC COMPLETE: Preserved {} tickets, Removed {} duplicates ===", totalPreserved, totalDeleted);

        // 2. Now verify that the unique index can be built cleanly
        try {
            mongoTemplate.indexOps("tickets").ensureIndex(
                    new Index().on("bookingId", Sort.Direction.ASC).unique().named("bookingId_1")
            );
            mongoTemplate.indexOps("tickets").ensureIndex(
                    new Index().on("ticketNumber", Sort.Direction.ASC).unique().named("ticketNumber_1")
            );
            log.info("SUCCESS: Unique index on 'bookingId' and 'ticketNumber' created and verified on MongoDB!");
        } catch (Exception ex) {
            log.error("Index creation error: ", ex);
            throw new RuntimeException("Failed to ensure unique index after deduplication", ex);
        }
    }
}
