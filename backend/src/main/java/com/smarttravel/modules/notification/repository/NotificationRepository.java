package com.smarttravel.modules.notification.repository;

import com.smarttravel.modules.notification.model.Notification;
import com.smarttravel.modules.notification.model.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Notification entities.
 */
@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {

    Optional<Notification> findByIdempotencyKey(String idempotencyKey);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    long countByUserIdAndReadFalse(String userId);

    List<Notification> findByUserIdAndReadFalse(String userId);

    Page<Notification> findByStatus(NotificationStatus status, Pageable pageable);

    List<Notification> findByFlightId(String flightId);

    List<Notification> findByBookingId(String bookingId);

    boolean existsByIdempotencyKey(String idempotencyKey);
}
