package com.smarttravel.modules.flight.repository;

import com.smarttravel.modules.flight.model.FlightStatusHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlightStatusHistoryRepository extends MongoRepository<FlightStatusHistory, String> {

    List<FlightStatusHistory> findByFlightIdOrderByChangedAtDesc(String flightId);

    List<FlightStatusHistory> findByFlightNumberOrderByChangedAtDesc(String flightNumber);
}
