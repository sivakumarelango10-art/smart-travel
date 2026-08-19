package com.smarttravel.modules.analytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Flight analytics: status distribution, performance metrics, top/bottom flights.
 */
public class FlightAnalyticsResponse {

    // Status counts
    private long totalFlights;
    private long activeFlights;
    private long scheduledFlights;
    private long boardingFlights;
    private long delayedFlights;
    private long cancelledFlights;
    private long departedFlights;
    private long arrivedFlights;
    private long divertedFlights;

    // Operational
    private long flightsDepartingToday;
    private long flightsWithLowInventory;  // < 10% seats available
    private BigDecimal averageOccupancyPercentage;

    // Status distribution map (for chart rendering)
    private Map<String, Long> statusDistribution;

    // Top performers
    private List<FlightPerformanceDto> topByRevenue;
    private List<FlightPerformanceDto> topByBookings;
    private List<FlightPerformanceDto> topByOccupancy;
    private List<FlightPerformanceDto> leastUtilized;

    private String period;
    private Instant from;
    private Instant to;
    private Instant generatedAt = Instant.now();

    public FlightAnalyticsResponse() {
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final FlightAnalyticsResponse r = new FlightAnalyticsResponse();
        public Builder totalFlights(long v) { r.totalFlights = v; return this; }
        public Builder activeFlights(long v) { r.activeFlights = v; return this; }
        public Builder scheduledFlights(long v) { r.scheduledFlights = v; return this; }
        public Builder boardingFlights(long v) { r.boardingFlights = v; return this; }
        public Builder delayedFlights(long v) { r.delayedFlights = v; return this; }
        public Builder cancelledFlights(long v) { r.cancelledFlights = v; return this; }
        public Builder departedFlights(long v) { r.departedFlights = v; return this; }
        public Builder arrivedFlights(long v) { r.arrivedFlights = v; return this; }
        public Builder divertedFlights(long v) { r.divertedFlights = v; return this; }
        public Builder flightsDepartingToday(long v) { r.flightsDepartingToday = v; return this; }
        public Builder flightsWithLowInventory(long v) { r.flightsWithLowInventory = v; return this; }
        public Builder averageOccupancyPercentage(BigDecimal v) { r.averageOccupancyPercentage = v; return this; }
        public Builder statusDistribution(Map<String, Long> v) { r.statusDistribution = v; return this; }
        public Builder topByRevenue(List<FlightPerformanceDto> v) { r.topByRevenue = v; return this; }
        public Builder topByBookings(List<FlightPerformanceDto> v) { r.topByBookings = v; return this; }
        public Builder topByOccupancy(List<FlightPerformanceDto> v) { r.topByOccupancy = v; return this; }
        public Builder leastUtilized(List<FlightPerformanceDto> v) { r.leastUtilized = v; return this; }
        public Builder period(String v) { r.period = v; return this; }
        public Builder from(Instant v) { r.from = v; return this; }
        public Builder to(Instant v) { r.to = v; return this; }
        public FlightAnalyticsResponse build() { return r; }
    }

    public long getTotalFlights() { return totalFlights; }
    public long getActiveFlights() { return activeFlights; }
    public long getScheduledFlights() { return scheduledFlights; }
    public long getBoardingFlights() { return boardingFlights; }
    public long getDelayedFlights() { return delayedFlights; }
    public long getCancelledFlights() { return cancelledFlights; }
    public long getDepartedFlights() { return departedFlights; }
    public long getArrivedFlights() { return arrivedFlights; }
    public long getDivertedFlights() { return divertedFlights; }
    public long getFlightsDepartingToday() { return flightsDepartingToday; }
    public long getFlightsWithLowInventory() { return flightsWithLowInventory; }
    public BigDecimal getAverageOccupancyPercentage() { return averageOccupancyPercentage; }
    public Map<String, Long> getStatusDistribution() { return statusDistribution; }
    public List<FlightPerformanceDto> getTopByRevenue() { return topByRevenue; }
    public List<FlightPerformanceDto> getTopByBookings() { return topByBookings; }
    public List<FlightPerformanceDto> getTopByOccupancy() { return topByOccupancy; }
    public List<FlightPerformanceDto> getLeastUtilized() { return leastUtilized; }
    public String getPeriod() { return period; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public Instant getGeneratedAt() { return generatedAt; }
}
