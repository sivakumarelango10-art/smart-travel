package com.smarttravel.modules.payment.model;

/**
 * Status lifecycle enum for payment transactions.
 */
public enum PaymentStatus {
    CREATED,
    ORDER_CREATED,
    PENDING,
    VERIFIED,
    FAILED,
    CANCELLED,
    EXPIRED
}
