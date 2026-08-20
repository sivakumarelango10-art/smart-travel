package com.smarttravel.modules.payment.refund.repository;

import com.smarttravel.modules.payment.refund.model.Refund;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Refund entities.
 */
@Repository
public interface RefundRepository extends MongoRepository<Refund, String> {

    Optional<Refund> findByRefundNumber(String refundNumber);

    List<Refund> findByPaymentId(String paymentId);

    List<Refund> findByBookingId(String bookingId);

    Optional<Refund> findFirstByBookingIdOrderByCreatedAtDesc(String bookingId);

    Optional<Refund> findFirstByPaymentIdOrderByCreatedAtDesc(String paymentId);

    List<Refund> findByUserId(String userId);

    Page<Refund> findByStatus(RefundStatus status, Pageable pageable);

    boolean existsByPaymentIdAndStatusIn(String paymentId, List<RefundStatus> statuses);

    boolean existsByBookingIdAndStatusIn(String bookingId, List<RefundStatus> statuses);
}
