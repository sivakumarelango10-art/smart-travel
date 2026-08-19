package com.smarttravel.modules.payment.service;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingStateMachine;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * Production implementation of PaymentReconciliationService.
 * Handles state verification, monetary checks, atomic booking confirmation, and late-payment conflict safety.
 */
@Service
public class PaymentReconciliationServiceImpl implements PaymentReconciliationService {

    private static final Logger log = LoggerFactory.getLogger(PaymentReconciliationServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PaymentStateMachine paymentStateMachine;
    private final BookingStateMachine bookingStateMachine;
    private final MongoTemplate mongoTemplate;
    private final com.smarttravel.modules.ticket.service.TicketService ticketService;
    private final com.smarttravel.modules.flight.service.SeatMapService seatMapService;

    @Autowired
    public PaymentReconciliationServiceImpl(PaymentRepository paymentRepository,
                                            BookingRepository bookingRepository,
                                            PaymentStateMachine paymentStateMachine,
                                            BookingStateMachine bookingStateMachine,
                                            @Autowired(required = false) MongoTemplate mongoTemplate,
                                            @org.springframework.context.annotation.Lazy @Autowired(required = false) com.smarttravel.modules.ticket.service.TicketService ticketService,
                                            @org.springframework.context.annotation.Lazy @Autowired(required = false) com.smarttravel.modules.flight.service.SeatMapService seatMapService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.paymentStateMachine = paymentStateMachine;
        this.bookingStateMachine = bookingStateMachine;
        this.mongoTemplate = mongoTemplate;
        this.ticketService = ticketService;
        this.seatMapService = seatMapService;
        log.info("PaymentReconciliationServiceImpl initialized with ticketService: {}, seatMapService: {}",
                ticketService != null ? "ACTIVE" : "NULL",
                seatMapService != null ? "ACTIVE" : "NULL");
    }

    public PaymentReconciliationServiceImpl(PaymentRepository paymentRepository,
                                            BookingRepository bookingRepository,
                                            PaymentStateMachine paymentStateMachine,
                                            BookingStateMachine bookingStateMachine,
                                            MongoTemplate mongoTemplate,
                                            com.smarttravel.modules.ticket.service.TicketService ticketService) {
        this(paymentRepository, bookingRepository, paymentStateMachine, bookingStateMachine, mongoTemplate, ticketService, null);
    }

    public PaymentReconciliationServiceImpl(PaymentRepository paymentRepository,
                                            BookingRepository bookingRepository,
                                            PaymentStateMachine paymentStateMachine,
                                            BookingStateMachine bookingStateMachine) {
        this(paymentRepository, bookingRepository, paymentStateMachine, bookingStateMachine, null, null, null);
    }

    @Override
    @Transactional
    public Payment reconcilePaymentSuccess(String razorpayOrderId, String razorpayPaymentId, long amountPaise, String currency, String eventType) {
        log.info("Reconciling successful payment for Razorpay Order ID: {}, Payment ID: {}, Event: {}", razorpayOrderId, razorpayPaymentId, eventType);

        Optional<Payment> paymentOpt = Optional.empty();
        if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
            paymentOpt = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        }
        if (paymentOpt.isEmpty() && razorpayPaymentId != null && !razorpayPaymentId.isBlank()) {
            paymentOpt = paymentRepository.findByRazorpayPaymentId(razorpayPaymentId);
        }

        if (paymentOpt.isEmpty()) {
            log.warn("Payment reconciliation skipped: No payment record found for Razorpay Order: {}", razorpayOrderId);
            return null;
        }

        Payment payment = paymentOpt.get();

        // 1. Amount verification (paise level precision)
        if (amountPaise > 0 && payment.getAmountPaise() != null && payment.getAmountPaise() != amountPaise) {
            log.error("Payment reconciliation mismatch: Expected {} paise, received {} paise for payment ID: {}",
                    payment.getAmountPaise(), amountPaise, payment.getId());
            payment.setFailureReason("Amount mismatch: expected " + payment.getAmountPaise() + " paise, got " + amountPaise);
            return paymentRepository.save(payment);
        }

        // 2. Currency verification
        if (currency != null && payment.getCurrency() != null && !payment.getCurrency().equalsIgnoreCase(currency)) {
            log.error("Payment reconciliation mismatch: Expected currency {}, received {} for payment ID: {}",
                    payment.getCurrency(), currency, payment.getId());
            payment.setFailureReason("Currency mismatch: expected " + payment.getCurrency() + ", got " + currency);
            return paymentRepository.save(payment);
        }

        // 3. Find and inspect associated booking
        Booking booking = bookingRepository.findById(payment.getBookingId()).orElse(null);
        if (booking == null) {
            log.error("Payment reconciliation failed: Associated booking {} not found for payment {}", payment.getBookingId(), payment.getId());
            payment.setFailureReason("Associated booking not found: " + payment.getBookingId());
            return paymentRepository.save(payment);
        }

        // 4. Handle based on Booking Status
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            if (payment.getPaymentStatus() == PaymentStatus.VERIFIED) {
                log.info("Payment and Booking ID: {} already confirmed. Returning idempotent success.", booking.getId());
                if (ticketService != null) {
                    try {
                        ticketService.issueTicket(booking.getId());
                    } catch (Exception ex) {
                        log.warn("Non-fatal: Ticket issuance error during idempotent confirmation for booking ID: {}", booking.getId(), ex);
                    }
                }
                if (seatMapService != null) {
                    try {
                        seatMapService.confirmSeats(booking.getId());
                    } catch (Exception ex) {
                        log.warn("Non-fatal: Error confirming seats for booking ID: {}", booking.getId(), ex);
                    }
                }
                return payment;
            }
            // Update payment record to VERIFIED if not yet updated
            payment.setPaymentStatus(PaymentStatus.VERIFIED);
            payment.setRazorpayPaymentId(razorpayPaymentId);
            if (payment.getVerifiedAt() == null) {
                payment.setVerifiedAt(Instant.now());
            }
            Payment saved = paymentRepository.save(payment);
            if (ticketService != null) {
                try {
                    ticketService.issueTicket(booking.getId());
                } catch (Exception ex) {
                    log.warn("Non-fatal: Ticket issuance error for confirmed booking ID: {}", booking.getId(), ex);
                }
            }
            if (seatMapService != null) {
                try {
                    seatMapService.confirmSeats(booking.getId());
                } catch (Exception ex) {
                    log.warn("Non-fatal: Error confirming seats for booking ID: {}", booking.getId(), ex);
                }
            }
            return saved;
        }

        if (booking.getStatus() == BookingStatus.EXPIRED || booking.getStatus() == BookingStatus.CANCELLED) {
            // LATE PAYMENT CONFLICT: Booking is already terminal and seats were released.
            // Do NOT confirm booking and do NOT alter inventory. Flag payment for manual audit.
            log.warn("LATE PAYMENT CONFLICT: Received payment success for {} booking: {} (Payment ID: {}, Razorpay Order: {})",
                    booking.getStatus(), booking.getId(), payment.getId(), razorpayOrderId);

            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setFailureReason("LATE_PAYMENT_CONFLICT: Payment received after booking reached status " + booking.getStatus());
            payment.setUpdatedAt(Instant.now());
            return paymentRepository.save(payment);
        }

        // 5. Atomic conditional update: transition Booking from PENDING to CONFIRMED
        if (mongoTemplate != null) {
            Query bookingQuery = Query.query(
                    Criteria.where("_id").is(booking.getId())
                            .and("status").is(BookingStatus.PENDING)
            );
            Update bookingUpdate = new Update()
                    .set("status", BookingStatus.CONFIRMED)
                    .set("updatedAt", Instant.now());

            UpdateResult updateResult = mongoTemplate.updateFirst(bookingQuery, bookingUpdate, Booking.class);
            if (updateResult.getModifiedCount() == 0) {
                Booking currentBooking = bookingRepository.findById(booking.getId()).orElse(null);
                if (currentBooking != null && currentBooking.getStatus() == BookingStatus.PENDING) {
                    currentBooking.setStatus(BookingStatus.CONFIRMED);
                    currentBooking.setUpdatedAt(Instant.now());
                    booking = bookingRepository.save(currentBooking);
                } else if (currentBooking != null && (currentBooking.getStatus() == BookingStatus.EXPIRED || currentBooking.getStatus() == BookingStatus.CANCELLED)) {
                    log.warn("LATE PAYMENT CONFLICT (Race condition caught): Received payment for {} booking: {}",
                            currentBooking.getStatus(), booking.getId());
                    payment.setRazorpayPaymentId(razorpayPaymentId);
                    payment.setFailureReason("LATE_PAYMENT_CONFLICT: Payment received after booking reached status " + currentBooking.getStatus());
                    payment.setUpdatedAt(Instant.now());
                    return paymentRepository.save(payment);
                }
            } else {
                booking.setStatus(BookingStatus.CONFIRMED);
                booking.setUpdatedAt(Instant.now());
            }
        } else {
            bookingStateMachine.validateTransition(booking.getStatus(), BookingStatus.CONFIRMED);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(Instant.now());
            booking = bookingRepository.save(booking);
        }

        // 6. Normal Path: Transition Payment to VERIFIED
        if (paymentStateMachine.isValidTransition(payment.getPaymentStatus(), PaymentStatus.VERIFIED)) {
            payment.setPaymentStatus(PaymentStatus.VERIFIED);
        }
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setVerifiedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Payment reconciled and Booking ID: {} successfully CONFIRMED via webhook", booking.getId());

        // 7. Automatic Ticket Issuance (idempotent & failure-isolated)
        if (ticketService != null) {
            try {
                ticketService.issueTicket(booking.getId());
                log.info("Ticket successfully issued upon payment reconciliation for booking ID: {}", booking.getId());
            } catch (Exception ex) {
                log.error("Non-fatal: Ticket issuance failed for confirmed booking ID: {}. Ticket remains retryable via admin/system.", booking.getId(), ex);
            }
        }

        // 8. Confirm any reserved physical seats
        if (seatMapService != null) {
            try {
                seatMapService.confirmSeats(booking.getId());
            } catch (Exception ex) {
                log.warn("Non-fatal: Failed to confirm physical seats for booking ID: {}", booking.getId(), ex);
            }
        }

        return savedPayment;
    }

    @Override
    @Transactional
    public Payment reconcilePaymentFailure(String razorpayOrderId, String razorpayPaymentId, String failureReason) {
        log.info("Reconciling payment failure for Razorpay Order ID: {}, Payment ID: {}, Reason: {}", razorpayOrderId, razorpayPaymentId, failureReason);

        Optional<Payment> paymentOpt = Optional.empty();
        if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
            paymentOpt = paymentRepository.findByRazorpayOrderId(razorpayOrderId);
        }
        if (paymentOpt.isEmpty() && razorpayPaymentId != null && !razorpayPaymentId.isBlank()) {
            paymentOpt = paymentRepository.findByRazorpayPaymentId(razorpayPaymentId);
        }

        if (paymentOpt.isEmpty()) {
            log.warn("Payment failure reconciliation skipped: No payment record found for Razorpay Order: {}", razorpayOrderId);
            return null;
        }

        Payment payment = paymentOpt.get();

        // If payment is already verified, do not regress terminal status
        if (payment.getPaymentStatus() == PaymentStatus.VERIFIED) {
            log.warn("Ignoring failure webhook for already VERIFIED payment ID: {}", payment.getId());
            return payment;
        }

        if (paymentStateMachine.isValidTransition(payment.getPaymentStatus(), PaymentStatus.FAILED)) {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }
        if (razorpayPaymentId != null && !razorpayPaymentId.isBlank()) {
            payment.setRazorpayPaymentId(razorpayPaymentId);
        }
        payment.setFailureReason(failureReason != null ? failureReason : "Payment failed at gateway");
        payment.setUpdatedAt(Instant.now());

        if (payment.getBookingId() != null && seatMapService != null) {
            try {
                seatMapService.releaseSeats(payment.getBookingId());
            } catch (Exception ex) {
                log.warn("Non-fatal: Failed to release physical seats on payment failure for booking ID: {}", payment.getBookingId(), ex);
            }
        }

        Payment saved = paymentRepository.save(payment);
        log.info("Payment ID: {} marked as FAILED with reason: {}", saved.getId(), saved.getFailureReason());
        return saved;
    }
}
