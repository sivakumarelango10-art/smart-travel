package com.smarttravel.modules.recommendation.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * MongoDB document tracking user activity for recommendation signals.
 * Each interaction is a separate document (append-only event log).
 */
@Document(collection = "user_activities")
@CompoundIndexes({
        @CompoundIndex(name = "activity_user_type_idx", def = "{'userId': 1, 'activityType': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "activity_target_idx", def = "{'targetId': 1, 'activityType': 1}"),
        @CompoundIndex(name = "activity_user_recent_idx", def = "{'userId': 1, 'createdAt': -1}")
})
public class UserActivity {

    @Id
    private String id;

    @Indexed
    private String userId;

    private UserActivityType activityType;

    /** The entity being interacted with (flightId, hotelId, destination) */
    private String targetId;

    private String targetType; // FLIGHT, HOTEL, DESTINATION

    /** Metadata context (e.g., search params, cabin class, route) */
    private Map<String, Object> metadata;

    /** How long the user spent viewing (in seconds) — quality signal */
    private Integer durationSeconds;

    @CreatedDate
    private Instant createdAt;

    public UserActivity() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UserActivity r = new UserActivity();
        public Builder id(String v) { r.id = v; return this; }
        public Builder userId(String v) { r.userId = v; return this; }
        public Builder activityType(UserActivityType v) { r.activityType = v; return this; }
        public Builder targetId(String v) { r.targetId = v; return this; }
        public Builder targetType(String v) { r.targetType = v; return this; }
        public Builder metadata(Map<String, Object> v) { r.metadata = v; return this; }
        public Builder durationSeconds(Integer v) { r.durationSeconds = v; return this; }
        public Builder createdAt(Instant v) { r.createdAt = v; return this; }
        public UserActivity build() { return r; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public UserActivityType getActivityType() { return activityType; }
    public void setActivityType(UserActivityType activityType) { this.activityType = activityType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
