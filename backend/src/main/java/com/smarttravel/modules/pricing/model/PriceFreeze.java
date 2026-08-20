package com.smarttravel.modules.pricing.model;

import com.smarttravel.modules.flight.model.CabinClass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * MongoDB document representing a locked price for a flight cabin.
 * Allows users to freeze a fare for a configurable duration before committing to booking.
 */
@Document(collection = "price_freezes")
@CompoundIndexes({
        @CompoundIndex(name = "freeze_user_flight_idx", def = "{'userId': 1, 'flightId': 1, 'status': 1}"),
        @CompoundIndex(name = "freeze_expires_status_idx", def = "{'expiresAt': 1, 'status': 1}")
})
public class PriceFreeze {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String flightId;

    private String flightNumber;

    private CabinClass cabinClass;

    private int passengerCount;

    /** Price locked per passenger in INR */
    private BigDecimal lockedPricePerPassenger;

    /** Total locked price (lockedPricePerPassenger * passengerCount) */
    private BigDecimal lockedTotalPrice;

    private String currency = "INR";

    private PriceFreezeStatus status = PriceFreezeStatus.ACTIVE;

    /** Booking ID if freeze was used to complete a booking */
    private String bookingId;

    @CreatedDate
    private Instant createdAt;

    private Instant expiresAt;

    @LastModifiedDate
    private Instant updatedAt;

    /** Price breakdown at time of freeze */
    private BigDecimal basePriceAtFreeze;
    private double demandAdjustmentPercentAtFreeze;
    private double holidayAdjustmentPercentAtFreeze;
    private double seasonalAdjustmentPercentAtFreeze;

    public PriceFreeze() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PriceFreeze r = new PriceFreeze();
        public Builder id(String v) { r.id = v; return this; }
        public Builder userId(String v) { r.userId = v; return this; }
        public Builder flightId(String v) { r.flightId = v; return this; }
        public Builder flightNumber(String v) { r.flightNumber = v; return this; }
        public Builder cabinClass(CabinClass v) { r.cabinClass = v; return this; }
        public Builder passengerCount(int v) { r.passengerCount = v; return this; }
        public Builder lockedPricePerPassenger(BigDecimal v) { r.lockedPricePerPassenger = v; return this; }
        public Builder lockedTotalPrice(BigDecimal v) { r.lockedTotalPrice = v; return this; }
        public Builder currency(String v) { r.currency = v; return this; }
        public Builder status(PriceFreezeStatus v) { r.status = v; return this; }
        public Builder bookingId(String v) { r.bookingId = v; return this; }
        public Builder createdAt(Instant v) { r.createdAt = v; return this; }
        public Builder expiresAt(Instant v) { r.expiresAt = v; return this; }
        public Builder basePriceAtFreeze(BigDecimal v) { r.basePriceAtFreeze = v; return this; }
        public Builder demandAdjustmentPercentAtFreeze(double v) { r.demandAdjustmentPercentAtFreeze = v; return this; }
        public Builder holidayAdjustmentPercentAtFreeze(double v) { r.holidayAdjustmentPercentAtFreeze = v; return this; }
        public Builder seasonalAdjustmentPercentAtFreeze(double v) { r.seasonalAdjustmentPercentAtFreeze = v; return this; }
        public PriceFreeze build() { return r; }
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }
    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }
    public BigDecimal getLockedPricePerPassenger() { return lockedPricePerPassenger; }
    public void setLockedPricePerPassenger(BigDecimal lockedPricePerPassenger) { this.lockedPricePerPassenger = lockedPricePerPassenger; }
    public BigDecimal getLockedTotalPrice() { return lockedTotalPrice; }
    public void setLockedTotalPrice(BigDecimal lockedTotalPrice) { this.lockedTotalPrice = lockedTotalPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public PriceFreezeStatus getStatus() { return status; }
    public void setStatus(PriceFreezeStatus status) { this.status = status; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public BigDecimal getBasePriceAtFreeze() { return basePriceAtFreeze; }
    public void setBasePriceAtFreeze(BigDecimal basePriceAtFreeze) { this.basePriceAtFreeze = basePriceAtFreeze; }
    public double getDemandAdjustmentPercentAtFreeze() { return demandAdjustmentPercentAtFreeze; }
    public void setDemandAdjustmentPercentAtFreeze(double demandAdjustmentPercentAtFreeze) { this.demandAdjustmentPercentAtFreeze = demandAdjustmentPercentAtFreeze; }
    public double getHolidayAdjustmentPercentAtFreeze() { return holidayAdjustmentPercentAtFreeze; }
    public void setHolidayAdjustmentPercentAtFreeze(double holidayAdjustmentPercentAtFreeze) { this.holidayAdjustmentPercentAtFreeze = holidayAdjustmentPercentAtFreeze; }
    public double getSeasonalAdjustmentPercentAtFreeze() { return seasonalAdjustmentPercentAtFreeze; }
    public void setSeasonalAdjustmentPercentAtFreeze(double seasonalAdjustmentPercentAtFreeze) { this.seasonalAdjustmentPercentAtFreeze = seasonalAdjustmentPercentAtFreeze; }
}
