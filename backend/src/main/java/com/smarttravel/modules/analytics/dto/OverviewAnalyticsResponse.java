package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Platform-wide KPI overview for the admin dashboard.
 */
public class OverviewAnalyticsResponse {

    // Booking KPIs
    private long totalBookings;
    private long confirmedBookings;
    private long pendingBookings;
    private long cancelledBookings;
    private long expiredBookings;

    // Revenue KPIs
    private BigDecimal totalGrossRevenue;
    private BigDecimal totalRefundedAmount;
    private BigDecimal totalNetRevenue;

    // Flight KPIs
    private long totalFlights;
    private long activeFlights;
    private long scheduledFlights;
    private long delayedFlights;
    private long cancelledFlights;
    private long departedFlights;

    // Seat KPIs
    private long totalSeats;
    private long availableSeats;
    private long bookedSeats;
    private long heldSeats;

    // Ticket & Check-in KPIs
    private long ticketsIssued;
    private long checkInsCompleted;

    // Customer KPIs
    private long totalCustomers;
    private long activeCustomers;

    // Payment KPIs
    private long successfulPayments;
    private long failedPayments;
    private BigDecimal paymentSuccessRate;

    private Instant generatedAt = Instant.now();

    public OverviewAnalyticsResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final OverviewAnalyticsResponse r = new OverviewAnalyticsResponse();

        public Builder totalBookings(long v) { r.totalBookings = v; return this; }
        public Builder confirmedBookings(long v) { r.confirmedBookings = v; return this; }
        public Builder pendingBookings(long v) { r.pendingBookings = v; return this; }
        public Builder cancelledBookings(long v) { r.cancelledBookings = v; return this; }
        public Builder expiredBookings(long v) { r.expiredBookings = v; return this; }
        public Builder totalGrossRevenue(BigDecimal v) { r.totalGrossRevenue = v; return this; }
        public Builder totalRefundedAmount(BigDecimal v) { r.totalRefundedAmount = v; return this; }
        public Builder totalNetRevenue(BigDecimal v) { r.totalNetRevenue = v; return this; }
        public Builder totalFlights(long v) { r.totalFlights = v; return this; }
        public Builder activeFlights(long v) { r.activeFlights = v; return this; }
        public Builder scheduledFlights(long v) { r.scheduledFlights = v; return this; }
        public Builder delayedFlights(long v) { r.delayedFlights = v; return this; }
        public Builder cancelledFlights(long v) { r.cancelledFlights = v; return this; }
        public Builder departedFlights(long v) { r.departedFlights = v; return this; }
        public Builder totalSeats(long v) { r.totalSeats = v; return this; }
        public Builder availableSeats(long v) { r.availableSeats = v; return this; }
        public Builder bookedSeats(long v) { r.bookedSeats = v; return this; }
        public Builder heldSeats(long v) { r.heldSeats = v; return this; }
        public Builder ticketsIssued(long v) { r.ticketsIssued = v; return this; }
        public Builder checkInsCompleted(long v) { r.checkInsCompleted = v; return this; }
        public Builder totalCustomers(long v) { r.totalCustomers = v; return this; }
        public Builder activeCustomers(long v) { r.activeCustomers = v; return this; }
        public Builder successfulPayments(long v) { r.successfulPayments = v; return this; }
        public Builder failedPayments(long v) { r.failedPayments = v; return this; }
        public Builder paymentSuccessRate(BigDecimal v) { r.paymentSuccessRate = v; return this; }
        public Builder generatedAt(Instant v) { r.generatedAt = v; return this; }

        public OverviewAnalyticsResponse build() { return r; }
    }

    // Getters
    public long getTotalBookings() { return totalBookings; }
    public long getConfirmedBookings() { return confirmedBookings; }
    public long getPendingBookings() { return pendingBookings; }
    public long getCancelledBookings() { return cancelledBookings; }
    public long getExpiredBookings() { return expiredBookings; }
    public BigDecimal getTotalGrossRevenue() { return totalGrossRevenue; }
    public BigDecimal getTotalRefundedAmount() { return totalRefundedAmount; }
    public BigDecimal getTotalNetRevenue() { return totalNetRevenue; }
    public long getTotalFlights() { return totalFlights; }
    public long getActiveFlights() { return activeFlights; }
    public long getScheduledFlights() { return scheduledFlights; }
    public long getDelayedFlights() { return delayedFlights; }
    public long getCancelledFlights() { return cancelledFlights; }
    public long getDepartedFlights() { return departedFlights; }
    public long getTotalSeats() { return totalSeats; }
    public long getAvailableSeats() { return availableSeats; }
    public long getBookedSeats() { return bookedSeats; }
    public long getHeldSeats() { return heldSeats; }
    public long getTicketsIssued() { return ticketsIssued; }
    public long getCheckInsCompleted() { return checkInsCompleted; }
    public long getTotalCustomers() { return totalCustomers; }
    public long getActiveCustomers() { return activeCustomers; }
    public long getSuccessfulPayments() { return successfulPayments; }
    public long getFailedPayments() { return failedPayments; }
    public BigDecimal getPaymentSuccessRate() { return paymentSuccessRate; }
    public Instant getGeneratedAt() { return generatedAt; }
}
