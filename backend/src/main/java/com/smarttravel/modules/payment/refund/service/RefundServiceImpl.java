package com.smarttravel.modules.payment.refund.service;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.notification.dto.NotificationSendRequest;
import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationType;
import com.smarttravel.modules.notification.service.NotificationService;
import com.smarttravel.modules.payment.gateway.RazorpayPaymentGateway;
import com.smarttravel.modules.payment.gateway.dto.RazorpayRefundDto;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.refund.dto.RefundEligibilityResponse;
import com.smarttravel.modules.payment.refund.dto.RefundProcessRequest;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
import com.smarttravel.modules.payment.refund.model.Refund;
import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import com.smarttravel.modules.payment.refund.repository.RefundRepository;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Production-grade implementation of RefundService managing refund execution, idempotency,
 * state machine transitions, and customer communications.
 */
@Service
public class RefundServiceImpl implements RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundServiceImpl.class);
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RefundEligibilityService refundEligibilityService;
    private final RefundStateMachine refundStateMachine;
    private final RazorpayPaymentGateway razorpayPaymentGateway;
    private final NotificationService notificationService;

    public RefundServiceImpl(RefundRepository refundRepository,
                             PaymentRepository paymentRepository,
                             BookingRepository bookingRepository,
                             RefundEligibilityService refundEligibilityService,
                             RefundStateMachine refundStateMachine,
                             RazorpayPaymentGateway razorpayPaymentGateway,
                             NotificationService notificationService) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.refundEligibilityService = refundEligibilityService;
        this.refundStateMachine = refundStateMachine;
        this.razorpayPaymentGateway = razorpayPaymentGateway;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public synchronized RefundResponse processRefund(String paymentId, RefundProcessRequest request, String createdBy) {
        log.info("Initiating refund processing for payment ID: {} by: {}", paymentId, createdBy);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));

        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", payment.getBookingId()));

        RefundReason reason = request != null && request.getReason() != null ? request.getReason() : RefundReason.FLIGHT_CANCELLED;
        String description = request != null ? request.getDescription() : null;

        // 1. Validate Eligibility
        RefundEligibilityResponse eligibility = refundEligibilityService.checkPaymentRefundEligibility(paymentId, reason);
        if (!eligibility.isEligible()) {
            if (eligibility.isAlreadyRefunded() && eligibility.getExistingRefundId() != null) {
                log.info("Payment ID: {} already refunded. Returning existing refund: {}", paymentId, eligibility.getExistingRefundId());
                Refund existing = refundRepository.findById(eligibility.getExistingRefundId())
                        .orElseThrow(() -> new ResourceNotFoundException("Refund", "id", eligibility.getExistingRefundId()));
                return toDto(existing);
            }
            throw new BadRequestException("Payment is not eligible for refund: " + eligibility.getReason());
        }

        // 2. Create Initial Refund Record (Status: REQUESTED)
        String refundNumber = generateRefundNumber();
        long amountPaise = eligibility.getRefundableAmountPaise();
        BigDecimal amountInr = eligibility.getRefundableAmount();

        Refund refund = Refund.builder()
                .refundNumber(refundNumber)
                .paymentId(paymentId)
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(booking.getUserId())
                .amount(amountInr)
                .amountPaise(amountPaise)
                .currency(payment.getCurrency() != null ? payment.getCurrency() : "INR")
                .reason(reason)
                .description(description)
                .status(RefundStatus.REQUESTED)
                .createdBy(createdBy)
                .requestedAt(Instant.now())
                .build();

        refund = refundRepository.save(refund);

        // 3. Transition to PROCESSING
        refundStateMachine.validateTransition(refund.getStatus(), RefundStatus.PROCESSING);
        refund.setStatus(RefundStatus.PROCESSING);
        refund.setProcessedAt(Instant.now());
        refund = refundRepository.save(refund);

        // 4. Call Payment Gateway Abstraction
        try {
            RazorpayRefundDto gatewayRes = razorpayPaymentGateway.refundPayment(
                    payment.getRazorpayPaymentId() != null ? payment.getRazorpayPaymentId() : paymentId,
                    amountPaise,
                    reason.name()
            );

            // 5. Transition to COMPLETED
            refundStateMachine.validateTransition(refund.getStatus(), RefundStatus.COMPLETED);
            refund.setStatus(RefundStatus.COMPLETED);
            refund.setGatewayRefundId(gatewayRes.getId());
            refund.setCompletedAt(Instant.now());
            refund = refundRepository.save(refund);

            log.info("Refund {} successfully completed via gateway for payment ID: {} (Gateway Ref: {})",
                    refundNumber, paymentId, gatewayRes.getId());

            // 6. Notify Customer
            sendRefundNotification(refund, booking);

        } catch (Exception ex) {
            log.error("Refund processing failed for payment ID: {}: {}", paymentId, ex.getMessage(), ex);
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason(ex.getMessage());
            refund = refundRepository.save(refund);
            throw new BadRequestException("Gateway refund processing failed: " + ex.getMessage());
        }

        return toDto(refund);
    }

    @Override
    public List<RefundResponse> processDisruptionRefundsForFlight(String flightId, RefundReason reason, String initiatedBy) {
        log.info("Processing disruption auto-refunds for flight ID: {} (Reason: {})", flightId, reason);

        List<Booking> bookings = bookingRepository.findByFlightId(flightId);
        List<RefundResponse> responses = new ArrayList<>();

        for (Booking booking : bookings) {
            Optional<Payment> paymentOpt = paymentRepository.findFirstByBookingIdOrderByCreatedAtDesc(booking.getId());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                RefundEligibilityResponse eligibility = refundEligibilityService.checkPaymentRefundEligibility(payment.getId(), reason);
                if (eligibility.isEligible()) {
                    try {
                        RefundResponse refundRes = processRefund(
                                payment.getId(),
                                new RefundProcessRequest(reason, "Automatic refund due to flight disruption"),
                                initiatedBy
                        );
                        responses.add(refundRes);
                    } catch (Exception ex) {
                        log.warn("Auto-refund failed for payment ID: {}: {}", payment.getId(), ex.getMessage());
                    }
                }
            }
        }

        return responses;
    }

    @Override
    public RefundResponse getRefundById(String refundId, String userId, boolean isAdmin) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", "id", refundId));

        if (!isAdmin && !refund.getUserId().equals(userId)) {
            // Strict IDOR protection
            throw new ResourceNotFoundException("Refund", "id", refundId);
        }

        return toDto(refund);
    }

    @Override
    public RefundResponse getRefundByBookingId(String bookingId, String userId, boolean isAdmin) {
        Refund refund = refundRepository.findFirstByBookingIdOrderByCreatedAtDesc(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund", "bookingId", bookingId));

        if (!isAdmin && !refund.getUserId().equals(userId)) {
            // Strict IDOR protection
            throw new ResourceNotFoundException("Refund", "bookingId", bookingId);
        }

        return toDto(refund);
    }

    @Override
    public PageResponse<RefundResponse> getAllRefunds(RefundStatus status, Pageable pageable) {
        Page<Refund> page;
        if (status != null) {
            page = refundRepository.findByStatus(status, pageable);
        } else {
            page = refundRepository.findAll(pageable);
        }
        return PageResponse.from(page.map(this::toDto));
    }

    private void sendRefundNotification(Refund refund, Booking booking) {
        try {
            String subject = String.format("SmartTravel Refund Processed: %s (PNR: %s)", refund.getRefundNumber(), booking.getBookingReference());
            String body = String.format("Dear Passenger,\n\nA refund of INR %.2f has been successfully processed for booking PNR %s.\nRefund Reference: %s\nGateway Reference: %s\nAmount should reflect in your source payment account within 3-5 business days.",
                    refund.getAmount(), booking.getBookingReference(), refund.getRefundNumber(), refund.getGatewayRefundId());

            notificationService.sendNotification(NotificationSendRequest.builder()
                    .userId(refund.getUserId())
                    .bookingId(booking.getId())
                    .flightId(booking.getFlightId())
                    .notificationType(NotificationType.REFUND_COMPLETED)
                    .channel(NotificationChannel.EMAIL)
                    .subject(subject)
                    .content(body)
                    .eventId("refund_" + refund.getId())
                    .build());
        } catch (Exception ex) {
            log.warn("Failed to dispatch refund notification for refund {}: {}", refund.getRefundNumber(), ex.getMessage());
        }
    }

    private String generateRefundNumber() {
        StringBuilder sb = new StringBuilder("RF-");
        for (int i = 0; i < 12; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private RefundResponse toDto(Refund refund) {
        return RefundResponse.builder()
                .id(refund.getId())
                .refundNumber(refund.getRefundNumber())
                .paymentId(refund.getPaymentId())
                .razorpayPaymentId(refund.getRazorpayPaymentId())
                .bookingId(refund.getBookingId())
                .bookingReference(refund.getBookingReference())
                .userId(refund.getUserId())
                .amount(refund.getAmount())
                .amountPaise(refund.getAmountPaise())
                .currency(refund.getCurrency())
                .reason(refund.getReason())
                .description(refund.getDescription())
                .status(refund.getStatus())
                .gatewayRefundId(refund.getGatewayRefundId())
                .failureReason(refund.getFailureReason())
                .requestedAt(refund.getRequestedAt())
                .completedAt(refund.getCompletedAt())
                .build();
    }
}
