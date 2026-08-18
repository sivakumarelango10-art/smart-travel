package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Cabin Tier Inventory and Fare Details")
public class CabinInventoryDto {

    @Schema(description = "Cabin class tier", example = "ECONOMY")
    @NotNull(message = "Cabin class is required")
    private CabinClass cabinClass;

    @Schema(description = "Total seat capacity allocated for this cabin", example = "180")
    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats;

    @Schema(description = "Available remaining seats", example = "142")
    @Min(value = 0, message = "Available seats cannot be negative")
    private int availableSeats;

    @Schema(description = "Base ticket price in INR", example = "5000.00")
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than zero")
    private BigDecimal basePrice;

    @Schema(description = "Tax amount (e.g. GST)", example = "600.00")
    @DecimalMin(value = "0.0", message = "Tax amount cannot be negative")
    private BigDecimal taxAmount;

    @Schema(description = "Airport and convenience fee amount", example = "150.00")
    @DecimalMin(value = "0.0", message = "Fee amount cannot be negative")
    private BigDecimal feeAmount;

    @Schema(description = "Total per-passenger price (basePrice + taxAmount + feeAmount)", example = "5750.00")
    private BigDecimal totalPrice;

    public CabinInventoryDto() {
    }

    public CabinInventoryDto(CabinClass cabinClass, int totalSeats, int availableSeats,
                             BigDecimal basePrice, BigDecimal taxAmount, BigDecimal feeAmount, BigDecimal totalPrice) {
        this.cabinClass = cabinClass;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.basePrice = basePrice;
        this.taxAmount = taxAmount != null ? taxAmount : BigDecimal.ZERO;
        this.feeAmount = feeAmount != null ? feeAmount : BigDecimal.ZERO;
        this.totalPrice = totalPrice != null ? totalPrice : this.basePrice.add(this.taxAmount).add(this.feeAmount);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private CabinClass cabinClass = CabinClass.ECONOMY;
        private int totalSeats;
        private int availableSeats;
        private BigDecimal basePrice = BigDecimal.ZERO;
        private BigDecimal taxAmount = BigDecimal.ZERO;
        private BigDecimal feeAmount = BigDecimal.ZERO;
        private BigDecimal totalPrice;

        public Builder cabinClass(CabinClass cabinClass) {
            this.cabinClass = cabinClass;
            return this;
        }

        public Builder totalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        public Builder availableSeats(int availableSeats) {
            this.availableSeats = availableSeats;
            return this;
        }

        public Builder basePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder taxAmount(BigDecimal taxAmount) {
            this.taxAmount = taxAmount;
            return this;
        }

        public Builder feeAmount(BigDecimal feeAmount) {
            this.feeAmount = feeAmount;
            return this;
        }

        public Builder totalPrice(BigDecimal totalPrice) {
            this.totalPrice = totalPrice;
            return this;
        }

        public CabinInventoryDto build() {
            BigDecimal computedTotal = totalPrice != null ? totalPrice : basePrice.add(taxAmount).add(feeAmount);
            return new CabinInventoryDto(cabinClass, totalSeats, availableSeats, basePrice, taxAmount, feeAmount, computedTotal);
        }
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(BigDecimal feeAmount) {
        this.feeAmount = feeAmount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
