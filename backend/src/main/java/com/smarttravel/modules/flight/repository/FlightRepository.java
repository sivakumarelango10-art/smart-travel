package com.smarttravel.modules.flight.repository;

import com.smarttravel.modules.flight.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlightRepository extends MongoRepository<Flight, String>, FlightRepositoryCustom {

    Optional<Flight> findByIdAndActiveTrue(String id);

    Optional<Flight> findByFlightNumber(String flightNumber);

    Optional<Flight> findByFlightNumberAndActiveTrue(String flightNumber);

    boolean existsByFlightNumber(String flightNumber);

    long countByDepartureTimeBetweenAndActiveTrue(java.time.Instant start, java.time.Instant end);

    Page<Flight> findByActiveTrue(Pageable pageable);
}
