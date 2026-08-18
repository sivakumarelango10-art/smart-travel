package com.smarttravel.modules.booking.repository;

import com.mongodb.client.result.UpdateResult;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Implementation of atomic seat inventory reservations using conditional MongoDB updates.
 */
@Repository
public class FlightInventoryReservationRepositoryImpl implements FlightInventoryReservationRepository {

    private static final Logger log = LoggerFactory.getLogger(FlightInventoryReservationRepositoryImpl.class);

    private static final List<FlightStatus> BOOKABLE_STATUSES = List.of(
            FlightStatus.SCHEDULED,
            FlightStatus.BOARDING,
            FlightStatus.ON_TIME,
            FlightStatus.DELAYED
    );

    private final MongoTemplate mongoTemplate;

    public FlightInventoryReservationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public boolean reserveCabinSeats(String flightId, CabinClass cabinClass, int seatCount) {
        if (flightId == null || cabinClass == null || seatCount <= 0) {
            return false;
        }

        // 1. Primary path: Match specific cabin within cabinInventories having availableSeats >= seatCount
        Query cabinQuery = new Query();
        cabinQuery.addCriteria(Criteria.where("_id").is(flightId)
                .and("active").is(true)
                .and("status").in(BOOKABLE_STATUSES)
                .and("cabinInventories").elemMatch(
                        Criteria.where("cabinClass").is(cabinClass)
                                .and("availableSeats").gte(seatCount)
                ));

        Update cabinUpdate = new Update()
                .inc("cabinInventories.$.availableSeats", -seatCount)
                .inc("availableSeats", -seatCount)
                .set("updatedAt", Instant.now());

        UpdateResult result = mongoTemplate.updateFirst(cabinQuery, cabinUpdate, Flight.class);
        if (result.getModifiedCount() > 0) {
            log.info("Atomically reserved {} seat(s) in cabin {} for flight ID: {}", seatCount, cabinClass, flightId);
            return true;
        }

        // 2. Fallback path for legacy documents where cabinInventories is empty/null but aggregate availableSeats >= seatCount
        Query legacyQuery = new Query();
        legacyQuery.addCriteria(Criteria.where("_id").is(flightId)
                .and("active").is(true)
                .and("status").in(BOOKABLE_STATUSES)
                .and("availableSeats").gte(seatCount)
                .orOperator(
                        Criteria.where("cabinInventories").exists(false),
                        Criteria.where("cabinInventories").size(0)
                ));

        Update legacyUpdate = new Update()
                .inc("availableSeats", -seatCount)
                .set("updatedAt", Instant.now());

        UpdateResult legacyResult = mongoTemplate.updateFirst(legacyQuery, legacyUpdate, Flight.class);
        if (legacyResult.getModifiedCount() > 0) {
            log.info("Atomically reserved {} seat(s) in legacy flight inventory for flight ID: {}", seatCount, flightId);
            return true;
        }

        log.warn("Atomic seat reservation failed for flight ID: {}, cabin: {}, requested seats: {}", flightId, cabinClass, seatCount);
        return false;
    }

    @Override
    public boolean releaseCabinSeats(String flightId, CabinClass cabinClass, int seatCount) {
        if (flightId == null || cabinClass == null || seatCount <= 0) {
            return false;
        }

        // 1. Primary path: Release seats back to matching cabin inventory element
        Query cabinQuery = new Query();
        cabinQuery.addCriteria(Criteria.where("_id").is(flightId)
                .and("cabinInventories").elemMatch(Criteria.where("cabinClass").is(cabinClass)));

        Update cabinUpdate = new Update()
                .inc("cabinInventories.$.availableSeats", seatCount)
                .inc("availableSeats", seatCount)
                .set("updatedAt", Instant.now());

        UpdateResult result = mongoTemplate.updateFirst(cabinQuery, cabinUpdate, Flight.class);
        if (result.getModifiedCount() > 0) {
            log.info("Atomically released {} seat(s) back into cabin {} for flight ID: {}", seatCount, cabinClass, flightId);
            return true;
        }

        // 2. Fallback path: Release back to aggregate availableSeats for legacy flights
        Query legacyQuery = new Query();
        legacyQuery.addCriteria(Criteria.where("_id").is(flightId));

        Update legacyUpdate = new Update()
                .inc("availableSeats", seatCount)
                .set("updatedAt", Instant.now());

        UpdateResult legacyResult = mongoTemplate.updateFirst(legacyQuery, legacyUpdate, Flight.class);
        if (legacyResult.getModifiedCount() > 0) {
            log.info("Atomically released {} seat(s) into legacy flight inventory for flight ID: {}", seatCount, flightId);
            return true;
        }

        log.error("Failed to release {} seat(s) for flight ID: {}", seatCount, flightId);
        return false;
    }
}
