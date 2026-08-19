package com.smarttravel.modules.booking.repository;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Booking entities.
 */
@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {

    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    Optional<Booking> findByIdAndUserId(String id, String userId);

    Optional<Booking> findByBookingReferenceAndUserId(String bookingReference, String userId);

    Page<Booking> findByUserId(String userId, Pageable pageable);

    Page<Booking> findByUserIdAndStatus(String userId, BookingStatus status, Pageable pageable);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    List<Booking> findByFlightId(String flightId);

    List<Booking> findByFlightIdAndStatus(String flightId, BookingStatus status);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, java.time.Instant now);
}
