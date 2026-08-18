package com.smarttravel.modules.flight.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Schema(description = "Flight Creation Request Payload")
public class FlightCreateRequest {

    @Schema(description = "Unique flight number", example = "AI-101")
    @NotBlank(message = "Flight number is required")
    private String flightNumber;

    @Schema(description = "Operating airline name", example = "Air India")
    @NotBlank(message = "Airline name is required")
    private String airline;

    @Schema(description = "IATA airline code", example = "AI")
    @NotBlank(message = "Airline code is required")
    private String airlineCode;

    @Schema(description = "Departure airport details")
    @NotNull(message = "Departure airport details are required")
    @Valid
    private AirportDto departureAirport;

    @Schema(description = "Arrival airport details")
    @NotNull(message = "Arrival airport details are required")
    @Valid
    private AirportDto arrivalAirport;

    @Schema(description = "Scheduled UTC departure time", example = "2026-08-20T18:30:00Z")
    @NotNull(message = "Departure time is required")
    private Instant departureTime;

    @Schema(description = "Scheduled UTC arrival time", example = "2026-08-20T20:45:00Z")
    @NotNull(message = "Arrival time is required")
    private Instant arrivalTime;

    @Schema(description = "Aircraft model", example = "Airbus A321neo")
    @NotBlank(message = "Aircraft model is required")
    private String aircraftModel;

    @Schema(description = "Base ticket price in INR", example = "5000.00")
    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than zero")
    private BigDecimal basePrice;

    @Schema(description = "Total seat capacity", example = "180")
    @Min(value = 1, message = "Total seats must be at least 1")
    private int totalSeats;

    @Schema(description = "Available seats", example = "180")
    private Integer availableSeats;

    @Schema(description = "Available cabin classes")
    private Set<CabinClass> cabinClasses;

    @Schema(description = "Initial flight status", example = "SCHEDULED")
    private FlightStatus status;

    public FlightCreateRequest() {
    }

    public FlightCreateRequest(String flightNumber, String airline, String airlineCode,
                               AirportDto departureAirport, AirportDto arrivalAirport,
                               Instant departureTime, Instant arrivalTime, String aircraftModel,
                               BigDecimal basePrice, int totalSeats, Integer availableSeats,
                               Set<CabinClass> cabinClasses, FlightStatus status) {
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.airlineCode = airlineCode;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.aircraftModel = aircraftModel;
        this.basePrice = basePrice;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.cabinClasses = cabinClasses;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String flightNumber;
        private String airline;
        private String airlineCode;
        private AirportDto departureAirport;
        private AirportDto arrivalAirport;
        private Instant departureTime;
        private Instant arrivalTime;
        private String aircraftModel;
        private BigDecimal basePrice;
        private int totalSeats;
        private Integer availableSeats;
        private Set<CabinClass> cabinClasses;
        private FlightStatus status;

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

        public Builder availableSeats(Integer availableSeats) {
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

        public FlightCreateRequest build() {
            return new FlightCreateRequest(flightNumber, airline, airlineCode, departureAirport,
                    arrivalAirport, departureTime, arrivalTime, aircraftModel, basePrice,
                    totalSeats, availableSeats, cabinClasses, status);
        }
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

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Set<CabinClass> getCabinClasses() {
        return cabinClasses;
    }

    public void setCabinClasses(Set<CabinClass> cabinClasses) {
        this.cabinClasses = cabinClasses;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }
}
