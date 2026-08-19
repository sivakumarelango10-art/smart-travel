package com.smarttravel.modules.payment.refund.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.notification.service.NotificationService;
import com.smarttravel.modules.payment.gateway.RazorpayPaymentGateway;
import com.smarttravel.modules.payment.gateway.dto.RazorpayRefundDto;
import com.smarttravel.modules.payment.model.Payment;
import com.smarttravel.modules.payment.model.PaymentStatus;
import com.smarttravel.modules.payment.refund.dto.RefundEligibilityResponse;
import com.smarttravel.modules.payment.refund.dto.RefundProcessRequest;
import com.smarttravel.modules.payment.refund.dto.RefundResponse;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RefundEligibilityService refundEligibilityService;

    @Spy
    private RefundStateMachine refundStateMachine = new RefundStateMachine();

    @Mock
    private RazorpayPaymentGateway razorpayPaymentGateway;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RefundServiceImpl refundService;

    private Payment testPayment;
    private Booking testBooking;

    @BeforeEach
    void setUp() {
        testPayment = Payment.builder()
                .id("pay-1")
                .bookingId("book-1")
                .razorpayPaymentId("pay_rzp_123")
                .amount(BigDecimal.valueOf(5190.00))
                .amountPaise(519000L)
                .currency("INR")
                .paymentStatus(PaymentStatus.VERIFIED)
                .build();

        testBooking = Booking.builder()
                .id("book-1")
                .bookingReference("ST8K4P")
                .userId("user-1")
                .flightId("flight-1")
                .build();
    }

    @Test
    @DisplayName("Should successfully process refund through payment gateway abstraction")
    void shouldProcessRefundSuccessfully() {
        RefundEligibilityResponse eligibility = RefundEligibilityResponse.builder()
                .paymentId("pay-1")
                .bookingId("book-1")
                .eligible(true)
                .refundableAmount(BigDecimal.valueOf(5190.00))
                .refundableAmountPaise(519000L)
                .build();

        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(testPayment));
        when(bookingRepository.findById("book-1")).thenReturn(Optional.of(testBooking));
        when(refundEligibilityService.checkPaymentRefundEligibility("pay-1", RefundReason.FLIGHT_CANCELLED))
                .thenReturn(eligibility);
        when(refundRepository.save(any(Refund.class))).thenAnswer(i -> {
            Refund r = i.getArgument(0);
            if (r.getId() == null) r.setId("rfnd-1");
            return r;
        });
        when(razorpayPaymentGateway.refundPayment("pay_rzp_123", 519000L, "FLIGHT_CANCELLED"))
                .thenReturn(RazorpayRefundDto.builder().id("rfnd_gw_999").amount(519000L).status("processed").build());

        RefundResponse res = refundService.processRefund("pay-1", new RefundProcessRequest(RefundReason.FLIGHT_CANCELLED, "Flight cancelled"), "admin@smarttravel.com");

        assertThat(res).isNotNull();
        assertThat(res.getStatus()).isEqualTo(RefundStatus.COMPLETED);
        assertThat(res.getGatewayRefundId()).isEqualTo("rfnd_gw_999");
        assertThat(res.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(5190.00));

        verify(notificationService).sendNotification(any());
    }

    @Test
    @DisplayName("Should reject refund when not eligible")
    void shouldRejectIneligibleRefund() {
        RefundEligibilityResponse eligibility = RefundEligibilityResponse.builder()
                .paymentId("pay-1")
                .eligible(false)
                .reason("Payment not captured")
                .build();

        when(paymentRepository.findById("pay-1")).thenReturn(Optional.of(testPayment));
        when(bookingRepository.findById("book-1")).thenReturn(Optional.of(testBooking));
        when(refundEligibilityService.checkPaymentRefundEligibility("pay-1", RefundReason.FLIGHT_CANCELLED))
                .thenReturn(eligibility);

        assertThatThrownBy(() -> refundService.processRefund("pay-1", new RefundProcessRequest(RefundReason.FLIGHT_CANCELLED, "Flight cancelled"), "admin@smarttravel.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Payment is not eligible for refund");
    }
}
