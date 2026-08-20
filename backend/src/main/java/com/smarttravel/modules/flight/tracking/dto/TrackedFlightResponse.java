package com.smarttravel.modules.flight.tracking.dto;

import com.smarttravel.modules.flight.model.FlightStatus;

import java.time.Instant;

/**
 * Response DTO for a tracked flight entry.
 */
public class TrackedFlightResponse {

    private String id;
    private String flightId;
    private String flightNumber;
    private String route;
    private boolean active;
    private FlightStatus lastKnownStatus;
    private Instant lastKnownEta;
    private Instant trackedAt;

    // Flight operational details (populated from Flight entity)
    private FlightStatus currentStatus;
    private Integer delayMinutes;
    private String delayReason;
    private Instant scheduledDeparture;
    private Instant revisedDeparture;
    private Instant scheduledArrival;
    private Instant estimatedArrival;
    private String departureAirportCode;
    private String arrivalAirportCode;
    private String departureAirportCity;
    private String arrivalAirportCity;

    public TrackedFlightResponse() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final TrackedFlightResponse r = new TrackedFlightResponse();
        public Builder id(String v) { r.id = v; return this; }
        public Builder flightId(String v) { r.flightId = v; return this; }
        public Builder flightNumber(String v) { r.flightNumber = v; return this; }
        public Builder route(String v) { r.route = v; return this; }
        public Builder active(boolean v) { r.active = v; return this; }
        public Builder lastKnownStatus(FlightStatus v) { r.lastKnownStatus = v; return this; }
        public Builder lastKnownEta(Instant v) { r.lastKnownEta = v; return this; }
        public Builder trackedAt(Instant v) { r.trackedAt = v; return this; }
        public Builder currentStatus(FlightStatus v) { r.currentStatus = v; return this; }
        public Builder delayMinutes(Integer v) { r.delayMinutes = v; return this; }
        public Builder delayReason(String v) { r.delayReason = v; return this; }
        public Builder scheduledDeparture(Instant v) { r.scheduledDeparture = v; return this; }
        public Builder revisedDeparture(Instant v) { r.revisedDeparture = v; return this; }
        public Builder scheduledArrival(Instant v) { r.scheduledArrival = v; return this; }
        public Builder estimatedArrival(Instant v) { r.estimatedArrival = v; return this; }
        public Builder departureAirportCode(String v) { r.departureAirportCode = v; return this; }
        public Builder arrivalAirportCode(String v) { r.arrivalAirportCode = v; return this; }
        public Builder departureAirportCity(String v) { r.departureAirportCity = v; return this; }
        public Builder arrivalAirportCity(String v) { r.arrivalAirportCity = v; return this; }
        public TrackedFlightResponse build() { return r; }
    }

    public String getId() { return id; }
    public String getFlightId() { return flightId; }
    public String getFlightNumber() { return flightNumber; }
    public String getRoute() { return route; }
    public boolean isActive() { return active; }
    public FlightStatus getLastKnownStatus() { return lastKnownStatus; }
    public Instant getLastKnownEta() { return lastKnownEta; }
    public Instant getTrackedAt() { return trackedAt; }
    public FlightStatus getCurrentStatus() { return currentStatus; }
    public Integer getDelayMinutes() { return delayMinutes; }
    public String getDelayReason() { return delayReason; }
    public Instant getScheduledDeparture() { return scheduledDeparture; }
    public Instant getRevisedDeparture() { return revisedDeparture; }
    public Instant getScheduledArrival() { return scheduledArrival; }
    public Instant getEstimatedArrival() { return estimatedArrival; }
    public String getDepartureAirportCode() { return departureAirportCode; }
    public String getArrivalAirportCode() { return arrivalAirportCode; }
    public String getDepartureAirportCity() { return departureAirportCity; }
    public String getArrivalAirportCity() { return arrivalAirportCity; }
}
