package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Schema(description = "Flight Details Response Payload")
public class FlightResponse {

    @Schema(description = "Flight unique database ID", example = "66c1e101f1a2b3c4d5e6f702")
    private String id;

    @Schema(description = "Unique flight number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Operating airline name", example = "Air India")
    private String airline;

    @Schema(description = "IATA airline code", example = "AI")
    private String airlineCode;

    @Schema(description = "Departure airport details")
    private AirportDto departureAirport;

    @Schema(description = "Arrival airport details")
    private AirportDto arrivalAirport;

    @Schema(description = "Scheduled UTC departure time (original schedule intact)", example = "2026-08-20T18:30:00Z")
    private Instant departureTime;

    @Schema(description = "Scheduled UTC arrival time (original schedule intact)", example = "2026-08-20T20:45:00Z")
    private Instant arrivalTime;

    @Schema(description = "Flight duration in minutes (calculated server-side)", example = "135")
    private Integer durationMinutes;

    @Schema(description = "Aircraft model", example = "Airbus A321neo")
    private String aircraftModel;

    @Schema(description = "Base ticket price in INR", example = "5000.00")
    private BigDecimal basePrice;

    @Schema(description = "Total seats capacity", example = "180")
    private int totalSeats;

    @Schema(description = "Available remaining seats", example = "42")
    private int availableSeats;

    @Schema(description = "Supported cabin classes")
    private Set<CabinClass> cabinClasses;

    @Schema(description = "Per-cabin inventory and fare pricing tiers")
    private java.util.List<CabinInventoryDto> cabinInventories;

    @Schema(description = "Selected cabin tier details and calculated multi-passenger fare (if filtered by cabin)")
    private CabinSelectionResponse selectedCabin;

    @Schema(description = "Current flight status", example = "SCHEDULED")
    private FlightStatus status;

    @Schema(description = "Delay duration in minutes (if delayed)", example = "45")
    private Integer delayMinutes;

    @Schema(description = "Reason for delay", example = "Weather conditions at destination")
    private String delayReason;

    @Schema(description = "Revised departure timestamp", example = "2026-08-20T19:15:00Z")
    private Instant revisedDepartureTime;

    @Schema(description = "Estimated arrival timestamp", example = "2026-08-20T21:30:00Z")
    private Instant estimatedArrival;

    @Schema(description = "Timestamp of last status modification", example = "2026-08-18T10:00:00Z")
    private Instant lastStatusUpdated;

    @Schema(description = "Active / published status", example = "true")
    private boolean active;

    @Schema(description = "Creation timestamp", example = "2026-08-18T10:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-08-18T10:00:00Z")
    private Instant updatedAt;

    public FlightResponse() {
    }

    public FlightResponse(String id, String flightNumber, String airline, String airlineCode,
                          AirportDto departureAirport, AirportDto arrivalAirport,
                          Instant departureTime, Instant arrivalTime, Integer durationMinutes,
                          String aircraftModel, BigDecimal basePrice, int totalSeats,
                          int availableSeats, Set<CabinClass> cabinClasses,
                          java.util.List<CabinInventoryDto> cabinInventories,
                          CabinSelectionResponse selectedCabin, FlightStatus status,
                          Integer delayMinutes, String delayReason, Instant revisedDepartureTime,
                          Instant estimatedArrival, Instant lastStatusUpdated,
                          boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.airlineCode = airlineCode;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.durationMinutes = durationMinutes;
        this.aircraftModel = aircraftModel;
        this.basePrice = basePrice;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.cabinClasses = cabinClasses;
        this.cabinInventories = cabinInventories;
        this.selectedCabin = selectedCabin;
        this.status = status;
        this.delayMinutes = delayMinutes;
        this.delayReason = delayReason;
        this.revisedDepartureTime = revisedDepartureTime;
        this.estimatedArrival = estimatedArrival;
        this.lastStatusUpdated = lastStatusUpdated;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String flightNumber;
        private String airline;
        private String airlineCode;
        private AirportDto departureAirport;
        private AirportDto arrivalAirport;
        private Instant departureTime;
        private Instant arrivalTime;
        private Integer durationMinutes;
        private String aircraftModel;
        private BigDecimal basePrice;
        private int totalSeats;
        private int availableSeats;
        private Set<CabinClass> cabinClasses;
        private java.util.List<CabinInventoryDto> cabinInventories;
        private CabinSelectionResponse selectedCabin;
        private FlightStatus status;
        private Integer delayMinutes;
        private String delayReason;
        private Instant revisedDepartureTime;
        private Instant estimatedArrival;
        private Instant lastStatusUpdated;
        private boolean active;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder flightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        public Builder airline(String airline) {
            this.airline = airline;
            return this;
        }

        public Builder airlineCode(String airlineCode) {
            this.airlineCode = airlineCode;
            return this;
        }

        public Builder departureAirport(AirportDto departureAirport) {
            this.departureAirport = departureAirport;
            return this;
        }

        public Builder arrivalAirport(AirportDto arrivalAirport) {
            this.arrivalAirport = arrivalAirport;
            return this;
        }

        public Builder departureTime(Instant departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public Builder arrivalTime(Instant arrivalTime) {
            this.arrivalTime = arrivalTime;
            return this;
        }

        public Builder durationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Builder aircraftModel(String aircraftModel) {
            this.aircraftModel = aircraftModel;
            return this;
        }

        public Builder basePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder totalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        public Builder availableSeats(int availableSeats) {
            this.availableSeats = availableSeats;
            return this;
        }

        public Builder cabinClasses(Set<CabinClass> cabinClasses) {
            this.cabinClasses = cabinClasses;
            return this;
        }

        public Builder status(FlightStatus status) {
            this.status = status;
            return this;
        }

        public Builder delayMinutes(Integer delayMinutes) {
            this.delayMinutes = delayMinutes;
            return this;
        }

        public Builder delayReason(String delayReason) {
            this.delayReason = delayReason;
            return this;
        }

        public Builder revisedDepartureTime(Instant revisedDepartureTime) {
            this.revisedDepartureTime = revisedDepartureTime;
            return this;
        }

        public Builder estimatedArrival(Instant estimatedArrival) {
            this.estimatedArrival = estimatedArrival;
            return this;
        }

        public Builder lastStatusUpdated(Instant lastStatusUpdated) {
            this.lastStatusUpdated = lastStatusUpdated;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder cabinInventories(java.util.List<CabinInventoryDto> cabinInventories) {
            this.cabinInventories = cabinInventories;
            return this;
        }

        public Builder selectedCabin(CabinSelectionResponse selectedCabin) {
            this.selectedCabin = selectedCabin;
            return this;
        }

        public FlightResponse build() {
            return new FlightResponse(id, flightNumber, airline, airlineCode, departureAirport,
                    arrivalAirport, departureTime, arrivalTime, durationMinutes, aircraftModel,
                    basePrice, totalSeats, availableSeats, cabinClasses, cabinInventories, selectedCabin, status,
                    delayMinutes, delayReason, revisedDepartureTime, estimatedArrival, lastStatusUpdated,
                    active, createdAt, updatedAt);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public AirportDto getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(AirportDto departureAirport) {
        this.departureAirport = departureAirport;
    }

    public AirportDto getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(AirportDto arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public Instant getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Instant departureTime) {
        this.departureTime = departureTime;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getAircraftModel() {
        return aircraftModel;
    }

    public void setAircraftModel(String aircraftModel) {
        this.aircraftModel = aircraftModel;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Set<CabinClass> getCabinClasses() {
        return cabinClasses;
    }

    public void setCabinClasses(Set<CabinClass> cabinClasses) {
        this.cabinClasses = cabinClasses;
    }

    public java.util.List<CabinInventoryDto> getCabinInventories() {
        return cabinInventories;
    }

    public void setCabinInventories(java.util.List<CabinInventoryDto> cabinInventories) {
        this.cabinInventories = cabinInventories;
    }

    public CabinSelectionResponse getSelectedCabin() {
        return selectedCabin;
    }

    public void setSelectedCabin(CabinSelectionResponse selectedCabin) {
        this.selectedCabin = selectedCabin;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public String getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(String delayReason) {
        this.delayReason = delayReason;
    }

    public Instant getRevisedDepartureTime() {
        return revisedDepartureTime;
    }

    public void setRevisedDepartureTime(Instant revisedDepartureTime) {
        this.revisedDepartureTime = revisedDepartureTime;
    }

    public Instant getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(Instant estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public Instant getLastStatusUpdated() {
        return lastStatusUpdated;
    }

    public void setLastStatusUpdated(Instant lastStatusUpdated) {
        this.lastStatusUpdated = lastStatusUpdated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
