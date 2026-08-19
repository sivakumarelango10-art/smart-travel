package com.smarttravel.modules.flight.repository;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Seat entities.
 */
@Repository
public interface SeatRepository extends MongoRepository<Seat, String>, SeatRepositoryCustom {

    List<Seat> findByFlightIdOrderByRowNumberAscColumnAsc(String flightId);

    List<Seat> findByFlightIdAndCabinClassOrderByRowNumberAscColumnAsc(String flightId, CabinClass cabinClass);

    Optional<Seat> findByFlightIdAndSeatNumber(String flightId, String seatNumber);

    List<Seat> findByBookingId(String bookingId);

    List<Seat> findByFlightIdAndStatus(String flightId, SeatStatus status);

    boolean existsByFlightId(String flightId);

    long deleteByFlightId(String flightId);
}
