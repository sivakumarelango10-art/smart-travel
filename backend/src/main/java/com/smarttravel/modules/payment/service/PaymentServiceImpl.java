package com.smarttravel.modules.payment.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingStateMachine;
import com.smarttravel.modules.payment.config.RazorpayProperties;
import com.smarttravel.modules.payment.dto.PaymentOrderCreateRequest;
import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.dto.PaymentResponse;
import com.smarttravel.modules.payment.dto.PaymentVerificationRequest;
import com.smarttravel.modules.payment.gateway.RazorpayPaymentGateway;
import com.smarttravel.modules.payment.gateway.dto.RazorpayOrderDto;
import com.smarttravel.modules.payment.mapper.PaymentMapper;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentMethod;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Production implementation of PaymentService providing idempotent order generation,
 * cryptographic HMAC signature verification, and booking lifecycle synchronization.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final RazorpayPaymentGateway razorpayGateway;
    private final PaymentStateMachine paymentStateMachine;
    private final BookingStateMachine bookingStateMachine;
    private final PaymentMapper paymentMapper;
    private final RazorpayProperties razorpayProperties;
    private final com.smarttravel.modules.ticket.service.TicketService ticketService;
    private final com.smarttravel.modules.flight.service.SeatMapService seatMapService;

    @org.springframework.beans.factory.annotation.Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository,
                              RazorpayPaymentGateway razorpayGateway,
                              PaymentStateMachine paymentStateMachine,
                              BookingStateMachine bookingStateMachine,
                              PaymentMapper paymentMapper,
                              RazorpayProperties razorpayProperties,
                              @org.springframework.context.annotation.Lazy @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.ticket.service.TicketService ticketService,
                              @org.springframework.context.annotation.Lazy @org.springframework.beans.factory.annotation.Autowired(required = false) com.smarttravel.modules.flight.service.SeatMapService seatMapService) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.razorpayGateway = razorpayGateway;
        this.paymentStateMachine = paymentStateMachine;
        this.bookingStateMachine = bookingStateMachine;
        this.paymentMapper = paymentMapper;
        this.razorpayProperties = razorpayProperties;
        this.ticketService = ticketService;
        this.seatMapService = seatMapService;
    }

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository,
                              RazorpayPaymentGateway razorpayGateway,
                              PaymentStateMachine paymentStateMachine,
                              BookingStateMachine bookingStateMachine,
                              PaymentMapper paymentMapper,
                              RazorpayProperties razorpayProperties) {
        this(paymentRepository, bookingRepository, razorpayGateway, paymentStateMachine, bookingStateMachine, paymentMapper, razorpayProperties, null, null);
    }

    @Override
    public PaymentOrderResponse createPaymentOrder(PaymentOrderCreateRequest request, String userId, String userEmail) {
        String bookingId = request.getBookingId();
        log.info("Initiating payment order creation for booking ID: {} (user: {})", bookingId, userId);

        // 1. Fetch booking and enforce ownership (support lookup by either MongoDB ID or PNR Reference)
        Booking booking = bookingRepository.findById(bookingId)
                .or(() -> bookingRepository.findByBookingReference(bookingId.toUpperCase()))
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (!booking.getUserId().equals(userId)) {
            log.warn("Unauthorized attempt to create payment for booking ID: {} by user: {}", bookingId, userId);
            throw new ResourceNotFoundException("Booking", "id", bookingId);
        }

        // 2. Validate booking state
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Cannot create payment for a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new ConflictException("Cannot create payment for an expired booking");
        }

        // 3. Idempotency Check: Return existing active order if already created and pending
        Optional<Payment> existingOrder = paymentRepository.findFirstByBookingIdAndPaymentStatusIn(
                booking.getId(), List.of(PaymentStatus.ORDER_CREATED, PaymentStatus.PENDING));
        if (existingOrder.isPresent()) {
            Payment activePayment = existingOrder.get();
            log.info("Returning existing active payment order: {} for booking ID: {}", activePayment.getRazorpayOrderId(), bookingId);
            return paymentMapper.toOrderResponse(activePayment, razorpayProperties.getKeyId());
        }

        // Check if already paid and verified
        Optional<Payment> verifiedPayment = paymentRepository.findFirstByBookingIdAndPaymentStatusIn(
                booking.getId(), List.of(PaymentStatus.VERIFIED));
        if (verifiedPayment.isPresent()) {
            throw new ConflictException("Booking is already confirmed and paid");
        }

        // 4. Calculate authoritative amount in paise
        BigDecimal totalAmount = booking.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Booking total amount must be greater than zero");
        }

        long amountInPaise = totalAmount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.UNNECESSARY)
                .longValueExact();

        // 5. Call Razorpay Gateway to create order
        Map<String, String> notes = new HashMap<>();
        notes.put("bookingId", booking.getId());
        notes.put("bookingReference", booking.getBookingReference());
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            notes.put("userNotes", request.getNotes());
        }

        RazorpayOrderDto orderDto = razorpayGateway.createOrder(
                booking.getBookingReference(),
                amountInPaise,
                booking.getCurrency(),
                notes
        );

        // 6. Persist Payment document
        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .bookingReference(booking.getBookingReference())
                .userId(userId)
                .userEmail(userEmail)
                .razorpayOrderId(orderDto.getId())
                .amount(totalAmount)
                .amountPaise(amountInPaise)
                .currency(booking.getCurrency())
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .paymentMethod(PaymentMethod.RAZORPAY)
                .description(request.getNotes())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment record created with ID: {} and Razorpay Order ID: {}", savedPayment.getId(), savedPayment.getRazorpayOrderId());

        return paymentMapper.toOrderResponse(savedPayment, razorpayProperties.getKeyId());
    }

    @Override
    public PaymentResponse verifyPayment(PaymentVerificationRequest request, String userId, String userEmail) {
        String orderId = request.getRazorpayOrderId();
        log.info("Initiating payment signature verification for Razorpay Order ID: {} (user: {})", orderId, userId);

        // 1. Fetch payment by Razorpay Order ID and verify ownership
        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "razorpayOrderId", orderId));

        if (!payment.getUserId().equals(userId)) {
            log.warn("Unauthorized attempt to verify payment order: {} by user: {}", orderId, userId);
            throw new ResourceNotFoundException("Payment", "razorpayOrderId", orderId);
        }

        // 2. Idempotency check: If already verified, return existing state
        if (payment.getPaymentStatus() == PaymentStatus.VERIFIED) {
            log.info("Payment order: {} is already VERIFIED. Returning idempotent response.", orderId);
            return paymentMapper.toResponse(payment);
        }

        // 3. Validate booking existence and state
        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", payment.getBookingId()));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Cannot verify payment for a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.EXPIRED) {
            throw new ConflictException("Cannot verify payment for an expired booking");
        }

        // 4. Perform cryptographic HMAC-SHA256 signature verification
        boolean isValidSignature = razorpayGateway.verifyPaymentSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValidSignature) {
            log.warn("Invalid payment signature received for order ID: {}", orderId);
            payment.setPaymentStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Invalid signature verification");
            payment.setUpdatedAt(Instant.now());
            paymentRepository.save(payment);
            throw new BadRequestException("Payment signature verification failed");
        }

        // 5. Transition payment status to VERIFIED
        paymentStateMachine.validateTransition(payment.getPaymentStatus(), PaymentStatus.VERIFIED);
        payment.setPaymentStatus(PaymentStatus.VERIFIED);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setVerifiedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        Payment savedPayment = paymentRepository.save(payment);

        // 6. Transition booking to CONFIRMED if pending
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            bookingStateMachine.validateTransition(booking.getStatus(), BookingStatus.CONFIRMED);
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setUpdatedAt(Instant.now());
            bookingRepository.save(booking);
            log.info("Booking ID: {} confirmed following successful payment verification", booking.getId());
        }

        // 7. Auto-issue E-Ticket and confirm seats upon payment verification
        if (ticketService != null) {
            try {
                ticketService.issueTicket(booking.getId());
            } catch (Exception ex) {
                log.warn("Non-fatal: Ticket issuance error during payment verification for booking ID: {}", booking.getId(), ex);
            }
        }
        if (seatMapService != null) {
            try {
                seatMapService.confirmSeats(booking.getId());
            } catch (Exception ex) {
                log.warn("Non-fatal: Error confirming seats during payment verification for booking ID: {}", booking.getId(), ex);
            }
        }

        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentById(String paymentId, String userId, boolean isAdmin) {
        log.debug("Fetching payment by ID: {} (user: {}, isAdmin: {})", paymentId, userId, isAdmin);
        Payment payment;
        if (isAdmin) {
            payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        } else {
            payment = paymentRepository.findByIdAndUserId(paymentId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        }
        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByBookingId(String bookingId, String userId, boolean isAdmin) {
        log.debug("Fetching payment by booking ID: {} (user: {}, isAdmin: {})", bookingId, userId, isAdmin);
        Payment payment;
        if (isAdmin) {
            payment = paymentRepository.findByBookingId(bookingId).stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "bookingId", bookingId));
        } else {
            payment = paymentRepository.findByBookingIdAndUserId(bookingId, userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment", "bookingId", bookingId));
        }
        return paymentMapper.toResponse(payment);
    }
}
