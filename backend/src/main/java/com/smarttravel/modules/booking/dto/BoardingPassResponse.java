package com.smarttravel.modules.booking.dto;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Data Transfer Object for passenger Boarding Pass representation.
 */
@Schema(description = "Boarding Pass Details Response")
public class BoardingPassResponse {

    @Schema(description = "Boarding Pass MongoDB ID", example = "66c1e101f1a2b3c4d5e6fa01")
    private String id;

    @Schema(description = "Public Boarding Pass Number", example = "BP-9AK72M189Q4L")
    private String boardingPassNumber;

    @Schema(description = "Booking PNR", example = "ST8K4P2Q")
    private String bookingReference;

    @Schema(description = "Ticket Number", example = "ST-MW827QQJRL45")
    private String ticketNumber;

    @Schema(description = "E-Ticket Number", example = "ST-MW827QQJRL45-01")
    private String eTicketNumber;

    @Schema(description = "Passenger Full Name", example = "Sarah Connor")
    private String passengerName;

    @Schema(description = "Assigned Seat", example = "12A")
    private String seatNumber;

    @Schema(description = "Cabin Class", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "Flight Number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Airline Name", example = "SmartAir Express")
    private String airline;

    @Schema(description = "Departure Airport")
    private AirportInfo departureAirport;

    @Schema(description = "Arrival Airport")
    private AirportInfo arrivalAirport;

    @Schema(description = "Departure Date and Time")
    private Instant departureTime;

    @Schema(description = "Arrival Date and Time")
    private Instant arrivalTime;

    @Schema(description = "Boarding Group", example = "Group 1")
    private String boardingGroup;

    @Schema(description = "Departure Gate", example = "Gate 12")
    private String gate;

    @Schema(description = "Departure Terminal", example = "T3")
    private String terminal;

    @Schema(description = "Boarding Commencement Time")
    private Instant boardingTime;

    @Schema(description = "Timestamp of issuance")
    private Instant issuedAt;

    public BoardingPassResponse() {
    }

    public BoardingPassResponse(String id, String boardingPassNumber, String bookingReference,
                                String ticketNumber, String eTicketNumber, String passengerName,
                                String seatNumber, CabinClass cabinClass, String flightNumber,
                                String airline, AirportInfo departureAirport, AirportInfo arrivalAirport,
                                Instant departureTime, Instant arrivalTime, String boardingGroup,
                                String gate, String terminal, Instant boardingTime, Instant issuedAt) {
        this.id = id;
        this.boardingPassNumber = boardingPassNumber;
        this.bookingReference = bookingReference;
        this.ticketNumber = ticketNumber;
        this.eTicketNumber = eTicketNumber;
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
        this.cabinClass = cabinClass;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.boardingGroup = boardingGroup;
        this.gate = gate;
        this.terminal = terminal;
        this.boardingTime = boardingTime;
        this.issuedAt = issuedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBoardingPassNumber() { return boardingPassNumber; }
    public void setBoardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

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

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

    public static class Builder {
        private String id;
        private String boardingPassNumber;
        private String bookingReference;
        private String ticketNumber;
        private String eTicketNumber;
        private String passengerName;
        private String seatNumber;
        private CabinClass cabinClass;
        private String flightNumber;
        private String airline;
        private AirportInfo departureAirport;
        private AirportInfo arrivalAirport;
        private Instant departureTime;
        private Instant arrivalTime;
        private String boardingGroup;
        private String gate;
        private String terminal;
        private Instant boardingTime;
        private Instant issuedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder boardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; return this; }
        public Builder bookingReference(String bookingReference) { this.bookingReference = bookingReference; return this; }
        public Builder ticketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; return this; }
        public Builder eTicketNumber(String eTicketNumber) { this.eTicketNumber = eTicketNumber; return this; }
        public Builder passengerName(String passengerName) { this.passengerName = passengerName; return this; }
        public Builder seatNumber(String seatNumber) { this.seatNumber = seatNumber; return this; }
        public Builder cabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder airline(String airline) { this.airline = airline; return this; }
        public Builder departureAirport(AirportInfo departureAirport) { this.departureAirport = departureAirport; return this; }
        public Builder arrivalAirport(AirportInfo arrivalAirport) { this.arrivalAirport = arrivalAirport; return this; }
        public Builder departureTime(Instant departureTime) { this.departureTime = departureTime; return this; }
        public Builder arrivalTime(Instant arrivalTime) { this.arrivalTime = arrivalTime; return this; }
        public Builder boardingGroup(String boardingGroup) { this.boardingGroup = boardingGroup; return this; }
        public Builder gate(String gate) { this.gate = gate; return this; }
        public Builder terminal(String terminal) { this.terminal = terminal; return this; }
        public Builder boardingTime(Instant boardingTime) { this.boardingTime = boardingTime; return this; }
        public Builder issuedAt(Instant issuedAt) { this.issuedAt = issuedAt; return this; }

        public BoardingPassResponse build() {
            return new BoardingPassResponse(id, boardingPassNumber, bookingReference, ticketNumber,
                    eTicketNumber, passengerName, seatNumber, cabinClass, flightNumber, airline,
                    departureAirport, arrivalAirport, departureTime, arrivalTime, boardingGroup,
                    gate, terminal, boardingTime, issuedAt);
        }
    }
}
