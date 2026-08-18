package com.smarttravel.modules.payment.service;

import com.smarttravel.modules.payment.model.Payment;

/**
 * Service orchestrating authoritative payment-versus-booking reconciliation from asynchronous signals.
 */
public interface PaymentReconciliationService {

    /**
     * Reconciles a successful payment event received from the gateway webhook or verification channel.
     *
     * @param razorpayOrderId   Razorpay Order ID
     * @param razorpayPaymentId Razorpay Payment ID
     * @param amountPaise       Paid amount in smallest currency unit (paise)
     * @param currency          Currency code (e.g., INR)
     * @param eventType         Webhook event type (e.g., payment.captured, order.paid)
     * @return Updated Payment entity or null if not reconcilable
     */
    Payment reconcilePaymentSuccess(String razorpayOrderId, String razorpayPaymentId, long amountPaise, String currency, String eventType);

    /**
     * Reconciles a payment failure event received from the gateway webhook.
     *
     * @param razorpayOrderId   Razorpay Order ID
     * @param razorpayPaymentId Razorpay Payment ID (if available)
     * @param failureReason     Gateway error message or reason code
     * @return Updated Payment entity or null if not reconcilable
     */
    Payment reconcilePaymentFailure(String razorpayOrderId, String razorpayPaymentId, String failureReason);
}
