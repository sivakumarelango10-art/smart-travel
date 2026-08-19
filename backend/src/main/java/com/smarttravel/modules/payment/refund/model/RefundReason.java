package com.smarttravel.modules.payment.refund.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Business grounds for issuing a payment refund.
 */
@Schema(description = "Payment Refund Reason")
public enum RefundReason {
    FLIGHT_CANCELLED,
    MAJOR_RESCHEDULE,
    CUSTOMER_CANCELLATION,
    OVERBOOKING,
    OTHER
}
