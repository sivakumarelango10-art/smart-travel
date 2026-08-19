package com.smarttravel.modules.ticket.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Passenger snapshot information preserved on an issued ticket.
 */
public class PassengerTicketInfo {

    private String title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationality;
    private String passportNumber;
    private String seatNumber;
    private String eTicketNumber;

    public PassengerTicketInfo() {
    }

    public PassengerTicketInfo(String title, String firstName, String lastName, LocalDate dateOfBirth,
                               String gender, String nationality, String passportNumber,
                               String seatNumber, String eTicketNumber) {
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationality = nationality;
        this.passportNumber = passportNumber;
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
        private String passportNumber;
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

        public Builder passportNumber(String passportNumber) {
            this.passportNumber = passportNumber;
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

        public PassengerTicketInfo build() {
            return new PassengerTicketInfo(title, firstName, lastName, dateOfBirth, gender, nationality, passportNumber, seatNumber, eTicketNumber);
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

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassengerTicketInfo that = (PassengerTicketInfo) o;
        return Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(eTicketNumber, that.eTicketNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, eTicketNumber);
    }
}
