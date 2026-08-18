package com.smarttravel.modules.flight.simulation.repository;

import com.smarttravel.modules.flight.simulation.model.FlightSimulationConfig;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlightSimulationConfigRepository extends MongoRepository<FlightSimulationConfig, String> {

    Optional<FlightSimulationConfig> findByFlightId(String flightId);

    List<FlightSimulationConfig> findByEnabledTrueAndCompletedFalse();

    boolean existsByFlightId(String flightId);
}
