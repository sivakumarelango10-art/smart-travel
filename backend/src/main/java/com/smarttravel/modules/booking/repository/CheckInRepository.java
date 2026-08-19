package com.smarttravel.modules.booking.repository;

import com.smarttravel.modules.booking.model.CheckIn;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for CheckIn entities.
 */
@Repository
public interface CheckInRepository extends MongoRepository<CheckIn, String> {

    Optional<CheckIn> findByBookingId(String bookingId);

    Optional<CheckIn> findByBookingIdAndUserId(String bookingId, String userId);

    Optional<CheckIn> findByCheckInNumber(String checkInNumber);

    List<CheckIn> findByUserId(String userId);

    boolean existsByBookingId(String bookingId);
}
