package com.smarttravel.modules.booking.model;

import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MongoDB Document entity representing a flight booking and reservation.
 */
@Document(collection = "bookings")
@CompoundIndexes({
        @CompoundIndex(name = "booking_user_created_idx", def = "{'userId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "booking_flight_status_idx", def = "{'flightId': 1, 'status': 1}"),
        @CompoundIndex(name = "booking_status_expires_idx", def = "{'status': 1, 'expiresAt': 1}")
})
public class Booking {

    @Id
    private String id;

    @Indexed(unique = true)
    private String bookingReference;

    @Indexed
    private String userId;

    private String userEmail;

    @Indexed
    private String flightId;

    private String flightNumber;

    private String airline;

    private String airlineCode;

    private AirportInfo departureAirport;

    private AirportInfo arrivalAirport;

    private Instant departureTime;

    private Instant arrivalTime;

    private Integer durationMinutes;

    private CabinClass cabinClass;

    private int passengerCount;

    private List<Passenger> passengers = new ArrayList<>();

    private FareBreakdownDto fareBreakdown;

    private BigDecimal totalAmount;

    private String currency = "INR";

    private BookingStatus status = BookingStatus.PENDING;

    private Instant cancelledAt;

    private String cancellationReason;

    private Instant expiresAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Booking() {
    }

    public Booking(String id, String bookingReference, String userId, String userEmail,
                   String flightId, String flightNumber, String airline, String airlineCode,
                   AirportInfo departureAirport, AirportInfo arrivalAirport,
                   Instant departureTime, Instant arrivalTime, Integer durationMinutes,
                   CabinClass cabinClass, int passengerCount, List<Passenger> passengers,
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
        this.passengers = passengers != null ? passengers : new ArrayList<>();
        this.fareBreakdown = fareBreakdown;
        this.totalAmount = totalAmount;
        this.currency = currency != null ? currency : "INR";
        this.status = status != null ? status : BookingStatus.PENDING;
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
        private AirportInfo departureAirport;
        private AirportInfo arrivalAirport;
        private Instant departureTime;
        private Instant arrivalTime;
        private Integer durationMinutes;
        private CabinClass cabinClass;
        private int passengerCount;
        private List<Passenger> passengers = new ArrayList<>();
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

        public Builder departureAirport(AirportInfo departureAirport) {
            this.departureAirport = departureAirport;
            return this;
        }

        public Builder arrivalAirport(AirportInfo arrivalAirport) {
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

        public Builder passengers(List<Passenger> passengers) {
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

        public Booking build() {
            return new Booking(id, bookingReference, userId, userEmail, flightId, flightNumber,
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

    public AirportInfo getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(AirportInfo departureAirport) {
        this.departureAirport = departureAirport;
    }

    public AirportInfo getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(AirportInfo arrivalAirport) {
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

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers != null ? passengers : new ArrayList<>();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id) &&
                Objects.equals(bookingReference, booking.bookingReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bookingReference);
    }
}
