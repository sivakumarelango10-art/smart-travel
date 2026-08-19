package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Customer analytics: registration counts, activity metrics (no PII).
 * No email, phone, JWT, or personal data exposed.
 */
public class CustomerAnalyticsResponse {

    // Summary
    private long totalCustomers;
    private long activeCustomers;
    private long customersWithBookings;
    private long repeatCustomers;  // customers with > 1 confirmed booking
    private BigDecimal averageBookingsPerCustomer;

    // Period-specific
    private long newCustomersInPeriod;

    // Trend
    private List<TrendDataPoint> trend;

    private String period;
    private Instant from;
    private Instant to;
    private Instant generatedAt = Instant.now();

    public CustomerAnalyticsResponse() {
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final CustomerAnalyticsResponse r = new CustomerAnalyticsResponse();
        public Builder totalCustomers(long v) { r.totalCustomers = v; return this; }
        public Builder activeCustomers(long v) { r.activeCustomers = v; return this; }
        public Builder customersWithBookings(long v) { r.customersWithBookings = v; return this; }
        public Builder repeatCustomers(long v) { r.repeatCustomers = v; return this; }
        public Builder averageBookingsPerCustomer(BigDecimal v) { r.averageBookingsPerCustomer = v; return this; }
        public Builder newCustomersInPeriod(long v) { r.newCustomersInPeriod = v; return this; }
        public Builder trend(List<TrendDataPoint> v) { r.trend = v; return this; }
        public Builder period(String v) { r.period = v; return this; }
        public Builder from(Instant v) { r.from = v; return this; }
        public Builder to(Instant v) { r.to = v; return this; }
        public CustomerAnalyticsResponse build() { return r; }
    }

    public long getTotalCustomers() { return totalCustomers; }
    public long getActiveCustomers() { return activeCustomers; }
    public long getCustomersWithBookings() { return customersWithBookings; }
    public long getRepeatCustomers() { return repeatCustomers; }
    public BigDecimal getAverageBookingsPerCustomer() { return averageBookingsPerCustomer; }
    public long getNewCustomersInPeriod() { return newCustomersInPeriod; }
    public List<TrendDataPoint> getTrend() { return trend; }
    public String getPeriod() { return period; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public Instant getGeneratedAt() { return generatedAt; }
}
