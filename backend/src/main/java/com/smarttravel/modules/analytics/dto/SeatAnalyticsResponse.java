package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Seat utilization analytics: platform-wide + per-cabin breakdown.
 */
public class SeatAnalyticsResponse {

    // Platform totals
    private long totalSeats;
    private long availableSeats;
    private long bookedSeats;
    private long heldSeats;
    private BigDecimal overallOccupancyPercentage;

    // Per-cabin breakdown
    private List<CabinUtilizationDto> cabinUtilization;

    private Instant generatedAt = Instant.now();

    public SeatAnalyticsResponse() {
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final SeatAnalyticsResponse r = new SeatAnalyticsResponse();
        public Builder totalSeats(long v) { r.totalSeats = v; return this; }
        public Builder availableSeats(long v) { r.availableSeats = v; return this; }
        public Builder bookedSeats(long v) { r.bookedSeats = v; return this; }
        public Builder heldSeats(long v) { r.heldSeats = v; return this; }
        public Builder overallOccupancyPercentage(BigDecimal v) { r.overallOccupancyPercentage = v; return this; }
        public Builder cabinUtilization(List<CabinUtilizationDto> v) { r.cabinUtilization = v; return this; }
        public SeatAnalyticsResponse build() { return r; }
    }

    public long getTotalSeats() { return totalSeats; }
    public long getAvailableSeats() { return availableSeats; }
    public long getBookedSeats() { return bookedSeats; }
    public long getHeldSeats() { return heldSeats; }
    public BigDecimal getOverallOccupancyPercentage() { return overallOccupancyPercentage; }
    public List<CabinUtilizationDto> getCabinUtilization() { return cabinUtilization; }
    public Instant getGeneratedAt() { return generatedAt; }
}
