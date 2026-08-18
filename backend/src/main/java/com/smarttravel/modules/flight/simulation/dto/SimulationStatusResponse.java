package com.smarttravel.modules.flight.simulation.dto;

import com.smarttravel.modules.flight.model.FlightStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Flight Simulation Status Response Payload")
public class SimulationStatusResponse {

    @Schema(description = "Simulation configuration ID", example = "66c1e101f1a2b3c4d5e6f799")
    private String simulationId;

    @Schema(description = "Associated Flight ID", example = "66c1e101f1a2b3c4d5e6f702")
    private String flightId;

    @Schema(description = "Flight number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Whether simulation is actively running", example = "true")
    private boolean enabled;

    @Schema(description = "Current operational flight status", example = "BOARDING")
    private FlightStatus currentStatus;

    @Schema(description = "Configured simulation speed multiplier", example = "60")
    private int speedMultiplier;

    @Schema(description = "Configured delay probability", example = "0.25")
    private double delayProbability;

    @Schema(description = "Current delay in minutes", example = "45")
    private Integer delayMinutes;

    @Schema(description = "Reason for delay", example = "Weather conditions at departure airport")
    private String delayReason;

    @Schema(description = "Revised departure timestamp", example = "2026-08-20T19:15:00Z")
    private Instant revisedDepartureTime;

    @Schema(description = "Estimated arrival timestamp", example = "2026-08-20T21:30:00Z")
    private Instant estimatedArrival;

    @Schema(description = "Simulation start timestamp", example = "2026-08-18T10:00:00Z")
    private Instant startTime;

    @Schema(description = "Timestamp of last status transition", example = "2026-08-18T10:05:00Z")
    private Instant lastTransitionAt;

    @Schema(description = "Whether flight has completed its lifecycle (ARRIVED, CANCELLED, DIVERTED)", example = "false")
    private boolean completed;

    public SimulationStatusResponse() {
    }

    public SimulationStatusResponse(String simulationId, String flightId, String flightNumber, boolean enabled,
                                    FlightStatus currentStatus, int speedMultiplier, double delayProbability,
                                    Integer delayMinutes, String delayReason, Instant revisedDepartureTime,
                                    Instant estimatedArrival, Instant startTime, Instant lastTransitionAt,
                                    boolean completed) {
        this.simulationId = simulationId;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.enabled = enabled;
        this.currentStatus = currentStatus;
        this.speedMultiplier = speedMultiplier;
        this.delayProbability = delayProbability;
        this.delayMinutes = delayMinutes;
        this.delayReason = delayReason;
        this.revisedDepartureTime = revisedDepartureTime;
        this.estimatedArrival = estimatedArrival;
        this.startTime = startTime;
        this.lastTransitionAt = lastTransitionAt;
        this.completed = completed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String simulationId;
        private String flightId;
        private String flightNumber;
        private boolean enabled;
        private FlightStatus currentStatus;
        private int speedMultiplier;
        private double delayProbability;
        private Integer delayMinutes;
        private String delayReason;
        private Instant revisedDepartureTime;
        private Instant estimatedArrival;
        private Instant startTime;
        private Instant lastTransitionAt;
        private boolean completed;

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

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder currentStatus(FlightStatus currentStatus) {
            this.currentStatus = currentStatus;
            return this;
        }

        public Builder speedMultiplier(int speedMultiplier) {
            this.speedMultiplier = speedMultiplier;
            return this;
        }

        public Builder delayProbability(double delayProbability) {
            this.delayProbability = delayProbability;
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

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder lastTransitionAt(Instant lastTransitionAt) {
            this.lastTransitionAt = lastTransitionAt;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
            return this;
        }

        public SimulationStatusResponse build() {
            return new SimulationStatusResponse(simulationId, flightId, flightNumber, enabled,
                    currentStatus, speedMultiplier, delayProbability, delayMinutes, delayReason,
                    revisedDepartureTime, estimatedArrival, startTime, lastTransitionAt, completed);
        }
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public FlightStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(FlightStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public int getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSpeedMultiplier(int speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public double getDelayProbability() {
        return delayProbability;
    }

    public void setDelayProbability(double delayProbability) {
        this.delayProbability = delayProbability;
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

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getLastTransitionAt() {
        return lastTransitionAt;
    }

    public void setLastTransitionAt(Instant lastTransitionAt) {
        this.lastTransitionAt = lastTransitionAt;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
