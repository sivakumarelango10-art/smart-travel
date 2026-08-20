package com.smarttravel.modules.pricing.model;

import com.smarttravel.modules.flight.model.CabinClass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MongoDB document recording historical price snapshots for a flight cabin.
 * Used to render price trend graphs and allow users to evaluate price freeze decisions.
 */
@Document(collection = "flight_price_histories")
@CompoundIndexes({
        @CompoundIndex(name = "price_hist_flight_cabin_time_idx",
                def = "{'flightId': 1, 'cabinClass': 1, 'capturedAt': -1}"),
        @CompoundIndex(name = "price_hist_flight_time_idx",
                def = "{'flightId': 1, 'capturedAt': -1}")
})
public class FlightPriceHistory {

    @Id
    private String id;

    private String flightId;

    private String flightNumber;

    private CabinClass cabinClass;

    /** Raw cabin base price before any dynamic adjustments */
    private BigDecimal basePrice;

    /** Demand adjustment percentage applied */
    private double demandAdjustmentPercent;

    /** Seasonal adjustment percentage applied */
    private double seasonalAdjustmentPercent;

    /** Holiday adjustment percentage applied */
    private double holidayAdjustmentPercent;

    /** Total dynamic adjustment amount in INR */
    private BigDecimal dynamicAdjustmentAmount;

    /** Tax in INR (GST 12%) */
    private BigDecimal taxAmount;

    /** Fees in INR */
    private BigDecimal feeAmount;

    /** Final total price per passenger in INR */
    private BigDecimal finalPrice;

    /** Cabin occupancy at time of snapshot (0.0 - 1.0) */
    private double occupancyRatio;

    /** Human-readable reason for price level */
    private String reason;

    @CreatedDate
    private Instant capturedAt;

    public FlightPriceHistory() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final FlightPriceHistory r = new FlightPriceHistory();
        public Builder id(String v) { r.id = v; return this; }
        public Builder flightId(String v) { r.flightId = v; return this; }
        public Builder flightNumber(String v) { r.flightNumber = v; return this; }
        public Builder cabinClass(CabinClass v) { r.cabinClass = v; return this; }
        public Builder basePrice(BigDecimal v) { r.basePrice = v; return this; }
        public Builder demandAdjustmentPercent(double v) { r.demandAdjustmentPercent = v; return this; }
        public Builder seasonalAdjustmentPercent(double v) { r.seasonalAdjustmentPercent = v; return this; }
        public Builder holidayAdjustmentPercent(double v) { r.holidayAdjustmentPercent = v; return this; }
        public Builder dynamicAdjustmentAmount(BigDecimal v) { r.dynamicAdjustmentAmount = v; return this; }
        public Builder taxAmount(BigDecimal v) { r.taxAmount = v; return this; }
        public Builder feeAmount(BigDecimal v) { r.feeAmount = v; return this; }
        public Builder finalPrice(BigDecimal v) { r.finalPrice = v; return this; }
        public Builder occupancyRatio(double v) { r.occupancyRatio = v; return this; }
        public Builder reason(String v) { r.reason = v; return this; }
        public Builder capturedAt(Instant v) { r.capturedAt = v; return this; }
        public FlightPriceHistory build() { return r; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public double getDemandAdjustmentPercent() { return demandAdjustmentPercent; }
    public void setDemandAdjustmentPercent(double demandAdjustmentPercent) { this.demandAdjustmentPercent = demandAdjustmentPercent; }
    public double getSeasonalAdjustmentPercent() { return seasonalAdjustmentPercent; }
    public void setSeasonalAdjustmentPercent(double seasonalAdjustmentPercent) { this.seasonalAdjustmentPercent = seasonalAdjustmentPercent; }
    public double getHolidayAdjustmentPercent() { return holidayAdjustmentPercent; }
    public void setHolidayAdjustmentPercent(double holidayAdjustmentPercent) { this.holidayAdjustmentPercent = holidayAdjustmentPercent; }
    public BigDecimal getDynamicAdjustmentAmount() { return dynamicAdjustmentAmount; }
    public void setDynamicAdjustmentAmount(BigDecimal dynamicAdjustmentAmount) { this.dynamicAdjustmentAmount = dynamicAdjustmentAmount; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
    public BigDecimal getFeeAmount() { return feeAmount; }
    public void setFeeAmount(BigDecimal feeAmount) { this.feeAmount = feeAmount; }
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    public double getOccupancyRatio() { return occupancyRatio; }
    public void setOccupancyRatio(double occupancyRatio) { this.occupancyRatio = occupancyRatio; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCapturedAt() { return capturedAt; }
    public void setCapturedAt(Instant capturedAt) { this.capturedAt = capturedAt; }
}
