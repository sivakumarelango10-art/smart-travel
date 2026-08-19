package com.smarttravel.modules.flight.impact.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Summary of Bookings and Passengers Affected by Flight Disruption")
public class FlightImpactSummaryDto {

    @Schema(description = "Flight MongoDB ID", example = "66c1e101f1a2b3c4d5e6f702")
    private String flightId;

    @Schema(description = "Flight Number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Total number of affected active bookings", example = "42")
    private int totalAffectedBookings;

    @Schema(description = "Total number of confirmed passengers affected", example = "58")
    private int totalAffectedPassengers;

    @Schema(description = "Count of confirmed bookings")
    private int confirmedBookingsCount;

    @Schema(description = "Count of checked-in bookings")
    private int checkedInBookingsCount;

    @Schema(description = "Count of pending unconfirmed bookings")
    private int pendingBookingsCount;

    @Schema(description = "List of affected booking IDs")
    private List<String> affectedBookingIds;

    public FlightImpactSummaryDto() {
    }

    public FlightImpactSummaryDto(String flightId, String flightNumber, int totalAffectedBookings,
                                  int totalAffectedPassengers, int confirmedBookingsCount,
                                  int checkedInBookingsCount, int pendingBookingsCount,
                                  List<String> affectedBookingIds) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.totalAffectedBookings = totalAffectedBookings;
        this.totalAffectedPassengers = totalAffectedPassengers;
        this.confirmedBookingsCount = confirmedBookingsCount;
        this.checkedInBookingsCount = checkedInBookingsCount;
        this.pendingBookingsCount = pendingBookingsCount;
        this.affectedBookingIds = affectedBookingIds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public int getTotalAffectedBookings() { return totalAffectedBookings; }
    public void setTotalAffectedBookings(int totalAffectedBookings) { this.totalAffectedBookings = totalAffectedBookings; }

    public int getTotalAffectedPassengers() { return totalAffectedPassengers; }
    public void setTotalAffectedPassengers(int totalAffectedPassengers) { this.totalAffectedPassengers = totalAffectedPassengers; }

    public int getConfirmedBookingsCount() { return confirmedBookingsCount; }
    public void setConfirmedBookingsCount(int confirmedBookingsCount) { this.confirmedBookingsCount = confirmedBookingsCount; }

    public int getCheckedInBookingsCount() { return checkedInBookingsCount; }
    public void setCheckedInBookingsCount(int checkedInBookingsCount) { this.checkedInBookingsCount = checkedInBookingsCount; }

    public int getPendingBookingsCount() { return pendingBookingsCount; }
    public void setPendingBookingsCount(int pendingBookingsCount) { this.pendingBookingsCount = pendingBookingsCount; }

    public List<String> getAffectedBookingIds() { return affectedBookingIds; }
    public void setAffectedBookingIds(List<String> affectedBookingIds) { this.affectedBookingIds = affectedBookingIds; }

    public static class Builder {
        private String flightId;
        private String flightNumber;
        private int totalAffectedBookings;
        private int totalAffectedPassengers;
        private int confirmedBookingsCount;
        private int checkedInBookingsCount;
        private int pendingBookingsCount;
        private List<String> affectedBookingIds;

        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder totalAffectedBookings(int totalAffectedBookings) { this.totalAffectedBookings = totalAffectedBookings; return this; }
        public Builder totalAffectedPassengers(int totalAffectedPassengers) { this.totalAffectedPassengers = totalAffectedPassengers; return this; }
        public Builder confirmedBookingsCount(int confirmedBookingsCount) { this.confirmedBookingsCount = confirmedBookingsCount; return this; }
        public Builder checkedInBookingsCount(int checkedInBookingsCount) { this.checkedInBookingsCount = checkedInBookingsCount; return this; }
        public Builder pendingBookingsCount(int pendingBookingsCount) { this.pendingBookingsCount = pendingBookingsCount; return this; }
        public Builder affectedBookingIds(List<String> affectedBookingIds) { this.affectedBookingIds = affectedBookingIds; return this; }

        public FlightImpactSummaryDto build() {
            return new FlightImpactSummaryDto(flightId, flightNumber, totalAffectedBookings,
                    totalAffectedPassengers, confirmedBookingsCount, checkedInBookingsCount,
                    pendingBookingsCount, affectedBookingIds);
        }
    }
}
