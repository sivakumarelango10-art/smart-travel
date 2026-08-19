package com.smarttravel.modules.payment.refund.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundEligibilityServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RefundRepository refundRepository;

    @InjectMocks
    private RefundEligibilityServiceImpl eligibilityService;

    private Payment testPayment;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testPayment = Payment.builder()
                .id("pay-101")
                .bookingId("book-101")
                .amount(BigDecimal.valueOf(5190.00))
                .amountPaise(519000L)
                .currency("INR")
                .paymentStatus(PaymentStatus.VERIFIED)
                .build();

        testBooking = Booking.builder()
                .id("book-101")
                .userId("user-101")
                .build();
    }

    @Test
    @DisplayName("Should confirm eligibility for captured payment")
    void shouldConfirmEligibilityForVerifiedPayment() {
        when(paymentRepository.findById("pay-101")).thenReturn(Optional.of(testPayment));
        when(bookingRepository.findById("book-101")).thenReturn(Optional.of(testBooking));
        when(refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc("pay-101")).thenReturn(Optional.empty());

        RefundEligibilityResponse res = eligibilityService.checkPaymentRefundEligibility("pay-101", RefundReason.FLIGHT_CANCELLED);

        assertThat(res.isEligible()).isTrue();
        assertThat(res.getRefundableAmount()).isEqualByComparingTo(BigDecimal.valueOf(5190.00));
        assertThat(res.getRefundableAmountPaise()).isEqualTo(519000L);
        assertThat(res.isAlreadyRefunded()).isFalse();
    }

    @Test
    @DisplayName("Should reject eligibility when payment is not in VERIFIED status")
    void shouldRejectEligibilityForUnverifiedPayment() {
        testPayment.setPaymentStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById("pay-101")).thenReturn(Optional.of(testPayment));
        when(bookingRepository.findById("book-101")).thenReturn(Optional.of(testBooking));
        when(refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc("pay-101")).thenReturn(Optional.empty());

        RefundEligibilityResponse res = eligibilityService.checkPaymentRefundEligibility("pay-101", RefundReason.FLIGHT_CANCELLED);

        assertThat(res.isEligible()).isFalse();
        assertThat(res.getReason()).contains("Only captured/verified payments are eligible");
    }

    @Test
    @DisplayName("Should detect already completed refund and reject duplicate")
    void shouldDetectAlreadyRefundedPayment() {
        Refund existing = Refund.builder()
                .id("rfnd-existing-1")
                .status(RefundStatus.COMPLETED)
                .reason(RefundReason.FLIGHT_CANCELLED)
                .build();

        when(paymentRepository.findById("pay-101")).thenReturn(Optional.of(testPayment));
        when(bookingRepository.findById("book-101")).thenReturn(Optional.of(testBooking));
        when(refundRepository.findFirstByPaymentIdOrderByCreatedAtDesc("pay-101")).thenReturn(Optional.of(existing));

        RefundEligibilityResponse res = eligibilityService.checkPaymentRefundEligibility("pay-101", RefundReason.FLIGHT_CANCELLED);

        assertThat(res.isEligible()).isFalse();
        assertThat(res.isAlreadyRefunded()).isTrue();
        assertThat(res.getExistingRefundId()).isEqualTo("rfnd-existing-1");
    }
}
