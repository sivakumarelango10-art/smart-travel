package com.smarttravel.modules.payment.dto;

import com.smarttravel.modules.payment.model.PaymentMethod;
import com.smarttravel.modules.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Standard customer-facing payment details response payload.
 */
@Schema(description = "Payment Details Response Payload")
public class PaymentResponse {

    @Schema(description = "Payment Record ID", example = "66c1e101f1a2b3c4d5e6f901")
    private String id;

    @Schema(description = "Associated Booking ID", example = "66c1e101f1a2b3c4d5e6f801")
    private String bookingId;

    @Schema(description = "PNR Booking Reference", example = "ST8K4P2Q")
    private String bookingReference;

    @Schema(description = "User ID", example = "66c1e101f1a2b3c4d5e6f001")
    private String userId;

    @Schema(description = "User Email", example = "john.doe@smarttravel.com")
    private String userEmail;

    @Schema(description = "Razorpay Order ID", example = "order_N1234567890abc")
    private String razorpayOrderId;

    @Schema(description = "Razorpay Payment ID", example = "pay_N9876543210xyz")
    private String razorpayPaymentId;

    @Schema(description = "Payable amount in currency units (INR)", example = "17100.00")
    private BigDecimal amount;

    @Schema(description = "Payable amount in smallest currency unit (paise)", example = "1710000")
    private Long amountPaise;

    @Schema(description = "Currency", example = "INR")
    private String currency;

    @Schema(description = "Payment Status", example = "VERIFIED")
    private PaymentStatus paymentStatus;

    @Schema(description = "Payment Method", example = "RAZORPAY")
    private PaymentMethod paymentMethod;

    @Schema(description = "Payment Description / Notes", example = "Flight to Mumbai payment")
    private String description;

    @Schema(description = "Failure Reason if payment failed", example = "Signature mismatch")
    private String failureReason;

    @Schema(description = "Timestamp when payment was cryptographically verified")
    private Instant verifiedAt;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Last update timestamp")
    private Instant updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(String id, String bookingId, String bookingReference, String userId,
                           String userEmail, String razorpayOrderId, String razorpayPaymentId,
                           BigDecimal amount, Long amountPaise, String currency,
                           PaymentStatus paymentStatus, PaymentMethod paymentMethod,
                           String description, String failureReason, Instant verifiedAt,
                           Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.userEmail = userEmail;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.amount = amount;
        this.amountPaise = amountPaise;
        this.currency = currency;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.failureReason = failureReason;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String bookingId;
        private String bookingReference;
        private String userId;
        private String userEmail;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private BigDecimal amount;
        private Long amountPaise;
        private String currency;
        private PaymentStatus paymentStatus;
        private PaymentMethod paymentMethod;
        private String description;
        private String failureReason;
        private Instant verifiedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder bookingId(String bookingId) {
            this.bookingId = bookingId;
            return this;
        }

        public Builder bookingReference(String bookingReference) {
            this.bookingReference = bookingReference;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
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

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder amountPaise(Long amountPaise) {
            this.amountPaise = amountPaise;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder paymentStatus(PaymentStatus paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder verifiedAt(Instant verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public PaymentResponse build() {
            return new PaymentResponse(id, bookingId, bookingReference, userId,
                    userEmail, razorpayOrderId, razorpayPaymentId, amount,
                    amountPaise, currency, paymentStatus, paymentMethod,
                    description, failureReason, verifiedAt, createdAt, updatedAt);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getAmountPaise() {
        return amountPaise;
    }

    public void setAmountPaise(Long amountPaise) {
        this.amountPaise = amountPaise;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Instant verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
