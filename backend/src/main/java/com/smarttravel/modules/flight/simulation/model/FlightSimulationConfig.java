package com.smarttravel.modules.flight.simulation.model;

import com.smarttravel.modules.flight.model.FlightStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document entity holding the active simulation state and parameters for a specific flight.
 */
@Document(collection = "flight_simulations")
public class FlightSimulationConfig {

    @Id
    private String id;

    @Indexed(unique = true)
    private String flightId;

    @Indexed
    private String flightNumber;

    private boolean enabled = true;

    private FlightStatus currentStatus = FlightStatus.SCHEDULED;

    private int speedMultiplier = 60;

    private double delayProbability = 0.25;

    private int minDelayMinutes = 15;

    private int maxDelayMinutes = 120;

    private Instant startTime;

    private Instant lastTransitionAt;

    private Instant nextTransitionTime;

    private boolean completed = false;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public FlightSimulationConfig() {
    }

    public FlightSimulationConfig(String id, String flightId, String flightNumber, boolean enabled,
                                  FlightStatus currentStatus, int speedMultiplier, double delayProbability,
                                  int minDelayMinutes, int maxDelayMinutes, Instant startTime,
                                  Instant lastTransitionAt, Instant nextTransitionTime, boolean completed,
                                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.enabled = enabled;
        this.currentStatus = currentStatus != null ? currentStatus : FlightStatus.SCHEDULED;
        this.speedMultiplier = speedMultiplier;
        this.delayProbability = delayProbability;
        this.minDelayMinutes = minDelayMinutes;
        this.maxDelayMinutes = maxDelayMinutes;
        this.startTime = startTime != null ? startTime : Instant.now();
        this.lastTransitionAt = lastTransitionAt;
        this.nextTransitionTime = nextTransitionTime;
        this.completed = completed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String flightId;
        private String flightNumber;
        private boolean enabled = true;
        private FlightStatus currentStatus = FlightStatus.SCHEDULED;
        private int speedMultiplier = 60;
        private double delayProbability = 0.25;
        private int minDelayMinutes = 15;
        private int maxDelayMinutes = 120;
        private Instant startTime = Instant.now();
        private Instant lastTransitionAt;
        private Instant nextTransitionTime;
        private boolean completed = false;
        private Instant createdAt;
        private Instant updatedAt;

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

        public Builder minDelayMinutes(int minDelayMinutes) {
            this.minDelayMinutes = minDelayMinutes;
            return this;
        }

        public Builder maxDelayMinutes(int maxDelayMinutes) {
            this.maxDelayMinutes = maxDelayMinutes;
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

        public Builder nextTransitionTime(Instant nextTransitionTime) {
            this.nextTransitionTime = nextTransitionTime;
            return this;
        }

        public Builder completed(boolean completed) {
            this.completed = completed;
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

        public FlightSimulationConfig build() {
            return new FlightSimulationConfig(id, flightId, flightNumber, enabled, currentStatus,
                    speedMultiplier, delayProbability, minDelayMinutes, maxDelayMinutes,
                    startTime, lastTransitionAt, nextTransitionTime, completed, createdAt, updatedAt);
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

    public int getMinDelayMinutes() {
        return minDelayMinutes;
    }

    public void setMinDelayMinutes(int minDelayMinutes) {
        this.minDelayMinutes = minDelayMinutes;
    }

    public int getMaxDelayMinutes() {
        return maxDelayMinutes;
    }

    public void setMaxDelayMinutes(int maxDelayMinutes) {
        this.maxDelayMinutes = maxDelayMinutes;
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

    public Instant getNextTransitionTime() {
        return nextTransitionTime;
    }

    public void setNextTransitionTime(Instant nextTransitionTime) {
        this.nextTransitionTime = nextTransitionTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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
}
