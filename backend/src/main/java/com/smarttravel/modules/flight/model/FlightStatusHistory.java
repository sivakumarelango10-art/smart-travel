package com.smarttravel.modules.flight.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document entity recording flight status transition history and audit trail.
 */
@Document(collection = "flight_status_histories")
@CompoundIndexes({
        @CompoundIndex(name = "flight_status_hist_time_idx", def = "{'flightId': 1, 'changedAt': -1}")
})
public class FlightStatusHistory {

    @Id
    private String id;

    @Indexed
    private String flightId;

    @Indexed
    private String flightNumber;

    private FlightStatus previousStatus;

    private FlightStatus newStatus;

    private Integer delayMinutes;

    private String delayReason;

    private Instant revisedDepartureTime;

    private Instant estimatedArrival;

    @CreatedDate
    private Instant changedAt;

    private String changedBy;

    public FlightStatusHistory() {
    }

    public FlightStatusHistory(String id, String flightId, String flightNumber,
                               FlightStatus previousStatus, FlightStatus newStatus,
                               Integer delayMinutes, String delayReason,
                               Instant revisedDepartureTime, Instant estimatedArrival,
                               Instant changedAt, String changedBy) {
        this.id = id;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.delayMinutes = delayMinutes;
        this.delayReason = delayReason;
        this.revisedDepartureTime = revisedDepartureTime;
        this.estimatedArrival = estimatedArrival;
        this.changedAt = changedAt != null ? changedAt : Instant.now();
        this.changedBy = changedBy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String flightId;
        private String flightNumber;
        private FlightStatus previousStatus;
        private FlightStatus newStatus;
        private Integer delayMinutes;
        private String delayReason;
        private Instant revisedDepartureTime;
        private Instant estimatedArrival;
        private Instant changedAt;
        private String changedBy;

        public Builder id(String id) {
            this.id = id;
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

        public Builder changedAt(Instant changedAt) {
            this.changedAt = changedAt;
            return this;
        }

        public Builder changedBy(String changedBy) {
            this.changedBy = changedBy;
            return this;
        }

        public FlightStatusHistory build() {
            return new FlightStatusHistory(id, flightId, flightNumber, previousStatus, newStatus,
                    delayMinutes, delayReason, revisedDepartureTime, estimatedArrival, changedAt, changedBy);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Instant getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(Instant changedAt) {
        this.changedAt = changedAt;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}
