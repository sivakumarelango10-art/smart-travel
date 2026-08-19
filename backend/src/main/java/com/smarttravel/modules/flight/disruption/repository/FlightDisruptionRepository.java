package com.smarttravel.modules.flight.disruption.repository;

import com.smarttravel.modules.flight.disruption.model.DisruptionStatus;
import com.smarttravel.modules.flight.disruption.model.FlightDisruption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB repository for FlightDisruption entities.
 */
@Repository
public interface FlightDisruptionRepository extends MongoRepository<FlightDisruption, String> {

    List<FlightDisruption> findByFlightIdOrderByCreatedAtDesc(String flightId);

    Page<FlightDisruption> findByFlightId(String flightId, Pageable pageable);

    List<FlightDisruption> findByFlightIdAndStatus(String flightId, DisruptionStatus status);

    List<FlightDisruption> findByStatus(DisruptionStatus status);
}
