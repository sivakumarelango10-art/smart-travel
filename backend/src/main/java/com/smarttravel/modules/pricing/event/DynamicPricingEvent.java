package com.smarttravel.modules.pricing.event;

import com.smarttravel.modules.flight.model.CabinClass;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Real-time event payload broadcast to /topic/pricing/{flightId}
 * when seat demand, cabin occupancy, or pricing conditions update.
 */
public class DynamicPricingEvent {

    private String flightId;
    private String flightNumber;
    private CabinClass cabinClass;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private String currency = "INR";
    private double demandAdjustmentPercent;
    private double seasonalAdjustmentPercent;
    private double holidayAdjustmentPercent;
    private double occupancyRatio;
    private int availableSeats;
    private String reason;
    private Instant timestamp;

    public DynamicPricingEvent() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DynamicPricingEvent e = new DynamicPricingEvent();

        public Builder flightId(String flightId) { e.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { e.flightNumber = flightNumber; return this; }
        public Builder cabinClass(CabinClass cabinClass) { e.cabinClass = cabinClass; return this; }
        public Builder oldPrice(BigDecimal oldPrice) { e.oldPrice = oldPrice; return this; }
        public Builder newPrice(BigDecimal newPrice) { e.newPrice = newPrice; return this; }
        public Builder currency(String currency) { e.currency = currency; return this; }
        public Builder demandAdjustmentPercent(double pct) { e.demandAdjustmentPercent = pct; return this; }
        public Builder seasonalAdjustmentPercent(double pct) { e.seasonalAdjustmentPercent = pct; return this; }
        public Builder holidayAdjustmentPercent(double pct) { e.holidayAdjustmentPercent = pct; return this; }
        public Builder occupancyRatio(double ratio) { e.occupancyRatio = ratio; return this; }
        public Builder availableSeats(int seats) { e.availableSeats = seats; return this; }
        public Builder reason(String reason) { e.reason = reason; return this; }
        public Builder timestamp(Instant timestamp) { e.timestamp = timestamp; return this; }

        public DynamicPricingEvent build() {
            if (e.timestamp == null) {
                e.timestamp = Instant.now();
            }
            return e;
        }
    }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }
    public BigDecimal getOldPrice() { return oldPrice; }
    public void setOldPrice(BigDecimal oldPrice) { this.oldPrice = oldPrice; }
    public BigDecimal getNewPrice() { return newPrice; }
    public void setNewPrice(BigDecimal newPrice) { this.newPrice = newPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public double getDemandAdjustmentPercent() { return demandAdjustmentPercent; }
    public void setDemandAdjustmentPercent(double demandAdjustmentPercent) { this.demandAdjustmentPercent = demandAdjustmentPercent; }
    public double getSeasonalAdjustmentPercent() { return seasonalAdjustmentPercent; }
    public void setSeasonalAdjustmentPercent(double seasonalAdjustmentPercent) { this.seasonalAdjustmentPercent = seasonalAdjustmentPercent; }
    public double getHolidayAdjustmentPercent() { return holidayAdjustmentPercent; }
    public void setHolidayAdjustmentPercent(double holidayAdjustmentPercent) { this.holidayAdjustmentPercent = holidayAdjustmentPercent; }
    public double getOccupancyRatio() { return occupancyRatio; }
    public void setOccupancyRatio(double occupancyRatio) { this.occupancyRatio = occupancyRatio; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
