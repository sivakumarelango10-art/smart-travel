package com.smarttravel.modules.ticket.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Public response DTO for individual passenger details on an issued ticket.
 */
@Schema(description = "Passenger Details on Issued Ticket")
public class PassengerTicketResponse {

    @Schema(description = "Passenger title", example = "Ms")
    private String title;

    @Schema(description = "First name", example = "Sarah")
    private String firstName;

    @Schema(description = "Last name", example = "Connor")
    private String lastName;

    @Schema(description = "Date of birth", example = "1992-05-15")
    private LocalDate dateOfBirth;

    @Schema(description = "Gender", example = "FEMALE")
    private String gender;

    @Schema(description = "Nationality", example = "Indian")
    private String nationality;

    @Schema(description = "Assigned seat number if any", example = "14B")
    private String seatNumber;

    @Schema(description = "Individual passenger E-Ticket number", example = "ST-8K4P2Q7X9Y1Z-01")
    private String eTicketNumber;

    public PassengerTicketResponse() {
    }

    public PassengerTicketResponse(String title, String firstName, String lastName, LocalDate dateOfBirth,
                                   String gender, String nationality, String seatNumber, String eTicketNumber) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationality = nationality;
        this.seatNumber = seatNumber;
        this.eTicketNumber = eTicketNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String gender;
        private String nationality;
        private String seatNumber;
        private String eTicketNumber;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder nationality(String nationality) {
            this.nationality = nationality;
            return this;
        }

        public Builder seatNumber(String seatNumber) {
            this.seatNumber = seatNumber;
            return this;
        }

        public Builder eTicketNumber(String eTicketNumber) {
            this.eTicketNumber = eTicketNumber;
            return this;
        }

        public PassengerTicketResponse build() {
            return new PassengerTicketResponse(title, firstName, lastName, dateOfBirth, gender, nationality, seatNumber, eTicketNumber);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getETicketNumber() {
        return eTicketNumber;
    }

    public void setETicketNumber(String eTicketNumber) {
        this.eTicketNumber = eTicketNumber;
    }
}
