package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Booking analytics: summary metrics + time-series trend + rate metrics.
 */
public class BookingAnalyticsResponse {

    // Summary
    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private long cancelledBookings;
    private long expiredBookings;

    // Rate metrics (0–100 percentage)
    private BigDecimal confirmationRate;
    private BigDecimal cancellationRate;
    private BigDecimal expirationRate;
    private BigDecimal averageBookingValue;

    // Trend (chart-ready)
    private List<TrendDataPoint> trend;

    private String period;
    private Instant from;
    private Instant to;
    private Instant generatedAt = Instant.now();

    public BookingAnalyticsResponse() {
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final BookingAnalyticsResponse r = new BookingAnalyticsResponse();
        public Builder totalBookings(long v) { r.totalBookings = v; return this; }
        public Builder confirmedBookings(long v) { r.confirmedBookings = v; return this; }
        public Builder pendingBookings(long v) { r.pendingBookings = v; return this; }
        public Builder cancelledBookings(long v) { r.cancelledBookings = v; return this; }
        public Builder expiredBookings(long v) { r.expiredBookings = v; return this; }
        public Builder confirmationRate(BigDecimal v) { r.confirmationRate = v; return this; }
        public Builder cancellationRate(BigDecimal v) { r.cancellationRate = v; return this; }
        public Builder expirationRate(BigDecimal v) { r.expirationRate = v; return this; }
        public Builder averageBookingValue(BigDecimal v) { r.averageBookingValue = v; return this; }
        public Builder trend(List<TrendDataPoint> v) { r.trend = v; return this; }
        public Builder period(String v) { r.period = v; return this; }
        public Builder from(Instant v) { r.from = v; return this; }
        public Builder to(Instant v) { r.to = v; return this; }
        public BookingAnalyticsResponse build() { return r; }
    }

    public long getTotalBookings() { return totalBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public long getPendingBookings() { return pendingBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public long getExpiredBookings() { return expiredBookings; }
    public BigDecimal getConfirmationRate() { return confirmationRate; }
    public BigDecimal getCancellationRate() { return cancellationRate; }
    public BigDecimal getExpirationRate() { return expirationRate; }
    public BigDecimal getAverageBookingValue() { return averageBookingValue; }
    public List<TrendDataPoint> getTrend() { return trend; }
    public String getPeriod() { return period; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public Instant getGeneratedAt() { return generatedAt; }
}
