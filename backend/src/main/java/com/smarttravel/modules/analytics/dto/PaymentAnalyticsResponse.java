package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Payment analytics: success/failure metrics + trend.
 * Revenue authority: only VERIFIED payments count as successful revenue.
 */
public class PaymentAnalyticsResponse {

    // Summary
    private long totalPayments;
    private long successfulPayments;
    private long failedPayments;
    private long pendingPayments;
    private long cancelledPayments;
    private long expiredPayments;

    private BigDecimal totalSuccessfulAmount;
    private BigDecimal totalRefundedAmount;
    private BigDecimal paymentSuccessRate;

    // Trend
    private List<TrendDataPoint> trend;

    private String period;
    private Instant from;
    private Instant to;
    private Instant generatedAt = Instant.now();

    public PaymentAnalyticsResponse() {
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PaymentAnalyticsResponse r = new PaymentAnalyticsResponse();
        public Builder totalPayments(long v) { r.totalPayments = v; return this; }
        public Builder successfulPayments(long v) { r.successfulPayments = v; return this; }
        public Builder failedPayments(long v) { r.failedPayments = v; return this; }
        public Builder pendingPayments(long v) { r.pendingPayments = v; return this; }
        public Builder cancelledPayments(long v) { r.cancelledPayments = v; return this; }
        public Builder expiredPayments(long v) { r.expiredPayments = v; return this; }
        public Builder totalSuccessfulAmount(BigDecimal v) { r.totalSuccessfulAmount = v; return this; }
        public Builder totalRefundedAmount(BigDecimal v) { r.totalRefundedAmount = v; return this; }
        public Builder paymentSuccessRate(BigDecimal v) { r.paymentSuccessRate = v; return this; }
        public Builder trend(List<TrendDataPoint> v) { r.trend = v; return this; }
        public Builder period(String v) { r.period = v; return this; }
        public Builder from(Instant v) { r.from = v; return this; }
        public Builder to(Instant v) { r.to = v; return this; }
        public PaymentAnalyticsResponse build() { return r; }
    }

    public long getTotalPayments() { return totalPayments; }
    public long getSuccessfulPayments() { return successfulPayments; }
    public long getFailedPayments() { return failedPayments; }
    public long getPendingPayments() { return pendingPayments; }
    public long getCancelledPayments() { return cancelledPayments; }
    public long getExpiredPayments() { return expiredPayments; }
    public BigDecimal getTotalSuccessfulAmount() { return totalSuccessfulAmount; }
    public BigDecimal getTotalRefundedAmount() { return totalRefundedAmount; }
    public BigDecimal getPaymentSuccessRate() { return paymentSuccessRate; }
    public List<TrendDataPoint> getTrend() { return trend; }
    public String getPeriod() { return period; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public Instant getGeneratedAt() { return generatedAt; }
}
