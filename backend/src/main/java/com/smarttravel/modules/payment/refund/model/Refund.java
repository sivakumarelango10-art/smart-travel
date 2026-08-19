package com.smarttravel.modules.payment.refund.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MongoDB Document entity representing an immutable refund transaction.
 */
@Document(collection = "refunds")
@CompoundIndexes({
        @CompoundIndex(name = "refund_payment_status_idx", def = "{'paymentId': 1, 'status': 1}"),
        @CompoundIndex(name = "refund_booking_status_idx", def = "{'bookingId': 1, 'status': 1}"),
        @CompoundIndex(name = "refund_user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
})
public class Refund {

    @Id
    private String id;

    @Indexed(unique = true)
    private String refundNumber;

    @Indexed
    private String paymentId;

    @Indexed
    private String razorpayPaymentId;

    @Indexed
    private String bookingId;

    @Indexed
    private String bookingReference;

    @Indexed
    private String userId;

    private BigDecimal amount;

    private long amountPaise;

    private String currency = "INR";

    private RefundReason reason;

    private String description;

    private RefundStatus status = RefundStatus.REQUESTED;

    @Indexed(sparse = true)
    private String gatewayRefundId;

    private String failureReason;

    private String createdBy;

    private Instant requestedAt;

    private Instant processedAt;

    private Instant completedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Refund() {
    }

    public Refund(String id, String refundNumber, String paymentId, String razorpayPaymentId,
                  String bookingId, String bookingReference, String userId,
                  BigDecimal amount, long amountPaise, String currency, RefundReason reason,
                  String description, RefundStatus status, String gatewayRefundId,
                  String failureReason, String createdBy, Instant requestedAt,
                  Instant processedAt, Instant completedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.refundNumber = refundNumber;
        this.paymentId = paymentId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.amount = amount;
        this.amountPaise = amountPaise;
        this.currency = currency != null ? currency : "INR";
        this.reason = reason;
        this.description = description;
        this.status = status != null ? status : RefundStatus.REQUESTED;
        this.gatewayRefundId = gatewayRefundId;
        this.failureReason = failureReason;
        this.createdBy = createdBy;
        this.requestedAt = requestedAt != null ? requestedAt : Instant.now();
        this.processedAt = processedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRefundNumber() { return refundNumber; }
    public void setRefundNumber(String refundNumber) { this.refundNumber = refundNumber; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(long amountPaise) { this.amountPaise = amountPaise; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public RefundReason getReason() { return reason; }
    public void setReason(RefundReason reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public String getGatewayRefundId() { return gatewayRefundId; }
    public void setGatewayRefundId(String gatewayRefundId) { this.gatewayRefundId = gatewayRefundId; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private String id;
        private String refundNumber;
        private String paymentId;
        private String razorpayPaymentId;
        private String bookingId;
        private String bookingReference;
        private String userId;
        private BigDecimal amount;
        private long amountPaise;
        private String currency = "INR";
        private RefundReason reason;
        private String description;
        private RefundStatus status = RefundStatus.REQUESTED;
        private String gatewayRefundId;
        private String failureReason;
        private String createdBy;
        private Instant requestedAt = Instant.now();
        private Instant processedAt;
        private Instant completedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder refundNumber(String refundNumber) { this.refundNumber = refundNumber; return this; }
        public Builder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public Builder razorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; return this; }
        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder bookingReference(String bookingReference) { this.bookingReference = bookingReference; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder amountPaise(long amountPaise) { this.amountPaise = amountPaise; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder reason(RefundReason reason) { this.reason = reason; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(RefundStatus status) { this.status = status; return this; }
        public Builder gatewayRefundId(String gatewayRefundId) { this.gatewayRefundId = gatewayRefundId; return this; }
        public Builder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder requestedAt(Instant requestedAt) { this.requestedAt = requestedAt; return this; }
        public Builder processedAt(Instant processedAt) { this.processedAt = processedAt; return this; }
        public Builder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Refund build() {
            return new Refund(id, refundNumber, paymentId, razorpayPaymentId, bookingId, bookingReference,
                    userId, amount, amountPaise, currency, reason, description, status,
                    gatewayRefundId, failureReason, createdBy, requestedAt, processedAt,
                    completedAt, createdAt, updatedAt);
        }
    }
}
