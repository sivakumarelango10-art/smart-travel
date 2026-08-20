package com.smarttravel.modules.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Unified Analytics Dashboard Response containing pre-computed metrics for the entire dashboard.
 */
@Schema(description = "Unified Admin Analytics Dashboard Response")
public class AdminDashboardResponse {

    @Schema(description = "Platform Overview KPI summary")
    private OverviewAnalyticsResponse overview;

    @Schema(description = "Revenue and monetization analytics")
    private RevenueAnalyticsResponse revenue;

    @Schema(description = "Booking trends and conversion metrics")
    private BookingAnalyticsResponse bookings;

    @Schema(description = "Flight operations, statuses, and performance")
    private FlightAnalyticsResponse flights;

    @Schema(description = "Seat occupancy and cabin utilization")
    private SeatAnalyticsResponse seats;

    @Schema(description = "Payment processing and settlement analytics")
    private PaymentAnalyticsResponse payments;

    @Schema(description = "Customer acquisition and activity metrics")
    private CustomerAnalyticsResponse customers;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(OverviewAnalyticsResponse overview,
                                  RevenueAnalyticsResponse revenue,
                                  BookingAnalyticsResponse bookings,
                                  FlightAnalyticsResponse flights,
                                  SeatAnalyticsResponse seats,
                                  PaymentAnalyticsResponse payments,
                                  CustomerAnalyticsResponse customers) {
        this.overview = overview;
        this.revenue = revenue;
        this.bookings = bookings;
        this.flights = flights;
        this.seats = seats;
        this.payments = payments;
        this.customers = customers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AdminDashboardResponse r = new AdminDashboardResponse();

        public Builder overview(OverviewAnalyticsResponse overview) {
            r.setOverview(overview);
            return this;
        }

        public Builder revenue(RevenueAnalyticsResponse revenue) {
            r.setRevenue(revenue);
            return this;
        }

        public Builder bookings(BookingAnalyticsResponse bookings) {
            r.setBookings(bookings);
            return this;
        }

        public Builder flights(FlightAnalyticsResponse flights) {
            r.setFlights(flights);
            return this;
        }

        public Builder seats(SeatAnalyticsResponse seats) {
            r.setSeats(seats);
            return this;
        }

        public Builder payments(PaymentAnalyticsResponse payments) {
            r.setPayments(payments);
            return this;
        }

        public Builder customers(CustomerAnalyticsResponse customers) {
            r.setCustomers(customers);
            return this;
        }

        public AdminDashboardResponse build() {
            return r;
        }
    }

    public OverviewAnalyticsResponse getOverview() {
        return overview;
    }

    public void setOverview(OverviewAnalyticsResponse overview) {
        this.overview = overview;
    }

    public RevenueAnalyticsResponse getRevenue() {
        return revenue;
    }

    public void setRevenue(RevenueAnalyticsResponse revenue) {
        this.revenue = revenue;
    }

    public BookingAnalyticsResponse getBookings() {
        return bookings;
    }

    public void setBookings(BookingAnalyticsResponse bookings) {
        this.bookings = bookings;
    }

    public FlightAnalyticsResponse getFlights() {
        return flights;
    }

    public void setFlights(FlightAnalyticsResponse flights) {
        this.flights = flights;
    }

    public SeatAnalyticsResponse getSeats() {
        return seats;
    }

    public void setSeats(SeatAnalyticsResponse seats) {
        this.seats = seats;
    }

    public PaymentAnalyticsResponse getPayments() {
        return payments;
    }

    public void setPayments(PaymentAnalyticsResponse payments) {
        this.payments = payments;
    }

    public CustomerAnalyticsResponse getCustomers() {
        return customers;
    }

    public void setCustomers(CustomerAnalyticsResponse customers) {
        this.customers = customers;
    }
}
