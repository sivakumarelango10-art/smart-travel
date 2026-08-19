package com.smarttravel.modules.booking.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB Document entity representing a passenger check-in record.
 */
@Document(collection = "check_ins")
@CompoundIndexes({
        @CompoundIndex(name = "checkin_booking_unique_idx", def = "{'bookingId': 1}", unique = true),
        @CompoundIndex(name = "checkin_user_idx", def = "{'userId': 1, 'checkedInAt': -1}")
})
public class CheckIn {

    @Id
    private String id;

    @Indexed(unique = true)
    private String checkInNumber;

    @Indexed
    private String bookingId;

    @Indexed
    private String bookingReference;

    @Indexed
    private String userId;

    private String flightId;

    private String flightNumber;

    private List<PassengerCheckInInfo> passengers = new ArrayList<>();

    private CheckInStatus status = CheckInStatus.COMPLETED;

    private Instant checkedInAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public CheckIn() {
    }

    public CheckIn(String id, String checkInNumber, String bookingId, String bookingReference,
                   String userId, String flightId, String flightNumber,
                   List<PassengerCheckInInfo> passengers, CheckInStatus status,
                   Instant checkedInAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.checkInNumber = checkInNumber;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.passengers = passengers != null ? passengers : new ArrayList<>();
        this.status = status != null ? status : CheckInStatus.COMPLETED;
        this.checkedInAt = checkedInAt != null ? checkedInAt : Instant.now();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCheckInNumber() { return checkInNumber; }
    public void setCheckInNumber(String checkInNumber) { this.checkInNumber = checkInNumber; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public List<PassengerCheckInInfo> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerCheckInInfo> passengers) { this.passengers = passengers; }

    public CheckInStatus getStatus() { return status; }
    public void setStatus(CheckInStatus status) { this.status = status; }

    public Instant getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Instant checkedInAt) { this.checkedInAt = checkedInAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private String id;
        private String checkInNumber;
        private String bookingId;
        private String bookingReference;
        private String userId;
        private String flightId;
        private String flightNumber;
        private List<PassengerCheckInInfo> passengers = new ArrayList<>();
        private CheckInStatus status = CheckInStatus.COMPLETED;
        private Instant checkedInAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder checkInNumber(String checkInNumber) { this.checkInNumber = checkInNumber; return this; }
        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder bookingReference(String bookingReference) { this.bookingReference = bookingReference; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder passengers(List<PassengerCheckInInfo> passengers) { this.passengers = passengers; return this; }
        public Builder status(CheckInStatus status) { this.status = status; return this; }
        public Builder checkedInAt(Instant checkedInAt) { this.checkedInAt = checkedInAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public CheckIn build() {
            return new CheckIn(id, checkInNumber, bookingId, bookingReference, userId, flightId, flightNumber,
                    passengers, status, checkedInAt, createdAt, updatedAt);
        }
    }
}
