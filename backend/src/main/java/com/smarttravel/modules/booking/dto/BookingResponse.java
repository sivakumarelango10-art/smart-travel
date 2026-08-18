package com.smarttravel.modules.booking.dto;

import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.CabinClass;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Customer-facing response representation of a flight booking with price snapshot.
 */
@Schema(description = "Flight Booking Details Response")
public class BookingResponse {

    @Schema(description = "Booking MongoDB ObjectId", example = "66c1e101f1a2b3c4d5e6f801")
    private String id;

    @Schema(description = "Unique PNR / Booking Reference", example = "ST8K4P2Q")
    private String bookingReference;

    @Schema(description = "User ID", example = "66c1e101f1a2b3c4d5e6f999")
    private String userId;

    @Schema(description = "User Email", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "Flight ID", example = "66c1e101f1a2b3c4d5e6f702")
    private String flightId;

    @Schema(description = "Flight Number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Airline Name", example = "Air India")
    private String airline;

    @Schema(description = "Airline IATA Code", example = "AI")
    private String airlineCode;

    @Schema(description = "Departure Airport")
    private AirportDto departureAirport;

    @Schema(description = "Arrival Airport")
    private AirportDto arrivalAirport;

    @Schema(description = "Scheduled Departure Time (UTC)", example = "2026-08-25T10:00:00Z")
    private Instant departureTime;

    @Schema(description = "Scheduled Arrival Time (UTC)", example = "2026-08-25T12:00:00Z")
    private Instant arrivalTime;

    @Schema(description = "Flight Duration in Minutes", example = "120")
    private Integer durationMinutes;

    @Schema(description = "Reserved Cabin Tier", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "Passenger Count", example = "2")
    private int passengerCount;

    @Schema(description = "List of Passengers")
    private List<PassengerDto> passengers;

    @Schema(description = "Itemized Fare Breakdown Snapshot")
    private FareBreakdownDto fareBreakdown;

    @Schema(description = "Total Payable Amount Snapshot", example = "11500.00")
    private BigDecimal totalAmount;

    @Schema(description = "Currency Code", example = "INR")
    private String currency;

    @Schema(description = "Current Booking Status", example = "CONFIRMED")
    private BookingStatus status;

    @Schema(description = "Cancellation Timestamp (if cancelled)", example = "2026-08-21T09:30:00Z")
    private Instant cancelledAt;

    @Schema(description = "Cancellation Reason (if cancelled)", example = "Change of travel dates")
    private String cancellationReason;

    @Schema(description = "Reservation Expiration Timestamp", example = "2026-08-25T10:30:00Z")
    private Instant expiresAt;

    @Schema(description = "Booking Creation Timestamp", example = "2026-08-20T14:30:00Z")
    private Instant createdAt;

    @Schema(description = "Booking Last Modified Timestamp", example = "2026-08-20T14:30:00Z")
    private Instant updatedAt;

    public BookingResponse() {
    }

    public BookingResponse(String id, String bookingReference, String userId, String userEmail,
                           String flightId, String flightNumber, String airline, String airlineCode,
                           AirportDto departureAirport, AirportDto arrivalAirport,
                           Instant departureTime, Instant arrivalTime, Integer durationMinutes,
                           CabinClass cabinClass, int passengerCount, List<PassengerDto> passengers,
                           FareBreakdownDto fareBreakdown, BigDecimal totalAmount, String currency,
                           BookingStatus status, Instant cancelledAt, String cancellationReason,
                           Instant expiresAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.userEmail = userEmail;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.airlineCode = airlineCode;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.durationMinutes = durationMinutes;
        this.cabinClass = cabinClass;
        this.passengerCount = passengerCount;
        this.passengers = passengers;
        this.fareBreakdown = fareBreakdown;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = status;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String bookingReference;
        private String userId;
        private String userEmail;
        private String flightId;
        private String flightNumber;
        private String airline;
        private String airlineCode;
        private AirportDto departureAirport;
        private AirportDto arrivalAirport;
        private Instant departureTime;
        private Instant arrivalTime;
        private Integer durationMinutes;
        private CabinClass cabinClass;
        private int passengerCount;
        private List<PassengerDto> passengers;
        private FareBreakdownDto fareBreakdown;
        private BigDecimal totalAmount;
        private String currency = "INR";
        private BookingStatus status = BookingStatus.PENDING;
        private Instant cancelledAt;
        private String cancellationReason;
        private Instant expiresAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder bookingReference(String bookingReference) {
            this.bookingReference = bookingReference;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder userEmail(String userEmail) {
            this.userEmail = userEmail;
            return this;
        }

        public Builder flightId(String flightId) {
            this.flightId = flightId;
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

        public Builder cabinClass(CabinClass cabinClass) {
            this.cabinClass = cabinClass;
            return this;
        }

        public Builder passengerCount(int passengerCount) {
            this.passengerCount = passengerCount;
            return this;
        }

        public Builder passengers(List<PassengerDto> passengers) {
            this.passengers = passengers;
            return this;
        }

        public Builder fareBreakdown(FareBreakdownDto fareBreakdown) {
            this.fareBreakdown = fareBreakdown;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(BookingStatus status) {
            this.status = status;
            return this;
        }

        public Builder cancelledAt(Instant cancelledAt) {
            this.cancelledAt = cancelledAt;
            return this;
        }

        public Builder cancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
            return this;
        }

        public Builder expiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
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

        public BookingResponse build() {
            return new BookingResponse(id, bookingReference, userId, userEmail, flightId, flightNumber,
                    airline, airlineCode, departureAirport, arrivalAirport, departureTime,
                    arrivalTime, durationMinutes, cabinClass, passengerCount, passengers,
                    fareBreakdown, totalAmount, currency, status, cancelledAt, cancellationReason,
                    expiresAt, createdAt, updatedAt);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
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

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public int getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(int passengerCount) {
        this.passengerCount = passengerCount;
    }

    public List<PassengerDto> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerDto> passengers) {
        this.passengers = passengers;
    }

    public FareBreakdownDto getFareBreakdown() {
        return fareBreakdown;
    }

    public void setFareBreakdown(FareBreakdownDto fareBreakdown) {
        this.fareBreakdown = fareBreakdown;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
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
