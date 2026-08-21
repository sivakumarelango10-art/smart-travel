package com.smarttravel.modules.flight.websocket;

import com.smarttravel.modules.flight.model.FlightStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * WebSocket event payload broadcast to /topic/flight-status/{flightId}
 * whenever a flight's operational status changes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlightStatusEvent {

    private String eventId;
    private String flightId;
    private String flightNumber;
    private FlightStatus previousStatus;
    private FlightStatus status;
    private Integer delayMinutes;
    private String delayReason;
    private Instant scheduledDeparture;
    private Instant revisedDeparture;
    private Instant scheduledArrival;
    private Instant estimatedArrival;
    private String gate;
    private String terminal;
    private Instant updatedAt;
    private String source;

    public FlightStatusEvent() {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.updatedAt = Instant.now();
        this.source = "SIMULATION";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId = java.util.UUID.randomUUID().toString();
        private String flightId;
        private String flightNumber;
        private FlightStatus previousStatus;
        private FlightStatus status;
        private Integer delayMinutes;
        private String delayReason;
        private Instant scheduledDeparture;
        private Instant revisedDeparture;
        private Instant scheduledArrival;
        private Instant estimatedArrival;
        private String gate;
        private String terminal;
        private Instant updatedAt = Instant.now();
        private String source = "SIMULATION";

        public Builder eventId(String eventId) { this.eventId = eventId; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder previousStatus(FlightStatus previousStatus) { this.previousStatus = previousStatus; return this; }
        public Builder status(FlightStatus status) { this.status = status; return this; }
        public Builder delayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; return this; }
        public Builder delayReason(String delayReason) { this.delayReason = delayReason; return this; }
        public Builder scheduledDeparture(Instant scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; return this; }
        public Builder revisedDeparture(Instant revisedDeparture) { this.revisedDeparture = revisedDeparture; return this; }
        public Builder scheduledArrival(Instant scheduledArrival) { this.scheduledArrival = scheduledArrival; return this; }
        public Builder estimatedArrival(Instant estimatedArrival) { this.estimatedArrival = estimatedArrival; return this; }
        public Builder gate(String gate) { this.gate = gate; return this; }
        public Builder terminal(String terminal) { this.terminal = terminal; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder source(String source) { this.source = source; return this; }

        public FlightStatusEvent build() {
            FlightStatusEvent e = new FlightStatusEvent();
            e.eventId = eventId;
            e.flightId = flightId;
            e.flightNumber = flightNumber;
            e.previousStatus = previousStatus;
            e.status = status;
            e.delayMinutes = delayMinutes;
            e.delayReason = delayReason;
            e.scheduledDeparture = scheduledDeparture;
            e.revisedDeparture = revisedDeparture;
            e.scheduledArrival = scheduledArrival;
            e.estimatedArrival = estimatedArrival;
            e.gate = gate;
            e.terminal = terminal;
            e.updatedAt = updatedAt;
            e.source = source;
            return e;
        }
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public FlightStatus getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(FlightStatus previousStatus) { this.previousStatus = previousStatus; }
    public FlightStatus getStatus() { return status; }
    public void setStatus(FlightStatus status) { this.status = status; }
    public Integer getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }
    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }
    public Instant getScheduledDeparture() { return scheduledDeparture; }
    public void setScheduledDeparture(Instant scheduledDeparture) { this.scheduledDeparture = scheduledDeparture; }
    public Instant getRevisedDeparture() { return revisedDeparture; }
    public void setRevisedDeparture(Instant revisedDeparture) { this.revisedDeparture = revisedDeparture; }
    public Instant getScheduledArrival() { return scheduledArrival; }
    public void setScheduledArrival(Instant scheduledArrival) { this.scheduledArrival = scheduledArrival; }
    public Instant getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(Instant estimatedArrival) { this.estimatedArrival = estimatedArrival; }
    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }
    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
