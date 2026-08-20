package com.smarttravel.modules.pricing.dto;

import com.smarttravel.modules.flight.model.CabinClass;

import java.math.BigDecimal;

/**
 * Transparent pricing breakdown including all dynamic adjustments.
 * Every component is exposed so the frontend can render a clear fare breakdown card.
 */
public class DynamicPriceBreakdown {

    private String flightId;
    private CabinClass cabinClass;
    private int passengerCount;

    /** Base fare (cabin multiplier already applied by FareCalculationService) */
    private BigDecimal baseFare;

    /** Demand adjustment amount in INR */
    private BigDecimal demandAdjustment;
    private double demandAdjustmentPercent;
    private String demandReason;

    /** Seasonal adjustment amount in INR */
    private BigDecimal seasonalAdjustment;
    private double seasonalAdjustmentPercent;
    private String seasonalReason;

    /** Holiday adjustment amount in INR */
    private BigDecimal holidayAdjustment;
    private double holidayAdjustmentPercent;
    private String holidayReason;

    /** Total dynamic adjustment = demand + seasonal + holiday */
    private BigDecimal totalDynamicAdjustment;

    /** Tax (12% GST on adjusted base) */
    private BigDecimal taxes;

    /** Fixed cabin fee */
    private BigDecimal fees;

    /** Final per-passenger total */
    private BigDecimal totalPerPassenger;

    /** Final total for all passengers */
    private BigDecimal grandTotal;

    private String currency = "INR";

    /** Overall occupancy at time of calculation */
    private double occupancyRatio;

    public DynamicPriceBreakdown() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DynamicPriceBreakdown r = new DynamicPriceBreakdown();
        public Builder flightId(String v) { r.flightId = v; return this; }
        public Builder cabinClass(CabinClass v) { r.cabinClass = v; return this; }
        public Builder passengerCount(int v) { r.passengerCount = v; return this; }
        public Builder baseFare(BigDecimal v) { r.baseFare = v; return this; }
        public Builder demandAdjustment(BigDecimal v) { r.demandAdjustment = v; return this; }
        public Builder demandAdjustmentPercent(double v) { r.demandAdjustmentPercent = v; return this; }
        public Builder demandReason(String v) { r.demandReason = v; return this; }
        public Builder seasonalAdjustment(BigDecimal v) { r.seasonalAdjustment = v; return this; }
        public Builder seasonalAdjustmentPercent(double v) { r.seasonalAdjustmentPercent = v; return this; }
        public Builder seasonalReason(String v) { r.seasonalReason = v; return this; }
        public Builder holidayAdjustment(BigDecimal v) { r.holidayAdjustment = v; return this; }
        public Builder holidayAdjustmentPercent(double v) { r.holidayAdjustmentPercent = v; return this; }
        public Builder holidayReason(String v) { r.holidayReason = v; return this; }
        public Builder totalDynamicAdjustment(BigDecimal v) { r.totalDynamicAdjustment = v; return this; }
        public Builder taxes(BigDecimal v) { r.taxes = v; return this; }
        public Builder fees(BigDecimal v) { r.fees = v; return this; }
        public Builder totalPerPassenger(BigDecimal v) { r.totalPerPassenger = v; return this; }
        public Builder grandTotal(BigDecimal v) { r.grandTotal = v; return this; }
        public Builder currency(String v) { r.currency = v; return this; }
        public Builder occupancyRatio(double v) { r.occupancyRatio = v; return this; }
        public DynamicPriceBreakdown build() { return r; }
    }

    public String getFlightId() { return flightId; }
    public CabinClass getCabinClass() { return cabinClass; }
    public int getPassengerCount() { return passengerCount; }
    public BigDecimal getBaseFare() { return baseFare; }
    public BigDecimal getDemandAdjustment() { return demandAdjustment; }
    public double getDemandAdjustmentPercent() { return demandAdjustmentPercent; }
    public String getDemandReason() { return demandReason; }
    public BigDecimal getSeasonalAdjustment() { return seasonalAdjustment; }
    public double getSeasonalAdjustmentPercent() { return seasonalAdjustmentPercent; }
    public String getSeasonalReason() { return seasonalReason; }
    public BigDecimal getHolidayAdjustment() { return holidayAdjustment; }
    public double getHolidayAdjustmentPercent() { return holidayAdjustmentPercent; }
    public String getHolidayReason() { return holidayReason; }
    public BigDecimal getTotalDynamicAdjustment() { return totalDynamicAdjustment; }
    public BigDecimal getTaxes() { return taxes; }
    public BigDecimal getFees() { return fees; }
    public BigDecimal getTotalPerPassenger() { return totalPerPassenger; }
    public BigDecimal getGrandTotal() { return grandTotal; }
    public String getCurrency() { return currency; }
    public double getOccupancyRatio() { return occupancyRatio; }
}
