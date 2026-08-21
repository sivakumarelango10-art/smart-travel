package com.smarttravel.modules.flight.tracking.repository;

import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for tracked flights per user.
 */
public interface TrackedFlightRepository extends MongoRepository<TrackedFlight, String> {

    Optional<TrackedFlight> findByUserIdAndFlightId(String userId, String flightId);

    List<TrackedFlight> findByUserIdAndActiveTrue(String userId);

    List<TrackedFlight> findByFlightIdAndActiveTrue(String flightId);

    List<TrackedFlight> findByActiveTrue();

    boolean existsByUserIdAndFlightId(String userId, String flightId);

    void deleteByUserIdAndFlightId(String userId, String flightId);
}
