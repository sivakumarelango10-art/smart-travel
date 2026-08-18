package com.smarttravel.modules.flight.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Itemized Fare Breakdown")
public class FareBreakdownDto {

    @Schema(description = "Base airfare component", example = "5000.00")
    private BigDecimal baseFare;

    @Schema(description = "Tax component (e.g. GST)", example = "600.00")
    private BigDecimal taxes;

    @Schema(description = "Airport and convenience fees", example = "150.00")
    private BigDecimal fees;

    @Schema(description = "Total payable amount", example = "5750.00")
    private BigDecimal totalAmount;

    @Schema(description = "Currency code", example = "INR")
    private String currency = "INR";

    @Schema(description = "Number of passengers calculated for", example = "1")
    private int passengerCount = 1;

    public FareBreakdownDto() {
    }

    public FareBreakdownDto(BigDecimal baseFare, BigDecimal taxes, BigDecimal fees,
                            BigDecimal totalAmount, String currency, int passengerCount) {
        this.baseFare = baseFare != null ? baseFare : BigDecimal.ZERO;
        this.taxes = taxes != null ? taxes : BigDecimal.ZERO;
        this.fees = fees != null ? fees : BigDecimal.ZERO;
        this.totalAmount = totalAmount != null ? totalAmount : this.baseFare.add(this.taxes).add(this.fees);
        this.currency = currency != null ? currency : "INR";
        this.passengerCount = passengerCount > 0 ? passengerCount : 1;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BigDecimal baseFare = BigDecimal.ZERO;
        private BigDecimal taxes = BigDecimal.ZERO;
        private BigDecimal fees = BigDecimal.ZERO;
        private BigDecimal totalAmount;
        private String currency = "INR";
        private int passengerCount = 1;

        public Builder baseFare(BigDecimal baseFare) {
            this.baseFare = baseFare;
            return this;
        }

        public Builder taxes(BigDecimal taxes) {
            this.taxes = taxes;
            return this;
        }

        public Builder fees(BigDecimal fees) {
            this.fees = fees;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder passengerCount(int passengerCount) {
            this.passengerCount = passengerCount;
            return this;
        }

        public FareBreakdownDto build() {
            BigDecimal computed = totalAmount != null ? totalAmount : baseFare.add(taxes).add(fees);
            return new FareBreakdownDto(baseFare, taxes, fees, computed, currency, passengerCount);
        }
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }

    public void setBaseFare(BigDecimal baseFare) {
        this.baseFare = baseFare;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }
}
