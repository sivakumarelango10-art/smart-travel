package com.smarttravel.modules.pricing.repository;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.pricing.model.FlightPriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for flight price history records.
 */
public interface FlightPriceHistoryRepository extends MongoRepository<FlightPriceHistory, String> {

    Page<FlightPriceHistory> findByFlightIdOrderByCapturedAtDesc(String flightId, Pageable pageable);

    Page<FlightPriceHistory> findByFlightIdAndCabinClassOrderByCapturedAtDesc(
            String flightId, CabinClass cabinClass, Pageable pageable);

    Page<FlightPriceHistory> findByFlightIdAndCapturedAtBetweenOrderByCapturedAtDesc(
            String flightId, Instant from, Instant to, Pageable pageable);

    Page<FlightPriceHistory> findByFlightIdAndCabinClassAndCapturedAtBetweenOrderByCapturedAtDesc(
            String flightId, CabinClass cabinClass, Instant from, Instant to, Pageable pageable);

    /** Count snapshots for a flight in last N minutes — used to prevent over-recording */
    long countByFlightIdAndCabinClassAndCapturedAtAfter(String flightId, CabinClass cabinClass, Instant since);
}
