package com.smarttravel.modules.flight.simulation.model;

import com.smarttravel.modules.flight.model.FlightStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Event representation produced whenever a simulated status transition or delay occurs.
 * This will serve as the event payload for subsequent WebSocket/SSE distribution.
 */
public class FlightSimulationEvent {

    private String eventId;
    private String simulationId;
    private String flightId;
    private String flightNumber;
    private FlightStatus previousStatus;
    private FlightStatus newStatus;
    private Integer delayMinutes;
    private String delayReason;
    private Instant revisedDepartureTime;
    private Instant estimatedArrival;
    private Instant eventTime;

    public FlightSimulationEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.eventTime = Instant.now();
    }

    public FlightSimulationEvent(String eventId, String simulationId, String flightId,
                                 String flightNumber, FlightStatus previousStatus, FlightStatus newStatus,
                                 Integer delayMinutes, String delayReason,
                                 Instant revisedDepartureTime, Instant estimatedArrival, Instant eventTime) {
        this.eventId = eventId != null ? eventId : UUID.randomUUID().toString();
        this.simulationId = simulationId;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.delayMinutes = delayMinutes;
        this.delayReason = delayReason;
        this.revisedDepartureTime = revisedDepartureTime;
        this.estimatedArrival = estimatedArrival;
        this.eventTime = eventTime != null ? eventTime : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId = UUID.randomUUID().toString();
        private String simulationId;
        private String flightId;
        private String flightNumber;
        private FlightStatus previousStatus;
        private FlightStatus newStatus;
        private Integer delayMinutes;
        private String delayReason;
        private Instant revisedDepartureTime;
        private Instant estimatedArrival;
        private Instant eventTime = Instant.now();

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder simulationId(String simulationId) {
            this.simulationId = simulationId;
            return this;
        }

        public Builder flightId(String flightId) {
            this.flightId = flightId;
            return this;
        }

        public Builder flightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
            return this;
        }

        public Builder previousStatus(FlightStatus previousStatus) {
            this.previousStatus = previousStatus;
            return this;
        }

        public Builder newStatus(FlightStatus newStatus) {
            this.newStatus = newStatus;
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

        public Builder eventTime(Instant eventTime) {
            this.eventTime = eventTime;
            return this;
        }

        public FlightSimulationEvent build() {
            return new FlightSimulationEvent(eventId, simulationId, flightId, flightNumber,
                    previousStatus, newStatus, delayMinutes, delayReason,
                    revisedDepartureTime, estimatedArrival, eventTime);
        }
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(String simulationId) {
        this.simulationId = simulationId;
    }

    public String getFlightId() {
        return flightId;
    }

    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public FlightStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(FlightStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public FlightStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(FlightStatus newStatus) {
        this.newStatus = newStatus;
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

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    @Override
    public String toString() {
        return "FlightSimulationEvent{" +
                "eventId='" + eventId + '\'' +
                ", flightNumber='" + flightNumber + '\'' +
                ", transition=" + previousStatus + " -> " + newStatus +
                ", delayMinutes=" + delayMinutes +
                ", eventTime=" + eventTime +
                '}';
    }
}
