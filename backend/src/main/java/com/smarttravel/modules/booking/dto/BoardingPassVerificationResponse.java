package com.smarttravel.modules.booking.dto;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;

import java.time.Instant;

/**
 * Public response DTO for Boarding Pass QR/Barcode verification scanners.
 */
public class BoardingPassVerificationResponse {

    private boolean valid;
    private String message;
    private String boardingPassNumber;
    private String bookingReference;
    private String passengerName;
    private String flightNumber;
    private String airline;
    private String seatNumber;
    private CabinClass cabinClass;
    private AirportInfo departureAirport;
    private AirportInfo arrivalAirport;
    private Instant departureTime;
    private Instant boardingTime;
    private String gate;
    private String terminal;
    private String boardingGroup;
    private String status;

    public BoardingPassVerificationResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BoardingPassVerificationResponse r = new BoardingPassVerificationResponse();

        public Builder valid(boolean v) { r.valid = v; return this; }
        public Builder message(String v) { r.message = v; return this; }
        public Builder boardingPassNumber(String v) { r.boardingPassNumber = v; return this; }
        public Builder bookingReference(String v) { r.bookingReference = v; return this; }
        public Builder passengerName(String v) { r.passengerName = v; return this; }
        public Builder flightNumber(String v) { r.flightNumber = v; return this; }
        public Builder airline(String v) { r.airline = v; return this; }
        public Builder seatNumber(String v) { r.seatNumber = v; return this; }
        public Builder cabinClass(CabinClass v) { r.cabinClass = v; return this; }
        public Builder departureAirport(AirportInfo v) { r.departureAirport = v; return this; }
        public Builder arrivalAirport(AirportInfo v) { r.arrivalAirport = v; return this; }
        public Builder departureTime(Instant v) { r.departureTime = v; return this; }
        public Builder boardingTime(Instant v) { r.boardingTime = v; return this; }
        public Builder gate(String v) { r.gate = v; return this; }
        public Builder terminal(String v) { r.terminal = v; return this; }
        public Builder boardingGroup(String v) { r.boardingGroup = v; return this; }
        public Builder status(String v) { r.status = v; return this; }

        public BoardingPassVerificationResponse build() {
            return r;
        }
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getBoardingPassNumber() { return boardingPassNumber; }
    public void setBoardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; }

    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }

    public AirportInfo getDepartureAirport() { return departureAirport; }
    public void setDepartureAirport(AirportInfo departureAirport) { this.departureAirport = departureAirport; }

    public AirportInfo getArrivalAirport() { return arrivalAirport; }
    public void setArrivalAirport(AirportInfo arrivalAirport) { this.arrivalAirport = arrivalAirport; }

    public Instant getDepartureTime() { return departureTime; }
    public void setDepartureTime(Instant departureTime) { this.departureTime = departureTime; }

    public Instant getBoardingTime() { return boardingTime; }
    public void setBoardingTime(Instant boardingTime) { this.boardingTime = boardingTime; }

    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public String getBoardingGroup() { return boardingGroup; }
    public void setBoardingGroup(String boardingGroup) { this.boardingGroup = boardingGroup; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
