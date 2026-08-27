package com.smarttravel.modules.hotel.repository;

import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.hotel.model.HotelBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelBookingRepository extends MongoRepository<HotelBooking, String> {

    Page<HotelBooking> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Page<HotelBooking> findByUserIdAndStatusOrderByCreatedAtDesc(String userId, BookingStatus status, Pageable pageable);

    Optional<HotelBooking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    long countByUserId(String userId);
}
