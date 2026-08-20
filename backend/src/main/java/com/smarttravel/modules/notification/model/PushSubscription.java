package com.smarttravel.modules.notification.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document storing a user's browser Web Push subscription (W3C Push API / VAPID).
 */
@Document(collection = "push_subscriptions")
@CompoundIndexes({
        @CompoundIndex(name = "push_user_endpoint_idx", def = "{'userId': 1, 'endpoint': 1}", unique = true),
        @CompoundIndex(name = "push_user_active_idx", def = "{'userId': 1, 'active': 1}")
})
public class PushSubscription {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String endpoint;

    private String p256dhKey;

    private String authKey;

    private String userAgent;

    private boolean active = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant lastUsedAt;

    public PushSubscription() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final PushSubscription s = new PushSubscription();
        public Builder id(String v) { s.id = v; return this; }
        public Builder userId(String v) { s.userId = v; return this; }
        public Builder endpoint(String v) { s.endpoint = v; return this; }
        public Builder p256dhKey(String v) { s.p256dhKey = v; return this; }
        public Builder authKey(String v) { s.authKey = v; return this; }
        public Builder userAgent(String v) { s.userAgent = v; return this; }
        public Builder active(boolean v) { s.active = v; return this; }
        public PushSubscription build() { return s; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getP256dhKey() { return p256dhKey; }
    public void setP256dhKey(String p256dhKey) { this.p256dhKey = p256dhKey; }
    public String getAuthKey() { return authKey; }
    public void setAuthKey(String authKey) { this.authKey = authKey; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
