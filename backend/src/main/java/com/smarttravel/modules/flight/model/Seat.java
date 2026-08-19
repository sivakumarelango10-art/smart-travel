package com.smarttravel.modules.flight.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * MongoDB Document entity representing a physical seat on a flight.
 */
@Document(collection = "seats")
@CompoundIndexes({
        @CompoundIndex(name = "flight_seat_unique_idx", def = "{'flightId': 1, 'seatNumber': 1}", unique = true),
        @CompoundIndex(name = "flight_cabin_status_idx", def = "{'flightId': 1, 'cabinClass': 1, 'status': 1}"),
        @CompoundIndex(name = "seat_booking_idx", def = "{'bookingId': 1}"),
        @CompoundIndex(name = "seat_expiry_idx", def = "{'status': 1, 'expiresAt': 1}")
})
public class Seat {

    @Id
    private String id;

    @Indexed
    private String flightId;

    private String flightNumber;

    private String seatNumber;

    private int rowNumber;

    private String column;

    private CabinClass cabinClass;

    private SeatStatus status = SeatStatus.AVAILABLE;

    private BigDecimal priceAdjustment = BigDecimal.ZERO;

    private String bookingId;

    private String bookingReference;

    private String passengerId;

    private Instant heldAt;

    private Instant expiresAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Seat() {
    }

    public Seat(String id, String flightId, String flightNumber, String seatNumber, int rowNumber,
                String column, CabinClass cabinClass, SeatStatus status, BigDecimal priceAdjustment,
                String bookingId, String bookingReference, String passengerId, Instant heldAt,
                Instant expiresAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.seatNumber = seatNumber;
        this.rowNumber = rowNumber;
        this.column = column;
        this.cabinClass = cabinClass;
        this.status = status != null ? status : SeatStatus.AVAILABLE;
        this.priceAdjustment = priceAdjustment != null ? priceAdjustment : BigDecimal.ZERO;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.passengerId = passengerId;
        this.heldAt = heldAt;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public BigDecimal getPriceAdjustment() {
        return priceAdjustment;
    }

    public void setPriceAdjustment(BigDecimal priceAdjustment) {
        this.priceAdjustment = priceAdjustment;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public Instant getHeldAt() {
        return heldAt;
    }

    public void setHeldAt(Instant heldAt) {
        this.heldAt = heldAt;
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
        Seat seat = (Seat) o;
        return Objects.equals(flightId, seat.flightId) && Objects.equals(seatNumber, seat.seatNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flightId, seatNumber);
    }

    public static class Builder {
        private String id;
        private String flightId;
        private String flightNumber;
        private String seatNumber;
        private int rowNumber;
        private String column;
        private CabinClass cabinClass;
        private SeatStatus status = SeatStatus.AVAILABLE;
        private BigDecimal priceAdjustment = BigDecimal.ZERO;
        private String bookingId;
        private String bookingReference;
        private String passengerId;
        private Instant heldAt;
        private Instant expiresAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder seatNumber(String seatNumber) { this.seatNumber = seatNumber; return this; }
        public Builder rowNumber(int rowNumber) { this.rowNumber = rowNumber; return this; }
        public Builder column(String column) { this.column = column; return this; }
        public Builder cabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; return this; }
        public Builder status(SeatStatus status) { this.status = status; return this; }
        public Builder priceAdjustment(BigDecimal priceAdjustment) { this.priceAdjustment = priceAdjustment; return this; }
        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder bookingReference(String bookingReference) { this.bookingReference = bookingReference; return this; }
        public Builder passengerId(String passengerId) { this.passengerId = passengerId; return this; }
        public Builder heldAt(Instant heldAt) { this.heldAt = heldAt; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Seat build() {
            return new Seat(id, flightId, flightNumber, seatNumber, rowNumber, column, cabinClass,
                    status, priceAdjustment, bookingId, bookingReference, passengerId, heldAt,
                    expiresAt, createdAt, updatedAt);
        }
    }
}
