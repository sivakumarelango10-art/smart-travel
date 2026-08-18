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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RazorpayPaymentGateway razorpayGateway;

    private PaymentStateMachine paymentStateMachine;
    private BookingStateMachine bookingStateMachine;
    private PaymentMapper paymentMapper;
    private RazorpayProperties razorpayProperties;

    private PaymentService paymentService;

    private Booking testBooking;

    @BeforeEach
    void setUp() {
        paymentStateMachine = new PaymentStateMachine();
        bookingStateMachine = new BookingStateMachine();
        paymentMapper = new PaymentMapper();
        razorpayProperties = new RazorpayProperties(true, "rzp_test_key123", "rzp_test_secret456", "INR");

        paymentService = new PaymentServiceImpl(
                paymentRepository,
                bookingRepository,
                razorpayGateway,
                paymentStateMachine,
                bookingStateMachine,
                paymentMapper,
                razorpayProperties
        );

        testBooking = Booking.builder()
                .id("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-1")
                .userEmail("traveler@smarttravel.com")
                .flightId("fl-123")
                .totalAmount(new BigDecimal("17100.00"))
                .currency("INR")
                .status(BookingStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Create Payment Order: Success with exact paise calculation")
    void testCreatePaymentOrder_Success() {
        PaymentOrderCreateRequest request = PaymentOrderCreateRequest.builder()
                .bookingId("bk-100")
                .notes("Payment for flight to Mumbai")
                .build();

        RazorpayOrderDto orderDto = RazorpayOrderDto.builder()
                .id("order_rzp_123")
                .amount(1710000L)
                .currency("INR")
                .receipt("ST8K4P2Q")
                .status("created")
                .build();

        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findFirstByBookingIdAndPaymentStatusIn(eq("bk-100"), any())).thenReturn(Optional.empty());
        when(razorpayGateway.createOrder(eq("ST8K4P2Q"), eq(1710000L), eq("INR"), anyMap())).thenReturn(orderDto);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment p = invocation.getArgument(0);
            p.setId("pay-999");
            return p;
        });

        PaymentOrderResponse response = paymentService.createPaymentOrder(request, "user-1", "traveler@smarttravel.com");

        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo("pay-999");
        assertThat(response.getRazorpayOrderId()).isEqualTo("order_rzp_123");
        assertThat(response.getAmount()).isEqualTo(1710000L);
        assertThat(response.getAmountInRupees()).isEqualByComparingTo("17100.00");
        assertThat(response.getCurrency()).isEqualTo("INR");
        assertThat(response.getBookingReference()).isEqualTo("ST8K4P2Q");
        assertThat(response.getRazorpayKeyId()).isEqualTo("rzp_test_key123");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getAmountPaise()).isEqualTo(1710000L);
        assertThat(saved.getPaymentStatus()).isEqualTo(PaymentStatus.ORDER_CREATED);
        assertThat(saved.getPaymentMethod()).isEqualTo(PaymentMethod.RAZORPAY);
    }

    @Test
    @DisplayName("Create Payment Order: Idempotency returns existing active order")
    void testCreatePaymentOrder_IdempotentExistingOrder() {
        PaymentOrderCreateRequest request = PaymentOrderCreateRequest.builder().bookingId("bk-100").build();

        Payment existingActive = Payment.builder()
                .id("pay-existing")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-1")
                .razorpayOrderId("order_existing_456")
                .amount(new BigDecimal("17100.00"))
                .amountPaise(1710000L)
                .currency("INR")
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .build();

        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));
        when(paymentRepository.findFirstByBookingIdAndPaymentStatusIn(eq("bk-100"), any()))
                .thenReturn(Optional.of(existingActive));

        PaymentOrderResponse response = paymentService.createPaymentOrder(request, "user-1", "traveler@smarttravel.com");

        assertThat(response.getPaymentId()).isEqualTo("pay-existing");
        assertThat(response.getRazorpayOrderId()).isEqualTo("order_existing_456");
        verify(razorpayGateway, never()).createOrder(anyString(), anyLong(), anyString(), anyMap());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create Payment Order: Non-owner access throws ResourceNotFoundException")
    void testCreatePaymentOrder_NonOwnerAccess() {
        PaymentOrderCreateRequest request = PaymentOrderCreateRequest.builder().bookingId("bk-100").build();
        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));

        assertThatThrownBy(() -> paymentService.createPaymentOrder(request, "attacker-user", "attacker@smarttravel.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Create Payment Order: Cancelled booking throws ConflictException")
    void testCreatePaymentOrder_CancelledBooking() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        PaymentOrderCreateRequest request = PaymentOrderCreateRequest.builder().bookingId("bk-100").build();
        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));

        assertThatThrownBy(() -> paymentService.createPaymentOrder(request, "user-1", "traveler@smarttravel.com"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cancelled booking");
    }

    @Test
    @DisplayName("Verify Payment: Successful signature transitions payment to VERIFIED and booking to CONFIRMED")
    void testVerifyPayment_Success() {
        Payment pendingPayment = Payment.builder()
                .id("pay-100")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-1")
                .razorpayOrderId("order_rzp_123")
                .amount(new BigDecimal("17100.00"))
                .amountPaise(1710000L)
                .currency("INR")
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .build();

        PaymentVerificationRequest request = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_rzp_123")
                .razorpayPaymentId("pay_rzp_987")
                .razorpaySignature("valid_signature_hex")
                .build();

        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(pendingPayment));
        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));
        when(razorpayGateway.verifyPaymentSignature("order_rzp_123", "pay_rzp_987", "valid_signature_hex")).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.verifyPayment(request, "user-1", "traveler@smarttravel.com");

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
        assertThat(response.getRazorpayPaymentId()).isEqualTo("pay_rzp_987");
        assertThat(response.getVerifiedAt()).isNotNull();

        verify(bookingRepository).save(testBooking);
        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Verify Payment: Invalid signature transitions payment to FAILED and throws BadRequestException")
    void testVerifyPayment_InvalidSignature() {
        Payment pendingPayment = Payment.builder()
                .id("pay-100")
                .bookingId("bk-100")
                .userId("user-1")
                .razorpayOrderId("order_rzp_123")
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .build();

        PaymentVerificationRequest request = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_rzp_123")
                .razorpayPaymentId("pay_rzp_987")
                .razorpaySignature("invalid_signature")
                .build();

        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(pendingPayment));
        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));
        when(razorpayGateway.verifyPaymentSignature("order_rzp_123", "pay_rzp_987", "invalid_signature")).thenReturn(false);

        assertThatThrownBy(() -> paymentService.verifyPayment(request, "user-1", "traveler@smarttravel.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Payment signature verification failed");

        verify(paymentRepository).save(pendingPayment);
        assertThat(pendingPayment.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(bookingRepository, never()).save(testBooking);
    }

    @Test
    @DisplayName("Verify Payment: Idempotent call on already VERIFIED payment returns success without repeating confirmation")
    void testVerifyPayment_IdempotentAlreadyVerified() {
        Payment verifiedPayment = Payment.builder()
                .id("pay-100")
                .bookingId("bk-100")
                .userId("user-1")
                .razorpayOrderId("order_rzp_123")
                .razorpayPaymentId("pay_rzp_987")
                .paymentStatus(PaymentStatus.VERIFIED)
                .verifiedAt(Instant.now())
                .build();

        PaymentVerificationRequest request = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_rzp_123")
                .razorpayPaymentId("pay_rzp_987")
                .razorpaySignature("sig")
                .build();

        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(verifiedPayment));

        PaymentResponse response = paymentService.verifyPayment(request, "user-1", "traveler@smarttravel.com");

        assertThat(response.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
        verify(razorpayGateway, never()).verifyPaymentSignature(anyString(), anyString(), anyString());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Get Payment by ID: Admin can access any payment, User can only access own payment")
    void testGetPaymentById_AccessControl() {
        Payment payment = Payment.builder().id("pay-1").userId("user-1").build();

        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(payment));
        when(paymentRepository.findByIdAndUserId("pay-1", "user-1")).thenReturn(Optional.of(payment));

        // Admin call
        PaymentResponse adminRes = paymentService.getPaymentById("pay-1", "admin-user", true);
        assertThat(adminRes.getId()).isEqualTo("pay-1");

        // User call (owner)
        PaymentResponse userRes = paymentService.getPaymentById("pay-1", "user-1", false);
        assertThat(userRes.getId()).isEqualTo("pay-1");

        // Non-owner call throws 404
        when(paymentRepository.findByIdAndUserId("pay-1", "user-2")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paymentService.getPaymentById("pay-1", "user-2", false))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
