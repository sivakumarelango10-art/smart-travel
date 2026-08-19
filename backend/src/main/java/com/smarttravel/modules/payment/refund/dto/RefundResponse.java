package com.smarttravel.modules.payment.refund.dto;

import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Refund Record Details")
public class RefundResponse {

    @Schema(description = "Refund MongoDB ID", example = "66c1e101f1a2b3c4d5e6fb22")
    private String id;

    @Schema(description = "Unique Refund Tracking Number", example = "RF-87A91F2C54B8")
    private String refundNumber;

    @Schema(description = "Internal Payment ID", example = "66c1e101f1a2b3c4d5e6f802")
    private String paymentId;

    @Schema(description = "Razorpay Payment ID", example = "pay_8f2f7fc3b52249")
    private String razorpayPaymentId;

    @Schema(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
    private String bookingId;

    @Schema(description = "PNR Booking Reference", example = "ST8K4P2Q")
    private String bookingReference;

    @Schema(description = "Customer User ID", example = "66c1e101f1a2b3c4d5e6f901")
    private String userId;

    @Schema(description = "Refund Amount in INR", example = "5190.00")
    private BigDecimal amount;

    @Schema(description = "Refund Amount in Paise", example = "519000")
    private long amountPaise;

    @Schema(description = "Currency Code", example = "INR")
    private String currency;

    @Schema(description = "Refund Ground / Reason", example = "FLIGHT_CANCELLED")
    private RefundReason reason;

    @Schema(description = "Detailed Description / Reason context")
    private String description;

    @Schema(description = "Refund Processing Status", example = "COMPLETED")
    private RefundStatus status;

    @Schema(description = "Gateway Refund Reference ID", example = "rfnd_9b8c7d6e5f4a3b")
    private String gatewayRefundId;

    @Schema(description = "Failure Reason if failed")
    private String failureReason;

    @Schema(description = "Timestamp when refund was requested")
    private Instant requestedAt;

    @Schema(description = "Timestamp when refund completed")
    private Instant completedAt;

    public RefundResponse() {
    }

    public RefundResponse(String id, String refundNumber, String paymentId, String razorpayPaymentId,
                          String bookingId, String bookingReference, String userId,
                          BigDecimal amount, long amountPaise, String currency, RefundReason reason,
                          String description, RefundStatus status, String gatewayRefundId,
                          String failureReason, Instant requestedAt, Instant completedAt) {
        this.id = id;
        this.refundNumber = refundNumber;
        this.paymentId = paymentId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.amount = amount;
        this.amountPaise = amountPaise;
        this.currency = currency;
        this.reason = reason;
        this.description = description;
        this.status = status;
        this.gatewayRefundId = gatewayRefundId;
        this.failureReason = failureReason;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
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

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

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
        private RefundStatus status;
        private String gatewayRefundId;
        private String failureReason;
        private Instant requestedAt;
        private Instant completedAt;

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
        public Builder requestedAt(Instant requestedAt) { this.requestedAt = requestedAt; return this; }
        public Builder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }

        public RefundResponse build() {
            return new RefundResponse(id, refundNumber, paymentId, razorpayPaymentId, bookingId,
                    bookingReference, userId, amount, amountPaise, currency, reason, description,
                    status, gatewayRefundId, failureReason, requestedAt, completedAt);
        }
    }
}
