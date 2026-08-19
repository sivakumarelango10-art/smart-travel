package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for complete flight seat map layout.
 */
@Schema(description = "Flight Seat Map Response")
public class SeatMapResponse {

    @Schema(description = "Flight MongoDB ID", example = "66c1e101f1a2b3c4d5e6f701")
    private String flightId;

    @Schema(description = "Flight Number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Aircraft Model", example = "Boeing 737 MAX 8")
    private String aircraftModel;

    @Schema(description = "Total Seats", example = "150")
    private int totalSeats;

    @Schema(description = "Available Seats Count", example = "142")
    private int availableSeatsCount;

    @Schema(description = "List of all seats on the aircraft")
    private List<SeatDto> seats;

    @Schema(description = "Seats grouped by Cabin Class")
    private Map<CabinClass, List<SeatDto>> cabinSeats;

    public SeatMapResponse() {
    }

    public SeatMapResponse(String flightId, String flightNumber, String aircraftModel,
                           int totalSeats, int availableSeatsCount, List<SeatDto> seats,
                           Map<CabinClass, List<SeatDto>> cabinSeats) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.aircraftModel = aircraftModel;
        this.totalSeats = totalSeats;
        this.availableSeatsCount = availableSeatsCount;
        this.seats = seats;
        this.cabinSeats = cabinSeats;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) { this.totalSeats = totalSeats; }

    public int getAvailableSeatsCount() { return availableSeatsCount; }
    public void setAvailableSeatsCount(int availableSeatsCount) { this.availableSeatsCount = availableSeatsCount; }

    public List<SeatDto> getSeats() { return seats; }
    public void setSeats(List<SeatDto> seats) { this.seats = seats; }

    public Map<CabinClass, List<SeatDto>> getCabinSeats() { return cabinSeats; }
    public void setCabinSeats(Map<CabinClass, List<SeatDto>> cabinSeats) { this.cabinSeats = cabinSeats; }

    public static class Builder {
        private String flightId;
        private String flightNumber;
        private String aircraftModel;
        private int totalSeats;
        private int availableSeatsCount;
        private List<SeatDto> seats;
        private Map<CabinClass, List<SeatDto>> cabinSeats;

        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder aircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; return this; }
        public Builder totalSeats(int totalSeats) { this.totalSeats = totalSeats; return this; }
        public Builder availableSeatsCount(int availableSeatsCount) { this.availableSeatsCount = availableSeatsCount; return this; }
        public Builder seats(List<SeatDto> seats) { this.seats = seats; return this; }
        public Builder cabinSeats(Map<CabinClass, List<SeatDto>> cabinSeats) { this.cabinSeats = cabinSeats; return this; }

        public SeatMapResponse build() {
            return new SeatMapResponse(flightId, flightNumber, aircraftModel, totalSeats,
                    availableSeatsCount, seats, cabinSeats);
        }
    }
}
