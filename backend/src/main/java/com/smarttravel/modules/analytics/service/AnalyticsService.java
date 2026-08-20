package com.smarttravel.modules.analytics.service;

import com.smarttravel.modules.analytics.dto.*;

/**
 * Analytics service interface for admin dashboard metrics.
 * All methods compute results via MongoDB aggregation pipelines — no in-memory collection iteration.
 */
public interface AnalyticsService {

    /**
     * Platform-wide KPI overview (cached briefly for dashboard performance).
     */
    OverviewAnalyticsResponse getOverview();

    /**
     * Revenue metrics and daily trend for the given date range.
     */
    RevenueAnalyticsResponse getRevenueAnalytics(AnalyticsDateRangeRequest request);

    /**
     * Booking metrics and daily trend for the given date range.
     */
    BookingAnalyticsResponse getBookingAnalytics(AnalyticsDateRangeRequest request);

    /**
     * Flight status distribution, top performers, and operational metrics.
     */
    FlightAnalyticsResponse getFlightAnalytics(AnalyticsDateRangeRequest request);

    /**
     * Seat utilization by cabin class across all active flights.
     */
    SeatAnalyticsResponse getSeatAnalytics();

    /**
     * Payment success/failure metrics and trend for the given date range.
     */
    PaymentAnalyticsResponse getPaymentAnalytics(AnalyticsDateRangeRequest request);

    /**
     * Customer registration and activity metrics for the given date range.
     */
    CustomerAnalyticsResponse getCustomerAnalytics(AnalyticsDateRangeRequest request);

    /**
     * Unified Dashboard dataset computed concurrently and cached for sub-10ms response times.
     */
    AdminDashboardResponse getDashboard(AnalyticsDateRangeRequest request);
}
