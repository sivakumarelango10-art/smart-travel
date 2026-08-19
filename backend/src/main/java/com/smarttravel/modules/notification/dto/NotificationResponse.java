package com.smarttravel.modules.notification.dto;

import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationStatus;
import com.smarttravel.modules.notification.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Customer Notification Payload Details")
public class NotificationResponse {

    @Schema(description = "Notification MongoDB ID", example = "66c1e101f1a2b3c4d5e6fc33")
    private String id;

    @Schema(description = "Customer User ID", example = "66c1e101f1a2b3c4d5e6f901")
    private String userId;

    @Schema(description = "Booking ID if associated", example = "66c1e101f1a2b3c4d5e6f801")
    private String bookingId;

    @Schema(description = "Flight ID if associated", example = "66c1e101f1a2b3c4d5e6f702")
    private String flightId;

    @Schema(description = "Notification Type", example = "FLIGHT_CANCELLED")
    private NotificationType notificationType;

    @Schema(description = "Delivery Channel", example = "EMAIL")
    private NotificationChannel channel;

    @Schema(description = "Notification Subject / Header", example = "Flight ST-601 Cancellation Notice")
    private String subject;

    @Schema(description = "Notification Content / Body")
    private String content;

    @Schema(description = "Delivery Status", example = "SENT")
    private NotificationStatus status;

    @Schema(description = "Whether customer has marked notification as read", example = "false")
    private boolean read;

    @Schema(description = "Timestamp when customer marked notification read")
    private Instant readAt;

    @Schema(description = "Timestamp when notification was created")
    private Instant createdAt;

    @Schema(description = "Timestamp when notification was sent")
    private Instant sentAt;

    public NotificationResponse() {
    }

    public NotificationResponse(String id, String userId, String bookingId, String flightId,
                                NotificationType notificationType, NotificationChannel channel,
                                String subject, String content, NotificationStatus status,
                                boolean read, Instant readAt, Instant createdAt, Instant sentAt) {
        this.id = id;
        this.userId = userId;
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.notificationType = notificationType;
        this.channel = channel;
        this.subject = subject;
        this.content = content;
        this.status = status;
        this.read = read;
        this.readAt = readAt;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
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

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public static class Builder {
        private String id;
        private String userId;
        private String bookingId;
        private String flightId;
        private NotificationType notificationType;
        private NotificationChannel channel;
        private String subject;
        private String content;
        private NotificationStatus status;
        private boolean read;
        private Instant readAt;
        private Instant createdAt;
        private Instant sentAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder notificationType(NotificationType notificationType) { this.notificationType = notificationType; return this; }
        public Builder channel(NotificationChannel channel) { this.channel = channel; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder status(NotificationStatus status) { this.status = status; return this; }
        public Builder read(boolean read) { this.read = read; return this; }
        public Builder readAt(Instant readAt) { this.readAt = readAt; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder sentAt(Instant sentAt) { this.sentAt = sentAt; return this; }

        public NotificationResponse build() {
            return new NotificationResponse(id, userId, bookingId, flightId, notificationType,
                    channel, subject, content, status, read, readAt, createdAt, sentAt);
        }
    }
}
