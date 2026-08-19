package com.smarttravel.modules.booking.dto;

import com.smarttravel.modules.flight.model.CabinClass;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Passenger details within a check-in response.
 */
@Schema(description = "Checked-in Passenger Details")
public class PassengerCheckInResponse {

    @Schema(description = "Passenger Title", example = "Ms")
    private String title;

    @Schema(description = "Passenger Full Name", example = "Sarah Connor")
    private String passengerName;

    @Schema(description = "Assigned Seat Number", example = "12A")
    private String seatNumber;

    @Schema(description = "Cabin Class", example = "ECONOMY")
    private CabinClass cabinClass;

    @Schema(description = "E-Ticket Number", example = "ST-MW827QQJRL45-01")
    private String eTicketNumber;

    @Schema(description = "Boarding Pass Reference Number", example = "BP-9AK72M189Q4L")
    private String boardingPassNumber;

    public PassengerCheckInResponse() {
    }

    public PassengerCheckInResponse(String title, String passengerName, String seatNumber,
                                    CabinClass cabinClass, String eTicketNumber, String boardingPassNumber) {
        this.title = title;
        this.passengerName = passengerName;
        this.seatNumber = seatNumber;
        this.cabinClass = cabinClass;
        this.eTicketNumber = eTicketNumber;
        this.boardingPassNumber = boardingPassNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }

    public String getETicketNumber() { return eTicketNumber; }
    public void setETicketNumber(String eTicketNumber) { this.eTicketNumber = eTicketNumber; }

    public String getBoardingPassNumber() { return boardingPassNumber; }
    public void setBoardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; }

    public static class Builder {
        private String title;
        private String passengerName;
        private String seatNumber;
        private CabinClass cabinClass;
        private String eTicketNumber;
        private String boardingPassNumber;

        public Builder title(String title) { this.title = title; return this; }
        public Builder passengerName(String passengerName) { this.passengerName = passengerName; return this; }
        public Builder seatNumber(String seatNumber) { this.seatNumber = seatNumber; return this; }
        public Builder cabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; return this; }
        public Builder eTicketNumber(String eTicketNumber) { this.eTicketNumber = eTicketNumber; return this; }
        public Builder boardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; return this; }

        public PassengerCheckInResponse build() {
            return new PassengerCheckInResponse(title, passengerName, seatNumber, cabinClass, eTicketNumber, boardingPassNumber);
        }
    }
}
