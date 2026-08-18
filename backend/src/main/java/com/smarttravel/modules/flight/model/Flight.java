package com.smarttravel.modules.flight.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * MongoDB Document entity representing a scheduled flight in the catalog.
 */
@Document(collection = "flights")
@CompoundIndexes({
        @CompoundIndex(name = "flight_route_time_idx", def = "{'departureAirport.code': 1, 'arrivalAirport.code': 1, 'departureTime': 1, 'active': 1}"),
        @CompoundIndex(name = "flight_city_route_time_idx", def = "{'departureAirport.city': 1, 'arrivalAirport.city': 1, 'departureTime': 1, 'active': 1}"),
        @CompoundIndex(name = "flight_airline_active_idx", def = "{'airline': 1, 'active': 1}")
})
public class Flight {

    @Id
    private String id;

    @Indexed(unique = true)
    private String flightNumber;

    @Indexed
    private String airline;

    private String airlineCode;

    private AirportInfo departureAirport;

    private AirportInfo arrivalAirport;

    private Instant departureTime;

    private Instant arrivalTime;

    private Integer durationMinutes;

    private String aircraftModel;

    private BigDecimal basePrice;

    private int totalSeats;

    private int availableSeats;

    private Set<CabinClass> cabinClasses = new HashSet<>();

    private FlightStatus status = FlightStatus.SCHEDULED;

    private Integer delayMinutes;

    private String delayReason;

    private Instant revisedDepartureTime;

    private Instant estimatedArrival;

    private Instant lastStatusUpdated;

    private boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Flight() {
    }

    public Flight(String id, String flightNumber, String airline, String airlineCode,
                  AirportInfo departureAirport, AirportInfo arrivalAirport,
                  Instant departureTime, Instant arrivalTime, Integer durationMinutes,
                  String aircraftModel, BigDecimal basePrice, int totalSeats, int availableSeats,
                  Set<CabinClass> cabinClasses, FlightStatus status,
                  Integer delayMinutes, String delayReason, Instant revisedDepartureTime,
                  Instant estimatedArrival, Instant lastStatusUpdated, boolean active,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.airlineCode = airlineCode;
        this.departureAirport = departureAirport;
        this.arrivalAirport = arrivalAirport;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.durationMinutes = durationMinutes;
        this.aircraftModel = aircraftModel;
        this.basePrice = basePrice;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.cabinClasses = cabinClasses != null ? cabinClasses : new HashSet<>();
        this.status = status != null ? status : FlightStatus.SCHEDULED;
        this.delayMinutes = delayMinutes;
        this.delayReason = delayReason;
        this.revisedDepartureTime = revisedDepartureTime;
        this.estimatedArrival = estimatedArrival;
        this.lastStatusUpdated = lastStatusUpdated;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String flightNumber;
        private String airline;
        private String airlineCode;
        private AirportInfo departureAirport;
        private AirportInfo arrivalAirport;
        private Instant departureTime;
        private Instant arrivalTime;
        private Integer durationMinutes;
        private String aircraftModel;
        private BigDecimal basePrice;
        private int totalSeats;
        private int availableSeats;
        private Set<CabinClass> cabinClasses = new HashSet<>();
        private FlightStatus status = FlightStatus.SCHEDULED;
        private Integer delayMinutes;
        private String delayReason;
        private Instant revisedDepartureTime;
        private Instant estimatedArrival;
        private Instant lastStatusUpdated;
        private boolean active = true;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder flightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        public Builder airline(String airline) {
            this.airline = airline;
            return this;
        }

        public Builder airlineCode(String airlineCode) {
            this.airlineCode = airlineCode;
            return this;
        }

        public Builder departureAirport(AirportInfo departureAirport) {
            this.departureAirport = departureAirport;
            return this;
        }

        public Builder arrivalAirport(AirportInfo arrivalAirport) {
            this.arrivalAirport = arrivalAirport;
            return this;
        }

        public Builder departureTime(Instant departureTime) {
            this.departureTime = departureTime;
            return this;
        }

        public Builder arrivalTime(Instant arrivalTime) {
            this.arrivalTime = arrivalTime;
            return this;
        }

        public Builder durationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Builder aircraftModel(String aircraftModel) {
            this.aircraftModel = aircraftModel;
            return this;
        }

        public Builder basePrice(BigDecimal basePrice) {
            this.basePrice = basePrice;
            return this;
        }

        public Builder totalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        public Builder availableSeats(int availableSeats) {
            this.availableSeats = availableSeats;
            return this;
        }

        public Builder cabinClasses(Set<CabinClass> cabinClasses) {
            this.cabinClasses = cabinClasses;
            return this;
        }

        public Builder status(FlightStatus status) {
            this.status = status;
            return this;
        }

        public Builder delayMinutes(Integer delayMinutes) {
            this.delayMinutes = delayMinutes;
            return this;
        }

        public Builder delayReason(String delayReason) {
            this.delayReason = delayReason;
            return this;
        }

        public Builder revisedDepartureTime(Instant revisedDepartureTime) {
            this.revisedDepartureTime = revisedDepartureTime;
            return this;
        }

        public Builder estimatedArrival(Instant estimatedArrival) {
            this.estimatedArrival = estimatedArrival;
            return this;
        }

        public Builder lastStatusUpdated(Instant lastStatusUpdated) {
            this.lastStatusUpdated = lastStatusUpdated;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Flight build() {
            return new Flight(id, flightNumber, airline, airlineCode, departureAirport, arrivalAirport,
                    departureTime, arrivalTime, durationMinutes, aircraftModel, basePrice,
                    totalSeats, availableSeats, cabinClasses, status, delayMinutes, delayReason,
                    revisedDepartureTime, estimatedArrival, lastStatusUpdated, active, createdAt, updatedAt);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getAirlineCode() {
        return airlineCode;
    }

    public void setAirlineCode(String airlineCode) {
        this.airlineCode = airlineCode;
    }

    public AirportInfo getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(AirportInfo departureAirport) {
        this.departureAirport = departureAirport;
    }

    public AirportInfo getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(AirportInfo arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public Instant getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Instant departureTime) {
        this.departureTime = departureTime;
    }

    public Instant getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Instant arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getAircraftModel() {
        return aircraftModel;
    }

    public void setAircraftModel(String aircraftModel) {
        this.aircraftModel = aircraftModel;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Set<CabinClass> getCabinClasses() {
        return cabinClasses;
    }

    public void setCabinClasses(Set<CabinClass> cabinClasses) {
        this.cabinClasses = cabinClasses;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public Integer getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(Integer delayMinutes) {
        this.delayMinutes = delayMinutes;
    }

    public String getDelayReason() {
        return delayReason;
    }

    public void setDelayReason(String delayReason) {
        this.delayReason = delayReason;
    }

    public Instant getRevisedDepartureTime() {
        return revisedDepartureTime;
    }

    public void setRevisedDepartureTime(Instant revisedDepartureTime) {
        this.revisedDepartureTime = revisedDepartureTime;
    }

    public Instant getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(Instant estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public Instant getLastStatusUpdated() {
        return lastStatusUpdated;
    }

    public void setLastStatusUpdated(Instant lastStatusUpdated) {
        this.lastStatusUpdated = lastStatusUpdated;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return Objects.equals(id, flight.id) && Objects.equals(flightNumber, flight.flightNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, flightNumber);
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id='" + id + '\'' +
                ", flightNumber='" + flightNumber + '\'' +
                ", airline='" + airline + '\'' +
                ", status=" + status +
                ", delayMinutes=" + delayMinutes +
                ", departureTime=" + departureTime +
                ", arrivalTime=" + arrivalTime +
                ", active=" + active +
                '}';
    }
}
