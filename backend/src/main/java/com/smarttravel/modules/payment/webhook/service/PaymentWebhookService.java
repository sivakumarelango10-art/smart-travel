package com.smarttravel.modules.payment.webhook.service;

import com.smarttravel.modules.payment.webhook.model.PaymentWebhookEvent;

/**
 * Service processing incoming Razorpay webhook payloads.
 */
public interface PaymentWebhookService {

    /**
     * Authenticates signature and processes incoming Razorpay webhook payload with full idempotency.
     *
     * @param rawPayloadBytes Raw HTTP request body bytes
     * @param signatureHeader Value of the X-Razorpay-Signature HTTP header
     * @return Processed PaymentWebhookEvent audit record
     */
    PaymentWebhookEvent handleWebhook(byte[] rawPayloadBytes, String signatureHeader);
}
