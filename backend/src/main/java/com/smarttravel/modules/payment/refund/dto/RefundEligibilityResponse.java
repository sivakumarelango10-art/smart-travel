package com.smarttravel.modules.payment.refund.dto;

import com.smarttravel.modules.payment.refund.model.RefundReason;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Payment Refund Eligibility Assessment")
public class RefundEligibilityResponse {

    @Schema(description = "Booking MongoDB ID", example = "66c1e101f1a2b3c4d5e6f801")
    private String bookingId;

    @Schema(description = "Payment MongoDB ID", example = "66c1e101f1a2b3c4d5e6f802")
    private String paymentId;

    @Schema(description = "Whether the payment is eligible for refund", example = "true")
    private boolean eligible;

    @Schema(description = "Reason or policy rationale for eligibility / non-eligibility")
    private String reason;

    @Schema(description = "Applicable Refund Ground", example = "FLIGHT_CANCELLED")
    private RefundReason refundReason;

    @Schema(description = "Maximum eligible refundable amount in INR", example = "5190.00")
    private BigDecimal refundableAmount;

    @Schema(description = "Maximum eligible refundable amount in Paise", example = "519000")
    private long refundableAmountPaise;

    @Schema(description = "Whether a refund has already been processed or is in progress", example = "false")
    private boolean alreadyRefunded;

    @Schema(description = "Existing refund ID if already created")
    private String existingRefundId;

    @Schema(description = "Refund percentage applicable under the cancellation policy", example = "50%")
    private String refundPercentage;

    @Schema(description = "Human-readable policy tier description for audit trail")
    private String policyDescription;

    public RefundEligibilityResponse() {
    }

    public RefundEligibilityResponse(String bookingId, String paymentId, boolean eligible, String reason,
                                     RefundReason refundReason, BigDecimal refundableAmount,
                                     long refundableAmountPaise, boolean alreadyRefunded, String existingRefundId,
                                     String refundPercentage, String policyDescription) {
        this.bookingId = bookingId;
        this.paymentId = paymentId;
        this.eligible = eligible;
        this.reason = reason;
        this.refundReason = refundReason;
        this.refundableAmount = refundableAmount;
        this.refundableAmountPaise = refundableAmountPaise;
        this.alreadyRefunded = alreadyRefunded;
        this.existingRefundId = existingRefundId;
        this.refundPercentage = refundPercentage;
        this.policyDescription = policyDescription;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public boolean isEligible() { return eligible; }
    public void setEligible(boolean eligible) { this.eligible = eligible; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RefundReason getRefundReason() { return refundReason; }
    public void setRefundReason(RefundReason refundReason) { this.refundReason = refundReason; }

    public BigDecimal getRefundableAmount() { return refundableAmount; }
    public void setRefundableAmount(BigDecimal refundableAmount) { this.refundableAmount = refundableAmount; }

    public long getRefundableAmountPaise() { return refundableAmountPaise; }
    public void setRefundableAmountPaise(long refundableAmountPaise) { this.refundableAmountPaise = refundableAmountPaise; }

    public boolean isAlreadyRefunded() { return alreadyRefunded; }
    public void setAlreadyRefunded(boolean alreadyRefunded) { this.alreadyRefunded = alreadyRefunded; }

    public String getExistingRefundId() { return existingRefundId; }
    public void setExistingRefundId(String existingRefundId) { this.existingRefundId = existingRefundId; }

    public String getRefundPercentage() { return refundPercentage; }
    public void setRefundPercentage(String refundPercentage) { this.refundPercentage = refundPercentage; }

    public String getPolicyDescription() { return policyDescription; }
    public void setPolicyDescription(String policyDescription) { this.policyDescription = policyDescription; }

    public static class Builder {
        private String bookingId;
        private String paymentId;
        private boolean eligible;
        private String reason;
        private RefundReason refundReason;
        private BigDecimal refundableAmount;
        private long refundableAmountPaise;
        private boolean alreadyRefunded;
        private String existingRefundId;
        private String refundPercentage;
        private String policyDescription;

        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public Builder eligible(boolean eligible) { this.eligible = eligible; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder refundReason(RefundReason refundReason) { this.refundReason = refundReason; return this; }
        public Builder refundableAmount(BigDecimal refundableAmount) { this.refundableAmount = refundableAmount; return this; }
        public Builder refundableAmountPaise(long refundableAmountPaise) { this.refundableAmountPaise = refundableAmountPaise; return this; }
        public Builder alreadyRefunded(boolean alreadyRefunded) { this.alreadyRefunded = alreadyRefunded; return this; }
        public Builder existingRefundId(String existingRefundId) { this.existingRefundId = existingRefundId; return this; }
        public Builder refundPercentage(String refundPercentage) { this.refundPercentage = refundPercentage; return this; }
        public Builder policyDescription(String policyDescription) { this.policyDescription = policyDescription; return this; }

        public RefundEligibilityResponse build() {
            return new RefundEligibilityResponse(bookingId, paymentId, eligible, reason, refundReason,
                    refundableAmount, refundableAmountPaise, alreadyRefunded, existingRefundId,
                    refundPercentage, policyDescription);
        }
    }
}
