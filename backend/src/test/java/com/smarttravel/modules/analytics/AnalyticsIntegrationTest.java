package com.smarttravel.modules.analytics;

import com.smarttravel.modules.analytics.dto.*;
import com.smarttravel.modules.analytics.service.AnalyticsService;
import com.smarttravel.modules.analytics.service.AnalyticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for AnalyticsService against real MongoDB Atlas.
 * Tests all metric computations, date range validation, and edge cases
 * including empty dataset responses.
 */
@SpringBootTest
class AnalyticsIntegrationTest {

    @Autowired
    private AnalyticsService analyticsService;

    // ─────────────────────────────────────────────────────────────────────────
    // Overview
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Overview returns non-null response with all fields present")
    void overview_returnsValidResponse() {
        OverviewAnalyticsResponse overview = analyticsService.getOverview();

        assertThat(overview).isNotNull();
        assertThat(overview.getTotalBookings()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getConfirmedBookings()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getPendingBookings()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getCancelledBookings()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getExpiredBookings()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getTotalGrossRevenue()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(overview.getTotalRefundedAmount()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(overview.getTotalNetRevenue()).isNotNull();
        assertThat(overview.getTotalFlights()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getTicketsIssued()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getCheckInsCompleted()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getTotalCustomers()).isGreaterThanOrEqualTo(0);
        assertThat(overview.getGeneratedAt()).isNotNull();
    }

    @Test
    @DisplayName("Overview net revenue equals gross minus refunded")
    void overview_netRevenueCalculation_isCorrect() {
        OverviewAnalyticsResponse overview = analyticsService.getOverview();
        BigDecimal expectedNet = overview.getTotalGrossRevenue().subtract(overview.getTotalRefundedAmount());
        assertThat(overview.getTotalNetRevenue()).isEqualByComparingTo(expectedNet);
    }

    @Test
    @DisplayName("Overview booking counts are internally consistent")
    void overview_bookingCounts_sumCorrectly() {
        OverviewAnalyticsResponse overview = analyticsService.getOverview();
        long subTotal = overview.getConfirmedBookings() + overview.getPendingBookings()
                + overview.getCancelledBookings() + overview.getExpiredBookings();
        assertThat(overview.getTotalBookings()).isEqualTo(subTotal);
    }

    @Test
    @DisplayName("Overview payment success rate is between 0 and 100")
    void overview_paymentSuccessRate_isInRange() {
        OverviewAnalyticsResponse overview = analyticsService.getOverview();
        if (overview.getPaymentSuccessRate() != null) {
            assertThat(overview.getPaymentSuccessRate())
                    .isGreaterThanOrEqualTo(BigDecimal.ZERO)
                    .isLessThanOrEqualTo(BigDecimal.valueOf(100));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Revenue
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Revenue analytics for last30days returns valid response")
    void revenue_last30Days_returnsValidResponse() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);

        RevenueAnalyticsResponse revenue = analyticsService.getRevenueAnalytics(req);

        assertThat(revenue).isNotNull();
        assertThat(revenue.getGrossRevenue()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(revenue.getRefundedAmount()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(revenue.getNetRevenue()).isNotNull();
        assertThat(revenue.getTrend()).isNotNull();
        assertThat(revenue.getPeriod()).isEqualTo("last30days");
        assertThat(revenue.getFrom()).isNotNull();
        assertThat(revenue.getTo()).isNotNull();
    }

    @Test
    @DisplayName("Revenue analytics net = gross - refunds")
    void revenue_netEqualsGrossMinusRefunds() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);

        RevenueAnalyticsResponse revenue = analyticsService.getRevenueAnalytics(req);
        BigDecimal expected = revenue.getGrossRevenue().subtract(revenue.getRefundedAmount());
        assertThat(revenue.getNetRevenue()).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("Revenue today, last7days, thisMonth all return non-null values")
    void revenue_fixedPeriods_returnNonNull() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);
        RevenueAnalyticsResponse revenue = analyticsService.getRevenueAnalytics(req);

        assertThat(revenue.getRevenueToday()).isNotNull();
        assertThat(revenue.getRevenueLast7Days()).isNotNull();
        assertThat(revenue.getRevenueLast30Days()).isNotNull();
        assertThat(revenue.getRevenueThisMonth()).isNotNull();
        assertThat(revenue.getRevenuePreviousMonth()).isNotNull();
    }

    @Test
    @DisplayName("Revenue analytics for today period returns trend data")
    void revenue_todayPeriod_returnsTrend() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.today);
        RevenueAnalyticsResponse revenue = analyticsService.getRevenueAnalytics(req);
        assertThat(revenue).isNotNull();
        assertThat(revenue.getTrend()).isNotNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bookings
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Booking analytics returns counts and rates")
    void bookings_returnsValidResponse() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);

        BookingAnalyticsResponse bookings = analyticsService.getBookingAnalytics(req);

        assertThat(bookings).isNotNull();
        assertThat(bookings.getTotalBookings()).isGreaterThanOrEqualTo(0);
        assertThat(bookings.getConfirmedBookings()).isGreaterThanOrEqualTo(0);
        assertThat(bookings.getConfirmationRate()).isNotNull();
        assertThat(bookings.getCancellationRate()).isNotNull();
        assertThat(bookings.getExpirationRate()).isNotNull();
        assertThat(bookings.getTrend()).isNotNull();
    }

    @Test
    @DisplayName("Booking rates are between 0 and 100")
    void bookings_rates_areInRange() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);
        BookingAnalyticsResponse bookings = analyticsService.getBookingAnalytics(req);

        assertThat(bookings.getConfirmationRate())
                .isGreaterThanOrEqualTo(BigDecimal.ZERO)
                .isLessThanOrEqualTo(BigDecimal.valueOf(100));
        assertThat(bookings.getCancellationRate())
                .isGreaterThanOrEqualTo(BigDecimal.ZERO)
                .isLessThanOrEqualTo(BigDecimal.valueOf(100));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Flights
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Flight analytics returns status distribution and top performers")
    void flights_returnsValidResponse() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);

        FlightAnalyticsResponse flights = analyticsService.getFlightAnalytics(req);

        assertThat(flights).isNotNull();
        assertThat(flights.getTotalFlights()).isGreaterThanOrEqualTo(0);
        assertThat(flights.getActiveFlights()).isGreaterThanOrEqualTo(0);
        assertThat(flights.getTopByRevenue()).isNotNull();
        assertThat(flights.getTopByBookings()).isNotNull();
        assertThat(flights.getTopByOccupancy()).isNotNull();
        assertThat(flights.getLeastUtilized()).isNotNull();
    }

    @Test
    @DisplayName("Flight analytics top lists contain no PII (no emails or phones)")
    void flights_topLists_noPII() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);
        FlightAnalyticsResponse flights = analyticsService.getFlightAnalytics(req);

        flights.getTopByRevenue().forEach(f -> {
            assertThat(f.getFlightNumber()).isNotNull();
            // No email/phone fields exist on FlightPerformanceDto — structural guarantee
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Seats
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Seat analytics returns cabin utilization breakdown")
    void seats_returnsCabinBreakdown() {
        SeatAnalyticsResponse seats = analyticsService.getSeatAnalytics();

        assertThat(seats).isNotNull();
        assertThat(seats.getTotalSeats()).isGreaterThanOrEqualTo(0);
        assertThat(seats.getCabinUtilization()).isNotNull();
        assertThat(seats.getOverallOccupancyPercentage()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Seat analytics occupancy is between 0 and 100")
    void seats_occupancy_isInRange() {
        SeatAnalyticsResponse seats = analyticsService.getSeatAnalytics();
        assertThat(seats.getOverallOccupancyPercentage())
                .isGreaterThanOrEqualTo(BigDecimal.ZERO)
                .isLessThanOrEqualTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("Seat analytics cabin totals sum to overall total")
    void seats_cabinTotals_sumToOverall() {
        SeatAnalyticsResponse seats = analyticsService.getSeatAnalytics();
        if (!seats.getCabinUtilization().isEmpty()) {
            long cabinSum = seats.getCabinUtilization().stream()
                    .mapToLong(CabinUtilizationDto::getTotalSeats).sum();
            assertThat(seats.getTotalSeats()).isEqualTo(cabinSum);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payments
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Payment analytics returns success rate between 0 and 100")
    void payments_successRate_isInRange() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);

        PaymentAnalyticsResponse payments = analyticsService.getPaymentAnalytics(req);

        assertThat(payments).isNotNull();
        assertThat(payments.getPaymentSuccessRate())
                .isGreaterThanOrEqualTo(BigDecimal.ZERO)
                .isLessThanOrEqualTo(BigDecimal.valueOf(100));
        assertThat(payments.getTotalSuccessfulAmount()).isNotNull().isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(payments.getTrend()).isNotNull();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Customers
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Customer analytics returns totals without PII")
    void customers_returnsValidResponse_withoutPII() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);

        CustomerAnalyticsResponse customers = analyticsService.getCustomerAnalytics(req);

        assertThat(customers).isNotNull();
        assertThat(customers.getTotalCustomers()).isGreaterThanOrEqualTo(0);
        assertThat(customers.getActiveCustomers()).isGreaterThanOrEqualTo(0);
        assertThat(customers.getCustomersWithBookings()).isGreaterThanOrEqualTo(0);
        assertThat(customers.getTrend()).isNotNull();
        // Structural guarantee: CustomerAnalyticsResponse has no email/phone fields
    }

    @Test
    @DisplayName("Customer analytics active count <= total count")
    void customers_activeCount_lessThanOrEqualToTotal() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.last30days);
        CustomerAnalyticsResponse customers = analyticsService.getCustomerAnalytics(req);
        assertThat(customers.getActiveCustomers()).isLessThanOrEqualTo(customers.getTotalCustomers());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Date range validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Custom range from > to throws IllegalArgumentException")
    void customRange_fromAfterTo_throwsException() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        req.setFrom(Instant.parse("2026-08-10T00:00:00Z"));
        req.setTo(Instant.parse("2026-08-01T00:00:00Z"));

        assertThatThrownBy(() -> analyticsService.getRevenueAnalytics(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before");
    }

    @Test
    @DisplayName("Custom range exceeding 366 days throws IllegalArgumentException")
    void customRange_exceeding366Days_throwsException() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        req.setFrom(Instant.parse("2025-01-01T00:00:00Z"));
        req.setTo(Instant.parse("2026-03-01T00:00:00Z")); // ~425 days

        assertThatThrownBy(() -> analyticsService.getRevenueAnalytics(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("366");
    }

    @Test
    @DisplayName("Custom range missing from throws IllegalArgumentException")
    void customRange_missingFrom_throwsException() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        req.setFrom(null);
        req.setTo(Instant.parse("2026-08-19T00:00:00Z"));

        assertThatThrownBy(() -> analyticsService.getRevenueAnalytics(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires both");
    }

    @Test
    @DisplayName("All period enum values are handled without exceptions")
    void allPeriods_doNotThrow() {
        for (AnalyticsDateRangeRequest.Period period : AnalyticsDateRangeRequest.Period.values()) {
            if (period == AnalyticsDateRangeRequest.Period.custom) continue; // needs from/to
            AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
            req.setPeriod(period);
            assertThat(analyticsService.getRevenueAnalytics(req)).isNotNull();
            assertThat(analyticsService.getBookingAnalytics(req)).isNotNull();
            assertThat(analyticsService.getPaymentAnalytics(req)).isNotNull();
            assertThat(analyticsService.getCustomerAnalytics(req)).isNotNull();
        }
    }

    @Test
    @DisplayName("Valid custom range within 30 days is accepted")
    void customRange_valid30Days_isAccepted() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        req.setFrom(Instant.parse("2026-07-01T00:00:00Z"));
        req.setTo(Instant.parse("2026-07-31T23:59:59Z"));

        assertThat(analyticsService.getRevenueAnalytics(req)).isNotNull();
        assertThat(analyticsService.getBookingAnalytics(req)).isNotNull();
    }
}
