package com.smarttravel.modules.payment.refund.service;

import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.refund.dto.RefundEligibilityResponse;
import com.smarttravel.modules.payment.refund.model.Refund;
import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import com.smarttravel.modules.payment.refund.repository.RefundRepository;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Production implementation of RefundEligibilityService.
 */
@Service
public class RefundEligibilityServiceImpl implements RefundEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(RefundEligibilityServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final CancellationRefundPolicy cancellationRefundPolicy;

    public RefundEligibilityServiceImpl(BookingRepository bookingRepository,
                                        PaymentRepository paymentRepository,
                                        RefundRepository refundRepository,
                                        CancellationRefundPolicy cancellationRefundPolicy) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.cancellationRefundPolicy = cancellationRefundPolicy;
    }

    @Override
    public RefundEligibilityResponse checkBookingRefundEligibility(String bookingId, RefundReason reason) {
        log.debug("Checking refund eligibility for booking ID: {} (Reason: {})", bookingId, reason);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        Payment payment = paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc(bookingId)
                .orElse(null);

        if (payment == null) {
            return RefundEligibilityResponse.builder()
                    .bookingId(bookingId)
                    .eligible(false)
                    .reason("No payment record found for booking")
                    .refundableAmount(BigDecimal.ZERO)
                    .refundableAmountPaise(0)
                    .alreadyRefunded(false)
                    .build();
        }

        return evaluatePaymentEligibility(payment, booking, reason);
    }

    @Override
    public RefundEligibilityResponse checkPaymentRefundEligibility(String paymentId, RefundReason reason) {
        log.debug("Checking refund eligibility for payment ID: {} (Reason: {})", paymentId, reason);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        Booking booking = bookingRepository.findById(payment.getBookingId()).orElse(null);

        return evaluatePaymentEligibility(payment, booking, reason);
    }

    private RefundEligibilityResponse evaluatePaymentEligibility(Payment payment, Booking booking, RefundReason reason) {
        String paymentId = payment.getId();
        String bookingId = payment.getBookingId();

        // 1. Check if refund already exists or is in progress
        List<RefundStatus> activeStatuses = List.of(RefundStatus.REQUESTED, RefundStatus.PROCESSING, RefundStatus.COMPLETED);
        Optional<Refund> existingRefundOpt = refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc(paymentId);

        if (existingRefundOpt.isPresent() && activeStatuses.contains(existingRefundOpt.get().getStatus())) {
            Refund existing = existingRefundOpt.get();
            return RefundEligibilityResponse.builder()
                    .bookingId(bookingId)
                    .paymentId(paymentId)
                    .eligible(false)
                    .reason("Refund already initiated or completed with status: " + existing.getStatus())
                    .refundReason(existing.getReason())
                    .refundableAmount(BigDecimal.ZERO)
                    .refundableAmountPaise(0)
                    .alreadyRefunded(true)
                    .existingRefundId(existing.getId())
                    .build();
        }

        // 2. Payment must be in VERIFIED status
        if (payment.getPaymentStatus() != PaymentStatus.VERIFIED) {
            return RefundEligibilityResponse.builder()
                    .bookingId(bookingId)
                    .paymentId(paymentId)
                    .eligible(false)
                    .reason("Payment is in status '" + payment.getPaymentStatus() + "'. Only captured/verified payments are eligible for refund.")
                    .refundableAmount(BigDecimal.ZERO)
                    .refundableAmountPaise(0)
                    .alreadyRefunded(false)
                    .build();
        }

        // 3. Calculate eligible refund amount applying the time-based cancellation policy
        long amountPaise = payment.getAmountPaise() != null ? payment.getAmountPaise() :
                (payment.getAmount() != null ? payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue() : 0L);

        RefundReason effectiveReason = reason != null ? reason : RefundReason.CUSTOMER_CANCELLATION;

        // Retrieve departure time from booking for policy calculation
        Instant departureTime = booking != null ? booking.getDepartureTime() : null;
        Instant now = Instant.now();

        // For flight-side disruptions (FLIGHT_CANCELLED, OVERBOOKING, MAJOR_RESCHEDULE)
        // the policy grants 100% regardless of departure proximity.
        long refundPaise;
        BigDecimal refundInr;
        String refundPercentage;
        String policyDescription;

        if (effectiveReason == RefundReason.FLIGHT_CANCELLED
                || effectiveReason == RefundReason.OVERBOOKING
                || effectiveReason == RefundReason.MAJOR_RESCHEDULE) {
            // Full refund for airline-initiated disruption
            refundPaise = amountPaise;
            refundInr = payment.getAmount() != null ? payment.getAmount() :
                    BigDecimal.valueOf(amountPaise).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            refundPercentage = "100%";
            policyDescription = "Full refund: airline-initiated disruption (" + effectiveReason + ")";
        } else {
            // Time-based policy for CUSTOMER_CANCELLATION / OTHER
            refundPaise = cancellationRefundPolicy.calculateRefundAmountPaise(amountPaise, departureTime, now);
            refundInr = cancellationRefundPolicy.calculateRefundAmountInr(amountPaise, departureTime, now);
            refundPercentage = cancellationRefundPolicy.getRefundPercentageLabel(departureTime, now);
            policyDescription = cancellationRefundPolicy.getPolicyDescription(departureTime, now);
        }

        boolean eligible = refundPaise > 0L;
        String eligibilityReason = eligible
                ? ("Eligible for " + refundPercentage + " refund. Policy: " + policyDescription)
                : ("Not eligible for refund: " + policyDescription);

        return RefundEligibilityResponse.builder()
                .bookingId(bookingId)
                .paymentId(paymentId)
                .eligible(eligible)
                .reason(eligibilityReason)
                .refundReason(effectiveReason)
                .refundableAmount(refundInr)
                .refundableAmountPaise(refundPaise)
                .refundPercentage(refundPercentage)
                .policyDescription(policyDescription)
                .alreadyRefunded(false)
                .build();
    }
}
