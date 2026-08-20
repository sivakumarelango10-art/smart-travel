package com.smarttravel.modules.booking.repository;

import com.smarttravel.modules.booking.model.BoardingPass;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for BoardingPass entities.
 */
@Repository
public interface BoardingPassRepository extends MongoRepository<BoardingPass, String> {

    List<BoardingPass> findByBookingId(String bookingId);

    List<BoardingPass> findByBookingIdAndUserId(String bookingId, String userId);

    Optional<BoardingPass> findByBoardingPassNumber(String boardingPassNumber);

    List<BoardingPass> findByBookingReference(String bookingReference);

    List<BoardingPass> findByCheckInId(String checkInId);

    boolean existsByBookingId(String bookingId);
}
