package com.smarttravel.modules.payment.service;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingStateMachine;
import com.smarttravel.modules.payment.model.Payment;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    private PaymentStateMachine paymentStateMachine;
    private BookingStateMachine bookingStateMachine;

    private PaymentReconciliationService reconciliationService;

    private Payment testPayment;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        paymentStateMachine = new PaymentStateMachine();
        bookingStateMachine = new BookingStateMachine();

        reconciliationService = new PaymentReconciliationServiceImpl(
                paymentRepository,
                bookingRepository,
                paymentStateMachine,
                bookingStateMachine
        );

        testPayment = Payment.builder()
                .id("pay-100")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-alice")
                .razorpayOrderId("order_rzp_123")
                .amount(new BigDecimal("5750.00"))
                .amountPaise(575000L)
                .currency("INR")
                .paymentStatus(PaymentStatus.ORDER_CREATED)
                .createdAt(Instant.now())
                .build();

        testBooking = Booking.builder()
                .id("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-alice")
                .flightId("fl-1")
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .status(BookingStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Reconcile Success: Normal PENDING booking transitions payment to VERIFIED and booking to CONFIRMED")
    void testReconcilePaymentSuccess_Success() {
        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(testPayment));
        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = reconciliationService.reconcilePaymentSuccess("order_rzp_123", "pay_rzp_999", 575000L, "INR", "payment.captured");

        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
        assertThat(result.getRazorpayPaymentId()).isEqualTo("pay_rzp_999");
        assertThat(result.getVerifiedAt()).isNotNull();

        verify(bookingRepository).save(testBooking);
        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Reconcile Success: Idempotent replay on already CONFIRMED booking and VERIFIED payment")
    void testReconcilePaymentSuccess_IdempotentAlreadyConfirmed() {
        testPayment.setPaymentStatus(PaymentStatus.VERIFIED);
        testPayment.setRazorpayPaymentId("pay_rzp_999");
        testBooking.setStatus(BookingStatus.CONFIRMED);

        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(testPayment));
        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));

        Payment result = reconciliationService.reconcilePaymentSuccess("order_rzp_123", "pay_rzp_999", 575000L, "INR", "payment.captured");

        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
        verify(paymentRepository, never()).save(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reconcile Success: Amount mismatch flags error and does NOT confirm booking")
    void testReconcilePaymentSuccess_AmountMismatch() {
        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = reconciliationService.reconcilePaymentSuccess("order_rzp_123", "pay_rzp_999", 100000L, "INR", "payment.captured");

        assertThat(result).isNotNull();
        assertThat(result.getFailureReason()).contains("Amount mismatch");
        verify(bookingRepository, never()).findById(any());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reconcile Success: Late payment on EXPIRED booking marks LATE_PAYMENT_CONFLICT without confirming booking")
    void testReconcilePaymentSuccess_LatePaymentOnExpiredBooking() {
        testBooking.setStatus(BookingStatus.EXPIRED);

        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(testPayment));
        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(testBooking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = reconciliationService.reconcilePaymentSuccess("order_rzp_123", "pay_rzp_999", 575000L, "INR", "payment.captured");

        assertThat(result).isNotNull();
        assertThat(result.getFailureReason()).contains("LATE_PAYMENT_CONFLICT");
        assertThat(result.getRazorpayPaymentId()).isEqualTo("pay_rzp_999");

        // Booking remains strictly EXPIRED and is not saved/overwritten
        assertThat(testBooking.getStatus()).isEqualTo(BookingStatus.EXPIRED);
        verify(bookingRepository, never()).save(testBooking);
    }

    @Test
    @DisplayName("Reconcile Failure: Gateway payment failure marks payment FAILED and leaves booking PENDING for retry")
    void testReconcilePaymentFailure() {
        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        Payment result = reconciliationService.reconcilePaymentFailure("order_rzp_123", "pay_rzp_fail", "Card declined by bank");

        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getFailureReason()).isEqualTo("Card declined by bank");
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reconcile Failure: Ignored if payment is already VERIFIED")
    void testReconcilePaymentFailure_IgnoredForVerifiedPayment() {
        testPayment.setPaymentStatus(PaymentStatus.VERIFIED);
        when(paymentRepository.findByRazorpayOrderId("order_rzp_123")).thenReturn(Optional.of(testPayment));

        Payment result = reconciliationService.reconcilePaymentFailure("order_rzp_123", "pay_rzp_fail", "Late failure");

        assertThat(result).isNotNull();
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.VERIFIED);
        verify(paymentRepository, never()).save(any());
    }
}
