package com.smarttravel.modules.payment.mapper;

import com.smarttravel.modules.payment.dto.PaymentOrderResponse;
import com.smarttravel.modules.payment.dto.PaymentResponse;
import com.smarttravel.modules.payment.model.Payment;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Payment entities and DTOs.
 */
@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBookingId())
                .bookingReference(payment.getBookingReference())
                .userId(payment.getUserId())
                .userEmail(payment.getUserEmail())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .amountPaise(payment.getAmountPaise())
                .currency(payment.getCurrency())
                .paymentStatus(payment.getPaymentStatus())
                .paymentMethod(payment.getPaymentMethod())
                .description(payment.getDescription())
                .failureReason(payment.getFailureReason())
                .verifiedAt(payment.getVerifiedAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public PaymentOrderResponse toOrderResponse(Payment payment, String razorpayKeyId) {
        if (payment == null) {
            return null;
        }

        return PaymentOrderResponse.builder()
                .paymentId(payment.getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayKeyId(razorpayKeyId)
                .amount(payment.getAmountPaise())
                .amountInRupees(payment.getAmount())
                .currency(payment.getCurrency())
                .bookingId(payment.getBookingId())
                .bookingReference(payment.getBookingReference())
                .status(payment.getPaymentStatus())
                .build();
    }
}
