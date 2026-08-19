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
 * MongoDB Document entity representing an immutable customer notification record.
 */
@Document(collection = "notifications")
@CompoundIndexes({
        @CompoundIndex(name = "notification_user_created_idx", def = "{'userId': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "notification_user_read_idx", def = "{'userId': 1, 'read': 1}"),
        @CompoundIndex(name = "notification_status_retry_idx", def = "{'status': 1, 'retryCount': 1}")
})
public class Notification {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String bookingId;

    @Indexed
    private String flightId;

    private NotificationType notificationType;

    private NotificationChannel channel;

    private String recipient;

    private String subject;

    private String content;

    private NotificationStatus status = NotificationStatus.PENDING;

    private String providerMessageId;

    @Indexed(unique = true)
    private String idempotencyKey;

    private int retryCount = 0;

    private String failureReason;

    private boolean read = false;

    private Instant readAt;

    private Instant sentAt;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Notification() {
    }

    public Notification(String id, String userId, String bookingId, String flightId,
                        NotificationType notificationType, NotificationChannel channel,
                        String recipient, String subject, String content, NotificationStatus status,
                        String providerMessageId, String idempotencyKey, int retryCount,
                        String failureReason, boolean read, Instant readAt, Instant sentAt,
                        Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.notificationType = notificationType;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.status = status != null ? status : NotificationStatus.PENDING;
        this.providerMessageId = providerMessageId;
        this.idempotencyKey = idempotencyKey;
        this.retryCount = retryCount;
        this.failureReason = failureReason;
        this.read = read;
        this.readAt = readAt;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getFlightId() { return flightId; }
    public void setFlightId(String flightId) { this.flightId = flightId; }

    public NotificationType getNotificationType() { return notificationType; }
    public void setNotificationType(NotificationType notificationType) { this.notificationType = notificationType; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public String getProviderMessageId() { return providerMessageId; }
    public void setProviderMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private String id;
        private String userId;
        private String bookingId;
        private String flightId;
        private NotificationType notificationType;
        private NotificationChannel channel = NotificationChannel.EMAIL;
        private String recipient;
        private String subject;
        private String content;
        private NotificationStatus status = NotificationStatus.PENDING;
        private String providerMessageId;
        private String idempotencyKey;
        private int retryCount = 0;
        private String failureReason;
        private boolean read = false;
        private Instant readAt;
        private Instant sentAt;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder notificationType(NotificationType notificationType) { this.notificationType = notificationType; return this; }
        public Builder channel(NotificationChannel channel) { this.channel = channel; return this; }
        public Builder recipient(String recipient) { this.recipient = recipient; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder status(NotificationStatus status) { this.status = status; return this; }
        public Builder providerMessageId(String providerMessageId) { this.providerMessageId = providerMessageId; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder retryCount(int retryCount) { this.retryCount = retryCount; return this; }
        public Builder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public Builder read(boolean read) { this.read = read; return this; }
        public Builder readAt(Instant readAt) { this.readAt = readAt; return this; }
        public Builder sentAt(Instant sentAt) { this.sentAt = sentAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Notification build() {
            return new Notification(id, userId, bookingId, flightId, notificationType, channel,
                    recipient, subject, content, status, providerMessageId, idempotencyKey,
                    retryCount, failureReason, read, readAt, sentAt, createdAt, updatedAt);
        }
    }
}
