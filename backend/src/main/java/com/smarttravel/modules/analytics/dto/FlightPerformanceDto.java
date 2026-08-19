package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;

/**
 * Performance metrics for a single flight (no PII exposed).
 */
public class FlightPerformanceDto {

    private String flightId;
    private String flightNumber;
    private String airline;
    private String origin;
    private String destination;
    private String departureTime;
    private long bookingCount;
    private BigDecimal revenue;
    private int totalSeats;
    private int bookedSeats;
    private BigDecimal occupancyPercentage;

    public FlightPerformanceDto() {
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final FlightPerformanceDto d = new FlightPerformanceDto();
        public Builder flightId(String v) { d.flightId = v; return this; }
        public Builder flightNumber(String v) { d.flightNumber = v; return this; }
        public Builder airline(String v) { d.airline = v; return this; }
        public Builder origin(String v) { d.origin = v; return this; }
        public Builder destination(String v) { d.destination = v; return this; }
        public Builder departureTime(String v) { d.departureTime = v; return this; }
        public Builder bookingCount(long v) { d.bookingCount = v; return this; }
        public Builder revenue(BigDecimal v) { d.revenue = v; return this; }
        public Builder totalSeats(int v) { d.totalSeats = v; return this; }
        public Builder bookedSeats(int v) { d.bookedSeats = v; return this; }
        public Builder occupancyPercentage(BigDecimal v) { d.occupancyPercentage = v; return this; }
        public FlightPerformanceDto build() { return d; }
    }

    public String getFlightId() { return flightId; }
    public String getFlightNumber() { return flightNumber; }
    public String getAirline() { return airline; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDepartureTime() { return departureTime; }
    public long getBookingCount() { return bookingCount; }
    public BigDecimal getRevenue() { return revenue; }
    public int getTotalSeats() { return totalSeats; }
    public int getBookedSeats() { return bookedSeats; }
    public BigDecimal getOccupancyPercentage() { return occupancyPercentage; }
}
