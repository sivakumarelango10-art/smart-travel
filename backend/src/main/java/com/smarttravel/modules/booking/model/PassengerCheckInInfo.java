package com.smarttravel.modules.booking.model;

import com.smarttravel.modules.flight.model.CabinClass;

/**
 * Embedded document representing passenger check-in and boarding pass mapping.
 */
public class PassengerCheckInInfo {

    private String passengerId;
    private String title;
    private String firstName;
    private String lastName;
    private String seatNumber;
    private CabinClass cabinClass;
    private String eTicketNumber;
    private String boardingPassNumber;

    public PassengerCheckInInfo() {
    }

    public PassengerCheckInInfo(String passengerId, String title, String firstName, String lastName,
                                String seatNumber, CabinClass cabinClass, String eTicketNumber,
                                String boardingPassNumber) {
        this.passengerId = passengerId;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.seatNumber = seatNumber;
        this.cabinClass = cabinClass;
        this.eTicketNumber = eTicketNumber;
        this.boardingPassNumber = boardingPassNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getSeatNumber() { return seatNumber; }
    public void setSeatNumber(String seatNumber) { this.seatNumber = seatNumber; }

    public CabinClass getCabinClass() { return cabinClass; }
    public void setCabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; }

    public String getETicketNumber() { return eTicketNumber; }
    public void setETicketNumber(String eTicketNumber) { this.eTicketNumber = eTicketNumber; }

    public String getBoardingPassNumber() { return boardingPassNumber; }
    public void setBoardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; }

    public static class Builder {
        private String passengerId;
        private String title;
        private String firstName;
        private String lastName;
        private String seatNumber;
        private CabinClass cabinClass;
        private String eTicketNumber;
        private String boardingPassNumber;

        public Builder passengerId(String passengerId) { this.passengerId = passengerId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder seatNumber(String seatNumber) { this.seatNumber = seatNumber; return this; }
        public Builder cabinClass(CabinClass cabinClass) { this.cabinClass = cabinClass; return this; }
        public Builder eTicketNumber(String eTicketNumber) { this.eTicketNumber = eTicketNumber; return this; }
        public Builder boardingPassNumber(String boardingPassNumber) { this.boardingPassNumber = boardingPassNumber; return this; }

        public PassengerCheckInInfo build() {
            return new PassengerCheckInInfo(passengerId, title, firstName, lastName, seatNumber, cabinClass, eTicketNumber, boardingPassNumber);
        }
    }
}
