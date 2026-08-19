package com.smarttravel.modules.payment.refund.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lifecycle state of a payment refund transaction.
 */
@Schema(description = "Payment Refund Processing Status")
public enum RefundStatus {
    REQUESTED,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}
