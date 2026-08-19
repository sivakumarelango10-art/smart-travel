package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A single data point for trend charts (daily/weekly/monthly aggregation).
 * Serializes to a flat, chart-ready JSON object.
 */
public class TrendDataPoint {

    /** ISO-8601 date string: "2026-08-19" or "2026-W33" or "2026-08" */
    private String date;

    // Booking trend fields
    private Long bookings;
    private Long confirmed;
    private Long cancelled;
    private Long expired;
    private Long pending;

    // Revenue trend fields
    private BigDecimal grossRevenue;
    private BigDecimal refunds;
    private BigDecimal netRevenue;

    // Customer trend fields
    private Long newCustomers;

    // Payment trend fields
    private Long successfulPayments;
    private Long failedPayments;

    // Period start for sorting
    private Instant periodStart;

    public TrendDataPoint() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final TrendDataPoint p = new TrendDataPoint();

        public Builder date(String v) { p.date = v; return this; }
        public Builder bookings(Long v) { p.bookings = v; return this; }
        public Builder confirmed(Long v) { p.confirmed = v; return this; }
        public Builder cancelled(Long v) { p.cancelled = v; return this; }
        public Builder expired(Long v) { p.expired = v; return this; }
        public Builder pending(Long v) { p.pending = v; return this; }
        public Builder grossRevenue(BigDecimal v) { p.grossRevenue = v; return this; }
        public Builder refunds(BigDecimal v) { p.refunds = v; return this; }
        public Builder netRevenue(BigDecimal v) { p.netRevenue = v; return this; }
        public Builder newCustomers(Long v) { p.newCustomers = v; return this; }
        public Builder successfulPayments(Long v) { p.successfulPayments = v; return this; }
        public Builder failedPayments(Long v) { p.failedPayments = v; return this; }
        public Builder periodStart(Instant v) { p.periodStart = v; return this; }
        public TrendDataPoint build() { return p; }
    }

    public String getDate() { return date; }
    public Long getBookings() { return bookings; }
    public Long getConfirmed() { return confirmed; }
    public Long getCancelled() { return cancelled; }
    public Long getExpired() { return expired; }
    public Long getPending() { return pending; }
    public BigDecimal getGrossRevenue() { return grossRevenue; }
    public BigDecimal getRefunds() { return refunds; }
    public BigDecimal getNetRevenue() { return netRevenue; }
    public Long getNewCustomers() { return newCustomers; }
    public Long getSuccessfulPayments() { return successfulPayments; }
    public Long getFailedPayments() { return failedPayments; }
    public Instant getPeriodStart() { return periodStart; }
}
