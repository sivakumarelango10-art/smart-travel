package com.smarttravel.modules.flight.provider.aviationstack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single flight record from Aviationstack.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackFlightItem {

    @JsonProperty("flight_date")
    private String flightDate;

    @JsonProperty("flight_status")
    private String flightStatus;

    @JsonProperty("departure")
    private AviationstackAirport departure;

    @JsonProperty("arrival")
    private AviationstackAirport arrival;

    @JsonProperty("airline")
    private AviationstackAirline airline;

    @JsonProperty("flight")
    private AviationstackFlightInfo flight;

    @JsonProperty("aircraft")
    private AviationstackAircraft aircraft;

    @JsonProperty("live")
    private AviationstackLive live;

    public AviationstackFlightItem() {
    }

    public String getFlightDate() {
        return flightDate;
    }

    public void setFlightDate(String flightDate) {
        this.flightDate = flightDate;
    }

    public String getFlightStatus() {
        return flightStatus;
    }

    public void setFlightStatus(String flightStatus) {
        this.flightStatus = flightStatus;
    }

    public AviationstackAirport getDeparture() {
        return departure;
    }

    public void setDeparture(AviationstackAirport departure) {
        this.departure = departure;
    }

    public AviationstackAirport getArrival() {
        return arrival;
    }

    public void setArrival(AviationstackAirport arrival) {
        this.arrival = arrival;
    }

    public AviationstackAirline getAirline() {
        return airline;
    }

    public void setAirline(AviationstackAirline airline) {
        this.airline = airline;
    }

    public AviationstackFlightInfo getFlight() {
        return flight;
    }

    public void setFlight(AviationstackFlightInfo flight) {
        this.flight = flight;
    }

    public AviationstackAircraft getAircraft() {
        return aircraft;
    }

    public void setAircraft(AviationstackAircraft aircraft) {
        this.aircraft = aircraft;
    }

    public AviationstackLive getLive() {
        return live;
    }

    public void setLive(AviationstackLive live) {
        this.live = live;
    }
}
