package com.smarttravel.modules.flight.repository;

import com.mongodb.client.result.UpdateResult;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;


/**
 * MongoTemplate implementation of atomic conditional seat updates.
 */
@Repository
public class SeatRepositoryCustomImpl implements SeatRepositoryCustom {

    private static final Logger log = LoggerFactory.getLogger(SeatRepositoryCustomImpl.class);

    private final MongoTemplate mongoTemplate;

    public SeatRepositoryCustomImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean atomicHoldSeat(String flightId, String seatNumber, String bookingId, String bookingReference, Instant expiresAt) {
        Instant now = Instant.now();

        // Seat is available if status is AVAILABLE, or if it was HELD but the hold has expired
        Criteria availabilityCriteria = new Criteria().orOperator(
                Criteria.where("status").is(SeatStatus.AVAILABLE),
                new Criteria().andOperator(
                        Criteria.where("status").is(SeatStatus.HELD),
                        Criteria.where("expiresAt").lt(now)
                )
        );

        Query query = new Query(new Criteria().andOperator(
                Criteria.where("flightId").is(flightId),
                Criteria.where("seatNumber").is(seatNumber),
                availabilityCriteria
        ));

        Update update = new Update()
                .set("status", SeatStatus.HELD)
                .set("bookingId", bookingId)
                .set("bookingReference", bookingReference)
                .set("heldAt", now)
                .set("expiresAt", expiresAt)
                .set("updatedAt", now);

        UpdateResult result = mongoTemplate.updateFirst(query, update, Seat.class);
        boolean modified = result.getModifiedCount() > 0;
        if (modified) {
            log.info("Atomically held seat {} on flight {} for booking {}", seatNumber, flightId, bookingId);
        } else {
            log.warn("Failed to atomically hold seat {} on flight {} (already taken or blocked)", seatNumber, flightId);
        }
        return modified;
    }

    @Override
    public boolean atomicConfirmSeat(String flightId, String seatNumber, String bookingId) {
        Instant now = Instant.now();

        Query query = new Query(Criteria.where("flightId").is(flightId)
                .and("seatNumber").is(seatNumber)
                .and("bookingId").is(bookingId)
                .and("status").in(SeatStatus.HELD, SeatStatus.AVAILABLE));

        Update update = new Update()
                .set("status", SeatStatus.BOOKED)
                .set("updatedAt", now);

        UpdateResult result = mongoTemplate.updateFirst(query, update, Seat.class);
        return result.getModifiedCount() > 0;
    }

    @Override
    public long confirmSeatsForBooking(String bookingId) {
        Instant now = Instant.now();

        Query query = new Query(Criteria.where("bookingId").is(bookingId)
                .and("status").is(SeatStatus.HELD));

        Update update = new Update()
                .set("status", SeatStatus.BOOKED)
                .set("updatedAt", now);

        UpdateResult result = mongoTemplate.updateMulti(query, update, Seat.class);
        log.info("Confirmed {} seats for booking ID: {}", result.getModifiedCount(), bookingId);
        return result.getModifiedCount();
    }

    @Override
    public long releaseSeatsForBooking(String bookingId) {
        Instant now = Instant.now();

        Query query = new Query(Criteria.where("bookingId").is(bookingId)
                .and("status").in(SeatStatus.HELD, SeatStatus.BOOKED));

        Update update = new Update()
                .set("status", SeatStatus.AVAILABLE)
                .set("bookingId", null)
                .set("bookingReference", null)
                .set("passengerId", null)
                .set("heldAt", null)
                .set("expiresAt", null)
                .set("updatedAt", now);

        UpdateResult result = mongoTemplate.updateMulti(query, update, Seat.class);
        log.info("Released {} seats for booking ID: {}", result.getModifiedCount(), bookingId);
        return result.getModifiedCount();
    }

    @Override
    public long releaseExpiredSeatHolds(Instant now) {
        Query query = new Query(Criteria.where("status").is(SeatStatus.HELD)
                .and("expiresAt").lt(now));

        Update update = new Update()
                .set("status", SeatStatus.AVAILABLE)
                .set("bookingId", null)
                .set("bookingReference", null)
                .set("passengerId", null)
                .set("heldAt", null)
                .set("expiresAt", null)
                .set("updatedAt", now);

        UpdateResult result = mongoTemplate.updateMulti(query, update, Seat.class);
        if (result.getModifiedCount() > 0) {
            log.info("Released {} expired seat holds", result.getModifiedCount());
        }
        return result.getModifiedCount();
    }
}
