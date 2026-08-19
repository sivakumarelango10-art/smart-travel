package com.smarttravel.modules.payment.repository;

import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for Payment entities.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    Optional<Payment> findByIdAndUserId(String id, String userId);

    Optional<Payment> findByBookingIdAndUserId(String bookingId, String userId);

    List<Payment> findByBookingId(String bookingId);

    Optional<Payment> findFirstByBookingIdOrderByCreatedAtDesc(String bookingId);

    List<Payment> findByUserId(String userId);

    Optional<Payment> findFirstByBookingIdAndPaymentStatusIn(String bookingId, Collection<PaymentStatus> statuses);

    boolean existsByRazorpayOrderId(String razorpayOrderId);

    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}
