package com.smarttravel.modules.payment.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * MongoDB Document entity representing a payment transaction record.
 */
@Document(collection = "payments")
@CompoundIndexes({
        @CompoundIndex(name = "payment_booking_user_idx", def = "{'bookingId': 1, 'userId': 1}"),
        @CompoundIndex(name = "payment_user_created_idx", def = "{'userId': 1, 'createdAt': -1}")
})
public class Payment {

    @Id
    private String id;

    @Indexed
    private String bookingId;

    @Indexed
    private String bookingReference;

    @Indexed
    private String userId;

    private String userEmail;

    @Indexed(unique = true, sparse = true)
    private String razorpayOrderId;

    @Indexed(unique = true, sparse = true)
    private String razorpayPaymentId;

    private String razorpaySignature;

    private BigDecimal amount;

    private Long amountPaise;

    private String currency = "INR";

    private PaymentStatus paymentStatus = PaymentStatus.CREATED;

    private PaymentMethod paymentMethod = PaymentMethod.RAZORPAY;

    private String description;

    private String failureReason;

    private Instant verifiedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Payment() {
    }

    public Payment(String id, String bookingId, String bookingReference, String userId, String userEmail,
                   String razorpayOrderId, String razorpayPaymentId, String razorpaySignature,
                   BigDecimal amount, Long amountPaise, String currency, PaymentStatus paymentStatus,
                   PaymentMethod paymentMethod, String description, String failureReason,
                   Instant verifiedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.userEmail = userEmail;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpaySignature = razorpaySignature;
        this.amount = amount;
        this.amountPaise = amountPaise;
        this.currency = currency != null ? currency : "INR";
        this.paymentStatus = paymentStatus != null ? paymentStatus : PaymentStatus.CREATED;
        this.paymentMethod = paymentMethod != null ? paymentMethod : PaymentMethod.RAZORPAY;
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
        private String razorpaySignature;
        private BigDecimal amount;
        private Long amountPaise;
        private String currency = "INR";
        private PaymentStatus paymentStatus = PaymentStatus.CREATED;
        private PaymentMethod paymentMethod = PaymentMethod.RAZORPAY;
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

        public Builder razorpaySignature(String razorpaySignature) {
            this.razorpaySignature = razorpaySignature;
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

        public Payment build() {
            return new Payment(id, bookingId, bookingReference, userId, userEmail,
                    razorpayOrderId, razorpayPaymentId, razorpaySignature,
                    amount, amountPaise, currency, paymentStatus,
                    paymentMethod, description, failureReason,
                    verifiedAt, createdAt, updatedAt);
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

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment payment)) return false;
        return Objects.equals(id, payment.id) ||
                (razorpayOrderId != null && Objects.equals(razorpayOrderId, payment.razorpayOrderId));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, razorpayOrderId);
    }
}
