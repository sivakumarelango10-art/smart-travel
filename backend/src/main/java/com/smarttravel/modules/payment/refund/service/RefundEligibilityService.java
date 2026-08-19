package com.smarttravel.modules.payment.refund.service;

import com.smarttravel.modules.payment.refund.dto.RefundEligibilityResponse;
import com.smarttravel.modules.payment.refund.model.RefundReason;

/**
 * Service determining customer refund eligibility, calculate refundable amounts server-side,
 * and check existing refund state.
 */
public interface RefundEligibilityService {

    /**
     * Assesses refund eligibility for a booking.
     *
     * @param bookingId Booking MongoDB ID
     * @param reason    Reason for refund evaluation
     * @return Eligibility evaluation response
     */
    RefundEligibilityResponse checkBookingRefundEligibility(String bookingId, RefundReason reason);

    /**
     * Assesses refund eligibility for a payment.
     *
     * @param paymentId Payment MongoDB ID
     * @param reason    Reason for refund evaluation
     * @return Eligibility evaluation response
     */
    RefundEligibilityResponse checkPaymentRefundEligibility(String paymentId, RefundReason reason);
}
