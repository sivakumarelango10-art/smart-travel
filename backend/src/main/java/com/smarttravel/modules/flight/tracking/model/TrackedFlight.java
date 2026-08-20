package com.smarttravel.modules.flight.tracking.model;

import com.smarttravel.modules.flight.model.FlightStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a user's active flight tracking subscription.
 * Users can track multiple flights simultaneously.
 */
@Document(collection = "tracked_flights")
@CompoundIndexes({
        @CompoundIndex(name = "tracked_user_flight_unique_idx",
                def = "{'userId': 1, 'flightId': 1}",
                unique = true),
        @CompoundIndex(name = "tracked_flight_active_idx",
                def = "{'flightId': 1, 'active': 1}")
})
public class TrackedFlight {

    @Id
    private String id;

    private String userId;

    private String flightId;

    private String flightNumber;

    private String route; // e.g. "DEL → BOM"

    private boolean active = true;

    private FlightStatus lastKnownStatus;

    private Instant lastKnownEta;

    @CreatedDate
    private Instant trackedAt;

    @LastModifiedDate
    private Instant updatedAt;

    public TrackedFlight() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String userId;
        private String flightId;
        private String flightNumber;
        private String route;
        private boolean active = true;
        private FlightStatus lastKnownStatus;
        private Instant lastKnownEta;
        private Instant trackedAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder flightNumber(String flightNumber) { this.flightNumber = flightNumber; return this; }
        public Builder route(String route) { this.route = route; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder lastKnownStatus(FlightStatus lastKnownStatus) { this.lastKnownStatus = lastKnownStatus; return this; }
        public Builder lastKnownEta(Instant lastKnownEta) { this.lastKnownEta = lastKnownEta; return this; }
        public Builder trackedAt(Instant trackedAt) { this.trackedAt = trackedAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public TrackedFlight build() {
            TrackedFlight tf = new TrackedFlight();
            tf.id = id;
            tf.userId = userId;
            tf.flightId = flightId;
            tf.flightNumber = flightNumber;
            tf.route = route;
            tf.active = active;
            tf.lastKnownStatus = lastKnownStatus;
            tf.lastKnownEta = lastKnownEta;
            tf.trackedAt = trackedAt;
            tf.updatedAt = updatedAt;
            return tf;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public String getRoute() { return route; }
    public void setRoute(String route) { this.route = route; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public FlightStatus getLastKnownStatus() { return lastKnownStatus; }
    public void setLastKnownStatus(FlightStatus lastKnownStatus) { this.lastKnownStatus = lastKnownStatus; }
    public Instant getLastKnownEta() { return lastKnownEta; }
    public void setLastKnownEta(Instant lastKnownEta) { this.lastKnownEta = lastKnownEta; }
    public Instant getTrackedAt() { return trackedAt; }
    public void setTrackedAt(Instant trackedAt) { this.trackedAt = trackedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
