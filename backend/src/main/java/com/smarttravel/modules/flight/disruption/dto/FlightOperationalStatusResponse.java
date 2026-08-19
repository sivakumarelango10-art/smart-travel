package com.smarttravel.modules.flight.disruption.dto;

import com.smarttravel.modules.flight.model.FlightStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Flight Operational Status and Active Disruptions")
public class FlightOperationalStatusResponse {

    @Schema(description = "Flight MongoDB ID", example = "66c1e101f1a2b3c4d5e6f702")
    private String flightId;

    @Schema(description = "Flight Number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Airline", example = "Air India")
    private String airline;

    @Schema(description = "Scheduled Departure Time (UTC)")
    private Instant scheduledDepartureTime;

    @Schema(description = "Scheduled Arrival Time (UTC)")
    private Instant scheduledArrivalTime;

    @Schema(description = "Operational Revised Departure Time (UTC)")
    private Instant revisedDepartureTime;

    @Schema(description = "Operational Estimated Arrival Time (UTC)")
    private Instant estimatedArrivalTime;

    @Schema(description = "Current Operational Flight Status", example = "SCHEDULED")
    private FlightStatus status;

    @Schema(description = "Delay in minutes if applicable", example = "45")
    private Integer delayMinutes;

    @Schema(description = "Delay reason if applicable", example = "Air Traffic Control congestion")
    private String delayReason;

    @Schema(description = "Operational Departure Gate", example = "12A")
    private String gate;

    @Schema(description = "Operational Departure Terminal", example = "T3")
    private String terminal;

    @Schema(description = "Operational Aircraft Model", example = "Boeing 737 MAX 8")
    private String aircraftModel;

    @Schema(description = "Operational notes", example = "Boarding starts 40m prior to revised departure")
    private String operationalNotes;

    @Schema(description = "Timestamp of last operational update")
    private Instant lastOperationalUpdate;

    @Schema(description = "List of active or recent disruptions")
    private List<FlightDisruptionDto> disruptions;

    public FlightOperationalStatusResponse() {
    }

    public FlightOperationalStatusResponse(String flightId, String flightNumber, String airline,
                                           Instant scheduledDepartureTime, Instant scheduledArrivalTime,
                                           Instant revisedDepartureTime, Instant estimatedArrivalTime,
                                           FlightStatus status, Integer delayMinutes, String delayReason,
                                           String gate, String terminal, String aircraftModel,
                                           String operationalNotes, Instant lastOperationalUpdate,
                                           List<FlightDisruptionDto> disruptions) {
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.airline = airline;
        this.scheduledDepartureTime = scheduledDepartureTime;
        this.scheduledArrivalTime = scheduledArrivalTime;
        this.revisedDepartureTime = revisedDepartureTime;
        this.estimatedArrivalTime = estimatedArrivalTime;
        this.status = status;
        this.delayMinutes = delayMinutes;
        this.delayReason = delayReason;
        this.gate = gate;
        this.terminal = terminal;
        this.aircraftModel = aircraftModel;
        this.operationalNotes = operationalNotes;
        this.lastOperationalUpdate = lastOperationalUpdate;
        this.disruptions = disruptions;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public Instant getScheduledDepartureTime() { return scheduledDepartureTime; }
    public void setScheduledDepartureTime(Instant scheduledDepartureTime) { this.scheduledDepartureTime = scheduledDepartureTime; }

    public Instant getScheduledArrivalTime() { return scheduledArrivalTime; }
    public void setScheduledArrivalTime(Instant scheduledArrivalTime) { this.scheduledArrivalTime = scheduledArrivalTime; }

    public Instant getRevisedDepartureTime() { return revisedDepartureTime; }
    public void setRevisedDepartureTime(Instant revisedDepartureTime) { this.revisedDepartureTime = revisedDepartureTime; }

    public Instant getEstimatedArrivalTime() { return estimatedArrivalTime; }
    public void setEstimatedArrivalTime(Instant estimatedArrivalTime) { this.estimatedArrivalTime = estimatedArrivalTime; }

    public FlightStatus getStatus() { return status; }
    public void setStatus(FlightStatus status) { this.status = status; }

    public Integer getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }

    public String getDelayReason() { return delayReason; }
    public void setDelayReason(String delayReason) { this.delayReason = delayReason; }

    public String getGate() { return gate; }
    public void setGate(String gate) { this.gate = gate; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public String getAircraftModel() { return aircraftModel; }
    public void setAircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; }

    public String getOperationalNotes() { return operationalNotes; }
    public void setOperationalNotes(String operationalNotes) { this.operationalNotes = operationalNotes; }

    public Instant getLastOperationalUpdate() { return lastOperationalUpdate; }
    public void setLastOperationalUpdate(Instant lastOperationalUpdate) { this.lastOperationalUpdate = lastOperationalUpdate; }

    public List<FlightDisruptionDto> getDisruptions() { return disruptions; }
    public void setDisruptions(List<FlightDisruptionDto> disruptions) { this.disruptions = disruptions; }

    public static class Builder {
        private String flightId;
        private String flightNumber;
        private String airline;
        private Instant scheduledDepartureTime;
        private Instant scheduledArrivalTime;
        private Instant revisedDepartureTime;
        private Instant estimatedArrivalTime;
        private FlightStatus status;
        private Integer delayMinutes;
        private String delayReason;
        private String gate;
        private String terminal;
        private String aircraftModel;
        private String operationalNotes;
        private Instant lastOperationalUpdate;
        private List<FlightDisruptionDto> disruptions;

        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder airline(String airline) { this.airline = airline; return this; }
        public Builder scheduledDepartureTime(Instant scheduledDepartureTime) { this.scheduledDepartureTime = scheduledDepartureTime; return this; }
        public Builder scheduledArrivalTime(Instant scheduledArrivalTime) { this.scheduledArrivalTime = scheduledArrivalTime; return this; }
        public Builder revisedDepartureTime(Instant revisedDepartureTime) { this.revisedDepartureTime = revisedDepartureTime; return this; }
        public Builder estimatedArrivalTime(Instant estimatedArrivalTime) { this.estimatedArrivalTime = estimatedArrivalTime; return this; }
        public Builder status(FlightStatus status) { this.status = status; return this; }
        public Builder delayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; return this; }
        public Builder delayReason(String delayReason) { this.delayReason = delayReason; return this; }
        public Builder gate(String gate) { this.gate = gate; return this; }
        public Builder terminal(String terminal) { this.terminal = terminal; return this; }
        public Builder aircraftModel(String aircraftModel) { this.aircraftModel = aircraftModel; return this; }
        public Builder operationalNotes(String operationalNotes) { this.operationalNotes = operationalNotes; return this; }
        public Builder lastOperationalUpdate(Instant lastOperationalUpdate) { this.lastOperationalUpdate = lastOperationalUpdate; return this; }
        public Builder disruptions(List<FlightDisruptionDto> disruptions) { this.disruptions = disruptions; return this; }

        public FlightOperationalStatusResponse build() {
            return new FlightOperationalStatusResponse(flightId, flightNumber, airline, scheduledDepartureTime,
                    scheduledArrivalTime, revisedDepartureTime, estimatedArrivalTime, status, delayMinutes,
                    delayReason, gate, terminal, aircraftModel, operationalNotes, lastOperationalUpdate, disruptions);
        }
    }
}
