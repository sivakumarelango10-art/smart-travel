package com.smarttravel.modules.flight.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Embedded domain model representing the seat capacity, availability, and pricing tier for a specific cabin class.
 */
public class CabinInventory {

    private CabinClass cabinClass;
    private int totalSeats;
    private int availableSeats;
    private BigDecimal basePrice;
    private BigDecimal taxAmount;
    private BigDecimal feeAmount;
    private BigDecimal totalPrice;

    public CabinInventory() {
    }

    public CabinInventory(CabinClass cabinClass, int totalSeats, int availableSeats,
                          BigDecimal basePrice, BigDecimal taxAmount, BigDecimal feeAmount, BigDecimal totalPrice) {
        this.cabinClass = cabinClass != null ? cabinClass : CabinClass.ECONOMY;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.basePrice = basePrice != null ? basePrice : BigDecimal.ZERO;
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

        public CabinInventory build() {
            BigDecimal computedTotal = totalPrice != null ? totalPrice : basePrice.add(taxAmount).add(feeAmount);
            return new CabinInventory(cabinClass, totalSeats, availableSeats, basePrice, taxAmount, feeAmount, computedTotal);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CabinInventory that = (CabinInventory) o;
        return cabinClass == that.cabinClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cabinClass);
    }

    @Override
    public String toString() {
        return "CabinInventory{" +
                "cabinClass=" + cabinClass +
                ", totalSeats=" + totalSeats +
                ", availableSeats=" + availableSeats +
                ", basePrice=" + basePrice +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
