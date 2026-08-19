package com.smarttravel.modules.payment.gateway.dto;

import java.time.Instant;

/**
 * Data Transfer Object representing Razorpay refund response payload.
 */
public class RazorpayRefundDto {

    private String id;
    private String paymentId;
    private long amount;
    private String currency;
    private String status;
    private String receipt;
    private Instant createdAt;

    public RazorpayRefundDto() {
    }

    public RazorpayRefundDto(String id, String paymentId, long amount, String currency, String status, String receipt, Instant createdAt) {
        this.id = id;
        this.paymentId = paymentId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.receipt = receipt;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReceipt() { return receipt; }
    public void setReceipt(String receipt) { this.receipt = receipt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class Builder {
        private String id;
        private String paymentId;
        private long amount;
        private String currency;
        private String status;
        private String receipt;
        private Instant createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder paymentId(String paymentId) { this.paymentId = paymentId; return this; }
        public Builder amount(long amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder receipt(String receipt) { this.receipt = receipt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public RazorpayRefundDto build() {
            return new RazorpayRefundDto(id, paymentId, amount, currency, status, receipt, createdAt);
        }
    }
}
