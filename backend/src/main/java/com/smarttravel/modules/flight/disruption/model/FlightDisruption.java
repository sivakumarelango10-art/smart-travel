package com.smarttravel.modules.flight.disruption.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Objects;

/**
 * MongoDB Document entity representing an immutable operational flight disruption event.
 */
@Document(collection = "flight_disruptions")
@CompoundIndexes({
        @CompoundIndex(name = "disruption_flight_created_idx", def = "{'flightId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "disruption_flight_status_idx", def = "{'flightId': 1, 'status': 1}")
})
public class FlightDisruption {

    @Id
    private String id;

    @Indexed
    private String flightId;

    @Indexed
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

    private DisruptionStatus status = DisruptionStatus.ACTIVE;

    private String createdBy;

    private Instant resolvedAt;

    private String resolvedBy;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public FlightDisruption() {
    }

    public FlightDisruption(String id, String flightId, String flightNumber, DisruptionType disruptionType,
                            String reason, String description, Instant previousDepartureTime,
                            Instant newDepartureTime, Instant previousArrivalTime, Instant newArrivalTime,
                            String previousGate, String newGate, String previousTerminal, String newTerminal,
                            String previousAircraftModel, String newAircraftModel, DisruptionStatus status,
                            String createdBy, Instant resolvedAt, String resolvedBy,
                            Instant createdAt, Instant updatedAt) {
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
        this.status = status != null ? status : DisruptionStatus.ACTIVE;
        this.createdBy = createdBy;
        this.resolvedAt = resolvedAt;
        this.resolvedBy = resolvedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

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
        private DisruptionStatus status = DisruptionStatus.ACTIVE;
        private String createdBy;
        private Instant resolvedAt;
        private String resolvedBy;
        private Instant createdAt;
        private Instant updatedAt;

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
        public Builder resolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; return this; }
        public Builder resolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public FlightDisruption build() {
            return new FlightDisruption(id, flightId, flightNumber, disruptionType, reason, description,
                    previousDepartureTime, newDepartureTime, previousArrivalTime, newArrivalTime,
                    previousGate, newGate, previousTerminal, newTerminal, previousAircraftModel,
                    newAircraftModel, status, createdBy, resolvedAt, resolvedBy, createdAt, updatedAt);
        }
    }
}
