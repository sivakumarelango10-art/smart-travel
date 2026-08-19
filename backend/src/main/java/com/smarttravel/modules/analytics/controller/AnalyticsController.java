package com.smarttravel.modules.analytics.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.analytics.dto.*;
import com.smarttravel.modules.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * Admin analytics REST controller.
 * All endpoints require ROLE_ADMIN — enforced both by SecurityConfig path matcher
 * and by @PreAuthorize at method level for defence-in-depth.
 * No PII is returned by any endpoint.
 */
@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Analytics", description = "Platform analytics and KPI metrics for admin dashboard")
public class AnalyticsController {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsController.class);

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Platform Overview KPIs", description = "Returns platform-wide KPI metrics for the admin dashboard.")
    public ResponseEntity<ApiResponse<OverviewAnalyticsResponse>> getOverview() {
        log.debug("Admin analytics: overview requested");
        return ResponseEntity.ok(ApiResponse.success("Overview analytics retrieved", analyticsService.getOverview()));
    }

    @GetMapping("/revenue")
    @Operation(summary = "Revenue Analytics", description = "Revenue metrics and trend data based on VERIFIED payments.")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> getRevenue(
            @RequestParam(required = false, defaultValue = "last30days") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        AnalyticsDateRangeRequest request = buildRequest(period, from, to);
        log.debug("Admin analytics: revenue [{} -> {}]", request.getFrom(), request.getTo());
        return ResponseEntity.ok(ApiResponse.success("Revenue analytics retrieved", analyticsService.getRevenueAnalytics(request)));
    }

    @GetMapping("/bookings")
    @Operation(summary = "Booking Analytics", description = "Booking counts, rates, and daily trend data.")
    public ResponseEntity<ApiResponse<BookingAnalyticsResponse>> getBookings(
            @RequestParam(required = false, defaultValue = "last30days") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        AnalyticsDateRangeRequest request = buildRequest(period, from, to);
        return ResponseEntity.ok(ApiResponse.success("Booking analytics retrieved", analyticsService.getBookingAnalytics(request)));
    }

    @GetMapping("/flights")
    @Operation(summary = "Flight Analytics", description = "Flight status distribution, occupancy, and top performers.")
    public ResponseEntity<ApiResponse<FlightAnalyticsResponse>> getFlights(
            @RequestParam(required = false, defaultValue = "last30days") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        AnalyticsDateRangeRequest request = buildRequest(period, from, to);
        return ResponseEntity.ok(ApiResponse.success("Flight analytics retrieved", analyticsService.getFlightAnalytics(request)));
    }

    @GetMapping("/seats")
    @Operation(summary = "Seat Utilization Analytics", description = "Seat occupancy by cabin class across all flights.")
    public ResponseEntity<ApiResponse<SeatAnalyticsResponse>> getSeats() {
        return ResponseEntity.ok(ApiResponse.success("Seat analytics retrieved", analyticsService.getSeatAnalytics()));
    }

    @GetMapping("/payments")
    @Operation(summary = "Payment Analytics", description = "Payment success/failure metrics and trend data.")
    public ResponseEntity<ApiResponse<PaymentAnalyticsResponse>> getPayments(
            @RequestParam(required = false, defaultValue = "last30days") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        AnalyticsDateRangeRequest request = buildRequest(period, from, to);
        return ResponseEntity.ok(ApiResponse.success("Payment analytics retrieved", analyticsService.getPaymentAnalytics(request)));
    }

    @GetMapping("/customers")
    @Operation(summary = "Customer Analytics", description = "Customer registration and activity metrics (no PII).")
    public ResponseEntity<ApiResponse<CustomerAnalyticsResponse>> getCustomers(
            @RequestParam(required = false, defaultValue = "last30days") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        AnalyticsDateRangeRequest request = buildRequest(period, from, to);
        return ResponseEntity.ok(ApiResponse.success("Customer analytics retrieved", analyticsService.getCustomerAnalytics(request)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private AnalyticsDateRangeRequest buildRequest(String period, Instant from, Instant to) {
        AnalyticsDateRangeRequest request = new AnalyticsDateRangeRequest();
        try {
            request.setPeriod(AnalyticsDateRangeRequest.Period.valueOf(period));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid period '" + period + "'. Valid values: today, yesterday, last7days, last30days, thisMonth, lastMonth, custom");
        }
        request.setFrom(from);
        request.setTo(to);
        return request;
    }
}
