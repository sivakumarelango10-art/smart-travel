package com.smarttravel.modules.booking.dto;

import com.smarttravel.modules.booking.model.CheckInStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/**
 * Data Transfer Object for complete online check-in details.
 */
@Schema(description = "Online Check-In Response Payload")
public class CheckInResponse {

    @Schema(description = "Check-In Record MongoDB ID", example = "66c1e101f1a2b3c4d5e6f901")
    private String id;

    @Schema(description = "Public Check-In Confirmation Number", example = "CI-98AK72M189QL")
    private String checkInNumber;

    @Schema(description = "Booking ID", example = "66c1e101f1a2b3c4d5e6f801")
    private String bookingId;

    @Schema(description = "Booking Reference (PNR)", example = "ST8K4P2Q")
    private String bookingReference;

    @Schema(description = "Flight Number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Check-in Status", example = "COMPLETED")
    private CheckInStatus status;

    @Schema(description = "Timestamp of check-in")
    private Instant checkedInAt;

    @Schema(description = "List of checked-in passengers with assigned seats and boarding passes")
    private List<PassengerCheckInResponse> passengers;

    public CheckInResponse() {
    }

    public CheckInResponse(String id, String checkInNumber, String bookingId, String bookingReference,
                           String flightNumber, CheckInStatus status, Instant checkedInAt,
                           List<PassengerCheckInResponse> passengers) {
        this.id = id;
        this.checkInNumber = checkInNumber;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.flightNumber = flightNumber;
        this.status = status;
        this.checkedInAt = checkedInAt;
        this.passengers = passengers;
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

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public CheckInStatus getStatus() { return status; }
    public void setStatus(CheckInStatus status) { this.status = status; }

    public Instant getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Instant checkedInAt) { this.checkedInAt = checkedInAt; }

    public List<PassengerCheckInResponse> getPassengers() { return passengers; }
    public void setPassengers(List<PassengerCheckInResponse> passengers) { this.passengers = passengers; }

    public static class Builder {
        private String id;
        private String checkInNumber;
        private String bookingId;
        private String bookingReference;
        private String flightNumber;
        private CheckInStatus status;
        private Instant checkedInAt;
        private List<PassengerCheckInResponse> passengers;

        public Builder id(String id) { this.id = id; return this; }
        public Builder checkInNumber(String checkInNumber) { this.checkInNumber = checkInNumber; return this; }
        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder bookingReference(String bookingReference) { this.bookingReference = bookingReference; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder status(CheckInStatus status) { this.status = status; return this; }
        public Builder checkedInAt(Instant checkedInAt) { this.checkedInAt = checkedInAt; return this; }
        public Builder passengers(List<PassengerCheckInResponse> passengers) { this.passengers = passengers; return this; }

        public CheckInResponse build() {
            return new CheckInResponse(id, checkInNumber, bookingId, bookingReference, flightNumber, status, checkedInAt, passengers);
        }
    }
}
