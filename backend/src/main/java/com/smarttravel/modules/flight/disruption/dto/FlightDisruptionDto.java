package com.smarttravel.modules.flight.disruption.dto;

import com.smarttravel.modules.flight.disruption.model.DisruptionStatus;
import com.smarttravel.modules.flight.disruption.model.DisruptionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Flight Disruption Record Details")
public class FlightDisruptionDto {

    @Schema(description = "Disruption MongoDB ID", example = "66c1e101f1a2b3c4d5e6fa11")
    private String id;

    @Schema(description = "Flight ID", example = "66c1e101f1a2b3c4d5e6f702")
    private String flightId;

    @Schema(description = "Flight Number", example = "AI-101")
    private String flightNumber;

    @Schema(description = "Disruption Category", example = "DELAY")
    private DisruptionType disruptionType;

    @Schema(description = "Primary Reason", example = "Severe weather conditions at destination")
    private String reason;

    @Schema(description = "Detailed Description", example = "Fog at DEL causing temporary ground stop")
    private String description;

    @Schema(description = "Original Scheduled Departure Time")
    private Instant previousDepartureTime;

    @Schema(description = "Revised Departure Time")
    private Instant newDepartureTime;

    @Schema(description = "Original Scheduled Arrival Time")
    private Instant previousArrivalTime;

    @Schema(description = "Revised Arrival Time")
    private Instant newArrivalTime;

    @Schema(description = "Previous Departure Gate", example = "12A")
    private String previousGate;

    @Schema(description = "New Departure Gate", example = "14B")
    private String newGate;

    @Schema(description = "Previous Departure Terminal", example = "T3")
    private String previousTerminal;

    @Schema(description = "New Departure Terminal", example = "T2")
    private String newTerminal;

    @Schema(description = "Previous Aircraft Model", example = "Boeing 737 MAX 8")
    private String previousAircraftModel;

    @Schema(description = "New Aircraft Model", example = "Airbus A321neo")
    private String newAircraftModel;

    @Schema(description = "Disruption Status", example = "ACTIVE")
    private DisruptionStatus status;

    @Schema(description = "Admin who recorded disruption", example = "admin@smarttravel.com")
    private String createdBy;

    @Schema(description = "Timestamp when disruption was recorded")
    private Instant createdAt;

    @Schema(description = "Timestamp when disruption was marked resolved")
    private Instant resolvedAt;

    public FlightDisruptionDto() {
    }

    public FlightDisruptionDto(String id, String flightId, String flightNumber, DisruptionType disruptionType,
                               String reason, String description, Instant previousDepartureTime,
                               Instant newDepartureTime, Instant previousArrivalTime, Instant newArrivalTime,
                               String previousGate, String newGate, String previousTerminal, String newTerminal,
                               String previousAircraftModel, String newAircraftModel, DisruptionStatus status,
                               String createdBy, Instant createdAt, Instant resolvedAt) {
        this.id = id;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.disruptionType = disruptionType;
        this.reason = reason;
        this.description = description;
        this.previousDepartureTime = previousDepartureTime;
        this.newDepartureTime = newDepartureTime;
        this.previousArrivalTime = previousArrivalTime;
        this.newArrivalTime = newArrivalTime;
        this.previousGate = previousGate;
        this.newGate = newGate;
        this.previousTerminal = previousTerminal;
        this.newTerminal = newTerminal;
        this.previousAircraftModel = previousAircraftModel;
        this.newAircraftModel = newAircraftModel;
        this.status = status;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public DisruptionType getDisruptionType() { return disruptionType; }
    public void setDisruptionType(DisruptionType disruptionType) { this.disruptionType = disruptionType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getPreviousDepartureTime() { return previousDepartureTime; }
    public void setPreviousDepartureTime(Instant previousDepartureTime) { this.previousDepartureTime = previousDepartureTime; }

    public Instant getNewDepartureTime() { return newDepartureTime; }
    public void setNewDepartureTime(Instant newDepartureTime) { this.newDepartureTime = newDepartureTime; }

    public Instant getPreviousArrivalTime() { return previousArrivalTime; }
    public void setPreviousArrivalTime(Instant previousArrivalTime) { this.previousArrivalTime = previousArrivalTime; }

    public Instant getNewArrivalTime() { return newArrivalTime; }
    public void setNewArrivalTime(Instant newArrivalTime) { this.newArrivalTime = newArrivalTime; }

    public String getPreviousGate() { return previousGate; }
    public void setPreviousGate(String previousGate) { this.previousGate = previousGate; }

    public String getNewGate() { return newGate; }
    public void setNewGate(String newGate) { this.newGate = newGate; }

    public String getPreviousTerminal() { return previousTerminal; }
    public void setPreviousTerminal(String previousTerminal) { this.previousTerminal = previousTerminal; }

    public String getNewTerminal() { return newTerminal; }
    public void setNewTerminal(String newTerminal) { this.newTerminal = newTerminal; }

    public String getPreviousAircraftModel() { return previousAircraftModel; }
    public void setPreviousAircraftModel(String previousAircraftModel) { this.previousAircraftModel = previousAircraftModel; }

    public String getNewAircraftModel() { return newAircraftModel; }
    public void setNewAircraftModel(String newAircraftModel) { this.newAircraftModel = newAircraftModel; }

    public DisruptionStatus getStatus() { return status; }
    public void setStatus(DisruptionStatus status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public static class Builder {
        private String id;
        private String flightId;
        private String flightNumber;
        private DisruptionType disruptionType;
        private String reason;
        private String description;
        private Instant previousDepartureTime;
        private Instant newDepartureTime;
        private Instant previousArrivalTime;
        private Instant newArrivalTime;
        private String previousGate;
        private String newGate;
        private String previousTerminal;
        private String newTerminal;
        private String previousAircraftModel;
        private String newAircraftModel;
        private DisruptionStatus status;
        private String createdBy;
        private Instant createdAt;
        private Instant resolvedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder disruptionType(DisruptionType disruptionType) { this.disruptionType = disruptionType; return this; }
        public Builder reason(String reason) { this.reason = reason; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder previousDepartureTime(Instant previousDepartureTime) { this.previousDepartureTime = previousDepartureTime; return this; }
        public Builder newDepartureTime(Instant newDepartureTime) { this.newDepartureTime = newDepartureTime; return this; }
        public Builder previousArrivalTime(Instant previousArrivalTime) { this.previousArrivalTime = previousArrivalTime; return this; }
        public Builder newArrivalTime(Instant newArrivalTime) { this.newArrivalTime = newArrivalTime; return this; }
        public Builder previousGate(String previousGate) { this.previousGate = previousGate; return this; }
        public Builder newGate(String newGate) { this.newGate = newGate; return this; }
        public Builder previousTerminal(String previousTerminal) { this.previousTerminal = previousTerminal; return this; }
        public Builder newTerminal(String newTerminal) { this.newTerminal = newTerminal; return this; }
        public Builder previousAircraftModel(String previousAircraftModel) { this.previousAircraftModel = previousAircraftModel; return this; }
        public Builder newAircraftModel(String newAircraftModel) { this.newAircraftModel = newAircraftModel; return this; }
        public Builder status(DisruptionStatus status) { this.status = status; return this; }
        public Builder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder resolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public FlightDisruptionDto build() {
            return new FlightDisruptionDto(id, flightId, flightNumber, disruptionType, reason, description,
                    previousDepartureTime, newDepartureTime, previousArrivalTime, newArrivalTime,
                    previousGate, newGate, previousTerminal, newTerminal, previousAircraftModel,
                    newAircraftModel, status, createdBy, createdAt, resolvedAt);
        }
    }
}
