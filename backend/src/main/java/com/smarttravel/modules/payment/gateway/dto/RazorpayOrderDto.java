package com.smarttravel.modules.payment.gateway.dto;

/**
 * Data Transfer Object representing a created order on the Razorpay gateway.
 */
public class RazorpayOrderDto {

    private String id;
    private Long amount;
    private String currency;
    private String receipt;
    private String status;

    public RazorpayOrderDto() {
    }

    public RazorpayOrderDto(String id, Long amount, String currency, String receipt, String status) {
        this.id = id;
        this.amount = amount;
        this.currency = currency;
        this.receipt = receipt;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private Long amount;
        private String currency;
        private String receipt;
        private String status;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder amount(Long amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder receipt(String receipt) {
            this.receipt = receipt;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public RazorpayOrderDto build() {
            return new RazorpayOrderDto(id, amount, currency, receipt, status);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReceipt() {
        return receipt;
    }

    public void setReceipt(String receipt) {
        this.receipt = receipt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
