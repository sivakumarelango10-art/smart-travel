package com.smarttravel.modules.booking.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Embedded domain entity representing an individual passenger within a booking.
 */
public class Passenger {

    private String passengerId;
    private String title;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String nationality;
    private String passportNumber;
    private String seatNumber;

    public Passenger() {
    }

    public Passenger(String passengerId, String title, String firstName, String lastName,
                     LocalDate dateOfBirth, String gender, String nationality,
                     String passportNumber, String seatNumber) {
        this.passengerId = passengerId;
        this.title = title;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.nationality = nationality;
        this.passportNumber = passportNumber;
        this.seatNumber = seatNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String passengerId;
        private String title;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private String gender;
        private String nationality;
        private String passportNumber;
        private String seatNumber;

        public Builder passengerId(String passengerId) {
            this.passengerId = passengerId;
            return this;
        }

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

        public Passenger build() {
            return new Passenger(passengerId, title, firstName, lastName,
                    dateOfBirth, gender, nationality, passportNumber, seatNumber);
        }
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Passenger passenger = (Passenger) o;
        return Objects.equals(passengerId, passenger.passengerId) &&
                Objects.equals(firstName, passenger.firstName) &&
                Objects.equals(lastName, passenger.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passengerId, firstName, lastName);
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "passengerId='" + passengerId + '\'' +
                ", title='" + title + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
