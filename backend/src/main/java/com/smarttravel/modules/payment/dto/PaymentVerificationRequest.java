package com.smarttravel.modules.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload containing Razorpay Checkout completion parameters for cryptographic verification.
 */
@Schema(description = "Payment Signature Verification Request Payload")
public class PaymentVerificationRequest {

    @NotBlank(message = "Razorpay Order ID is required")
    @Schema(description = "Razorpay Order ID returned during order creation", example = "order_N1234567890abc", requiredMode = Schema.RequiredMode.REQUIRED)
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay Payment ID is required")
    @Schema(description = "Razorpay Payment ID returned by Checkout JS upon payment completion", example = "pay_N9876543210xyz", requiredMode = Schema.RequiredMode.REQUIRED)
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay Signature is required")
    @Schema(description = "Cryptographic HMAC-SHA256 signature returned by Razorpay Checkout JS", example = "9efb4b6058a5e305e714652fb6a18d1844ec8eb2e67a7d483ef30b58e7f12e8b", requiredMode = Schema.RequiredMode.REQUIRED)
    private String razorpaySignature;

    public PaymentVerificationRequest() {
    }

    public PaymentVerificationRequest(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpaySignature = razorpaySignature;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;

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

        public PaymentVerificationRequest build() {
            return new PaymentVerificationRequest(razorpayOrderId, razorpayPaymentId, razorpaySignature);
        }
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
}
