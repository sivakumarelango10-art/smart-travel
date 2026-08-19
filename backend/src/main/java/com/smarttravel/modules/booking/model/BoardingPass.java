package com.smarttravel.modules.booking.model;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB Document entity representing an issued passenger Boarding Pass.
 */
@Document(collection = "boarding_passes")
@CompoundIndexes({
        @CompoundIndex(name = "bp_booking_passenger_idx", def = "{'bookingId': 1, 'passengerName': 1}"),
        @CompoundIndex(name = "bp_user_issued_idx", def = "{'userId': 1, 'issuedAt': -1}")
})
public class BoardingPass {

    @Id
    private String id;

    @Indexed(unique = true)
    private String boardingPassNumber;

    @Indexed
    private String checkInId;

    @Indexed
    private String bookingId;

    @Indexed
    private String bookingReference;

    private String userId;

    private String ticketNumber;

    private String eTicketNumber;

    private String passengerName;

    private String seatNumber;

    private CabinClass cabinClass;

    private String flightNumber;

    private String airline;

    private String airlineCode;

    private AirportInfo departureAirport;

    private AirportInfo arrivalAirport;

    private Instant departureTime;

    private Instant arrivalTime;

    private String boardingGroup = "Group 1";

    private String gate = "Gate 12";

    private String terminal = "T3";

    private Instant boardingTime;

    private String barcodeData;

    private Instant issuedAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public BoardingPass() {
    }

    public BoardingPass(String id, String boardingPassNumber, String checkInId, String bookingId,
                        String bookingReference, String userId, String ticketNumber,
                        String eTicketNumber, String passengerName, String seatNumber,
                        CabinClass cabinClass, String flightNumber, String airline,
                        String airlineCode, AirportInfo departureAirport, AirportInfo arrivalAirport,
                        Instant departureTime, Instant arrivalTime, String boardingGroup,
                        String gate, String terminal, Instant boardingTime, String barcodeData,
                        Instant issuedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.boardingPassNumber = boardingPassNumber;
        this.checkInId = checkInId;
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.userId = userId;
        this.ticketNumber = ticketNumber;
        this.eTicketNumber = eTicketNumber;
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
        this.cabinClass = cabinClass;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.airlineCode = airlineCode;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.boardingGroup = boardingGroup != null ? boardingGroup : "Group 1";
        this.gate = gate != null ? gate : "Gate 12";
        this.terminal = terminal != null ? terminal : "T3";
        this.boardingTime = boardingTime;
        this.barcodeData = barcodeData;
        this.issuedAt = issuedAt != null ? issuedAt : Instant.now();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBoardingPassNumber() { return boardingPassNumber; }
    public void setBoardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; }

    public String getCheckInId() { return checkInId; }
    public void setCheckInId(String checkInId) { this.checkInId = checkInId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getETicketNumber() { return eTicketNumber; }
    public void setETicketNumber(String eTicketNumber) { this.eTicketNumber = eTicketNumber; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getAirlineCode() { return airlineCode; }
    public void setAirlineCode(String airlineCode) { this.airlineCode = airlineCode; }

    public AirportInfo getDepartureAirport() { return departureAirport; }
    public void setDepartureAirport(AirportInfo departureAirport) { this.departureAirport = departureAirport; }

    public AirportInfo getArrivalAirport() { return arrivalAirport; }
    public void setArrivalAirport(AirportInfo arrivalAirport) { this.arrivalAirport = arrivalAirport; }

    public Instant getDepartureTime() { return departureTime; }
    public void setDepartureTime(Instant departureTime) { this.departureTime = departureTime; }

    public Instant getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(Instant arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getBoardingGroup() { return boardingGroup; }
    public void setBoardingGroup(String boardingGroup) { this.boardingGroup = boardingGroup; }

    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public Instant getBoardingTime() { return boardingTime; }
    public void setBoardingTime(Instant boardingTime) { this.boardingTime = boardingTime; }

    public String getBarcodeData() { return barcodeData; }
    public void setBarcodeData(String barcodeData) { this.barcodeData = barcodeData; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private String id;
        private String boardingPassNumber;
        private String checkInId;
        private String bookingId;
        private String bookingReference;
        private String userId;
        private String ticketNumber;
        private String eTicketNumber;
        private String passengerName;
        private String seatNumber;
        private CabinClass cabinClass;
        private String flightNumber;
        private String airline;
        private String airlineCode;
        private AirportInfo departureAirport;
        private AirportInfo arrivalAirport;
        private Instant departureTime;
        private Instant arrivalTime;
        private String boardingGroup = "Group 1";
        private String gate = "Gate 12";
        private String terminal = "T3";
        private Instant boardingTime;
        private String barcodeData;
        private Instant issuedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder boardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; return this; }
        public Builder checkInId(String checkInId) { this.checkInId = checkInId; return this; }
        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder bookingReference(String bookingReference) { this.bookingReference = bookingReference; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder ticketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; return this; }
        public Builder eTicketNumber(String eTicketNumber) { this.eTicketNumber = eTicketNumber; return this; }
        public Builder passengerName(String passengerName) { this.passengerName = passengerName; return this; }
        public Builder seatNumber(String seatNumber) { this.seatNumber = seatNumber; return this; }
        public Builder cabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder airline(String airline) { this.airline = airline; return this; }
        public Builder airlineCode(String airlineCode) { this.airlineCode = airlineCode; return this; }
        public Builder departureAirport(AirportInfo departureAirport) { this.departureAirport = departureAirport; return this; }
        public Builder arrivalAirport(AirportInfo arrivalAirport) { this.arrivalAirport = arrivalAirport; return this; }
        public Builder departureTime(Instant departureTime) { this.departureTime = departureTime; return this; }
        public Builder arrivalTime(Instant arrivalTime) { this.arrivalTime = arrivalTime; return this; }
        public Builder boardingGroup(String boardingGroup) { this.boardingGroup = boardingGroup; return this; }
        public Builder gate(String gate) { this.gate = gate; return this; }
        public Builder terminal(String terminal) { this.terminal = terminal; return this; }
        public Builder boardingTime(Instant boardingTime) { this.boardingTime = boardingTime; return this; }
        public Builder barcodeData(String barcodeData) { this.barcodeData = barcodeData; return this; }
        public Builder issuedAt(Instant issuedAt) { this.issuedAt = issuedAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public BoardingPass build() {
            return new BoardingPass(id, boardingPassNumber, checkInId, bookingId, bookingReference,
                    userId, ticketNumber, eTicketNumber, passengerName, seatNumber, cabinClass,
                    flightNumber, airline, airlineCode, departureAirport, arrivalAirport,
                    departureTime, arrivalTime, boardingGroup, gate, terminal, boardingTime,
                    barcodeData, issuedAt, createdAt, updatedAt);
        }
    }
}
