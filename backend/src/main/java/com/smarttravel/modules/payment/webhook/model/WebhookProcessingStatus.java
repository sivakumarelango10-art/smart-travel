package com.smarttravel.modules.payment.webhook.model;

/**
 * Processing status for incoming payment gateway webhook events.
 */
public enum WebhookProcessingStatus {
    RECEIVED,
    PROCESSED,
    FAILED,
    IGNORED
}
