package com.smarttravel.modules.payment.dto;

import com.smarttravel.modules.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * Response payload returned to frontend to initialize Razorpay Checkout.
 */
@Schema(description = "Payment Order Response for Frontend Checkout Integration")
public class PaymentOrderResponse {

    @Schema(description = "Internal payment record ID", example = "66c1e101f1a2b3c4d5e6f901")
    private String paymentId;

    @Schema(description = "Razorpay Order ID to pass to Razorpay Checkout JS", example = "order_N1234567890abc")
    private String razorpayOrderId;

    @Schema(description = "Public Razorpay Key ID for client SDK initialization", example = "rzp_test_1DP5mmOlF5G5ag")
    private String razorpayKeyId;

    @Schema(description = "Payable amount in smallest currency unit (paise)", example = "1710000")
    private Long amount;

    @Schema(description = "Payable amount in Rupees (INR)", example = "17100.00")
    private BigDecimal amountInRupees;

    @Schema(description = "ISO Currency code", example = "INR")
    private String currency;

    @Schema(description = "Associated Booking ID", example = "66c1e101f1a2b3c4d5e6f801")
    private String bookingId;

    @Schema(description = "PNR Booking Reference", example = "ST8K4P2Q")
    private String bookingReference;

    @Schema(description = "Payment Status", example = "ORDER_CREATED")
    private PaymentStatus status;

    public PaymentOrderResponse() {
    }

    public PaymentOrderResponse(String paymentId, String razorpayOrderId, String razorpayKeyId,
                                Long amount, BigDecimal amountInRupees, String currency,
                                String bookingId, String bookingReference, PaymentStatus status) {
        this.paymentId = paymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpayKeyId = razorpayKeyId;
        this.amount = amount;
        this.amountInRupees = amountInRupees;
        this.currency = currency;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String paymentId;
        private String razorpayOrderId;
        private String razorpayKeyId;
        private Long amount;
        private BigDecimal amountInRupees;
        private String currency;
        private String bookingId;
        private String bookingReference;
        private PaymentStatus status;

        public Builder paymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder razorpayOrderId(String razorpayOrderId) {
            this.razorpayOrderId = razorpayOrderId;
            return this;
        }

        public Builder razorpayKeyId(String razorpayKeyId) {
            this.razorpayKeyId = razorpayKeyId;
            return this;
        }

        public Builder amount(Long amount) {
            this.amount = amount;
            return this;
        }

        public Builder amountInRupees(BigDecimal amountInRupees) {
            this.amountInRupees = amountInRupees;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
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

        public Builder status(PaymentStatus status) {
            this.status = status;
            return this;
        }

        public PaymentOrderResponse build() {
            return new PaymentOrderResponse(paymentId, razorpayOrderId, razorpayKeyId,
                    amount, amountInRupees, currency, bookingId, bookingReference, status);
        }
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }

    public void setRazorpayKeyId(String razorpayKeyId) {
        this.razorpayKeyId = razorpayKeyId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountInRupees() {
        return amountInRupees;
    }

    public void setAmountInRupees(BigDecimal amountInRupees) {
        this.amountInRupees = amountInRupees;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}
