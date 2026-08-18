package com.smarttravel.modules.payment.webhook.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

/**
 * MongoDB Document entity representing an incoming payment webhook event for audit and deduplication.
 */
@Document(collection = "payment_webhook_events")
@CompoundIndexes({
        @CompoundIndex(name = "webhook_order_event_idx", def = "{'razorpayOrderId': 1, 'eventType': 1}"),
        @CompoundIndex(name = "webhook_status_received_idx", def = "{'processingStatus': 1, 'receivedAt': -1}")
})
public class PaymentWebhookEvent {

    @Id
    private String id;

    @Indexed(sparse = true)
    private String eventId;

    @Indexed
    private String eventType;

    @Indexed
    private String razorpayOrderId;

    @Indexed
    private String razorpayPaymentId;

    private String payloadHash;

    private WebhookProcessingStatus processingStatus = WebhookProcessingStatus.RECEIVED;

    private String processingError;

    @CreatedDate
    private Instant receivedAt = Instant.now();

    private Instant processedAt;

    public PaymentWebhookEvent() {
    }

    public PaymentWebhookEvent(String id, String eventId, String eventType, String razorpayOrderId,
                               String razorpayPaymentId, String payloadHash,
                               WebhookProcessingStatus processingStatus, String processingError,
                               Instant receivedAt, Instant processedAt) {
        this.id = id;
        this.eventId = eventId;
        this.eventType = eventType;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.payloadHash = payloadHash;
        this.processingStatus = processingStatus != null ? processingStatus : WebhookProcessingStatus.RECEIVED;
        this.processingError = processingError;
        this.receivedAt = receivedAt != null ? receivedAt : Instant.now();
        this.processedAt = processedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String eventId;
        private String eventType;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String payloadHash;
        private WebhookProcessingStatus processingStatus = WebhookProcessingStatus.RECEIVED;
        private String processingError;
        private Instant receivedAt = Instant.now();
        private Instant processedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder razorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
            return this;
        }

        public Builder razorpayPaymentId(String razorpayPaymentId) {
            this.razorpayPaymentId = razorpayPaymentId;
            return this;
        }

        public Builder payloadHash(String payloadHash) {
            this.payloadHash = payloadHash;
            return this;
        }

        public Builder processingStatus(WebhookProcessingStatus processingStatus) {
            this.processingStatus = processingStatus;
            return this;
        }

        public Builder processingError(String processingError) {
            this.processingError = processingError;
            return this;
        }

        public Builder receivedAt(Instant receivedAt) {
            this.receivedAt = receivedAt;
            return this;
        }

        public Builder processedAt(Instant processedAt) {
            this.processedAt = processedAt;
            return this;
        }

        public PaymentWebhookEvent build() {
            return new PaymentWebhookEvent(id, eventId, eventType, razorpayOrderId, razorpayPaymentId,
                    payloadHash, processingStatus, processingError, receivedAt, processedAt);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public void setPayloadHash(String payloadHash) {
        this.payloadHash = payloadHash;
    }

    public WebhookProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(WebhookProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getProcessingError() {
        return processingError;
    }

    public void setProcessingError(String processingError) {
        this.processingError = processingError;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentWebhookEvent that = (PaymentWebhookEvent) o;
        return Objects.equals(id, that.id) || (eventId != null && Objects.equals(eventId, that.eventId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventId);
    }
}
