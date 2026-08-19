package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Revenue analytics: summary metrics + time-series trend data.
 */
public class RevenueAnalyticsResponse {

    // Summary
    private BigDecimal grossRevenue;
    private BigDecimal refundedAmount;
    private BigDecimal netRevenue;
    private long successfulPaymentCount;
    private BigDecimal averageOrderValue;

    // Period comparisons
    private BigDecimal revenueToday;
    private BigDecimal revenueLast7Days;
    private BigDecimal revenueLast30Days;
    private BigDecimal revenueThisMonth;
    private BigDecimal revenuePreviousMonth;

    // Trend (chart-ready)
    private List<TrendDataPoint> trend;

    private String period;
    private Instant from;
    private Instant to;
    private Instant generatedAt = Instant.now();

    public RevenueAnalyticsResponse() {
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RevenueAnalyticsResponse r = new RevenueAnalyticsResponse();
        public Builder grossRevenue(BigDecimal v) { r.grossRevenue = v; return this; }
        public Builder refundedAmount(BigDecimal v) { r.refundedAmount = v; return this; }
        public Builder netRevenue(BigDecimal v) { r.netRevenue = v; return this; }
        public Builder successfulPaymentCount(long v) { r.successfulPaymentCount = v; return this; }
        public Builder averageOrderValue(BigDecimal v) { r.averageOrderValue = v; return this; }
        public Builder revenueToday(BigDecimal v) { r.revenueToday = v; return this; }
        public Builder revenueLast7Days(BigDecimal v) { r.revenueLast7Days = v; return this; }
        public Builder revenueLast30Days(BigDecimal v) { r.revenueLast30Days = v; return this; }
        public Builder revenueThisMonth(BigDecimal v) { r.revenueThisMonth = v; return this; }
        public Builder revenuePreviousMonth(BigDecimal v) { r.revenuePreviousMonth = v; return this; }
        public Builder trend(List<TrendDataPoint> v) { r.trend = v; return this; }
        public Builder period(String v) { r.period = v; return this; }
        public Builder from(Instant v) { r.from = v; return this; }
        public Builder to(Instant v) { r.to = v; return this; }
        public RevenueAnalyticsResponse build() { return r; }
    }

    public BigDecimal getGrossRevenue() { return grossRevenue; }
    public BigDecimal getRefundedAmount() { return refundedAmount; }
    public BigDecimal getNetRevenue() { return netRevenue; }
    public long getSuccessfulPaymentCount() { return successfulPaymentCount; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public BigDecimal getRevenueToday() { return revenueToday; }
    public BigDecimal getRevenueLast7Days() { return revenueLast7Days; }
    public BigDecimal getRevenueLast30Days() { return revenueLast30Days; }
    public BigDecimal getRevenueThisMonth() { return revenueThisMonth; }
    public BigDecimal getRevenuePreviousMonth() { return revenuePreviousMonth; }
    public List<TrendDataPoint> getTrend() { return trend; }
    public String getPeriod() { return period; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public Instant getGeneratedAt() { return generatedAt; }
}
