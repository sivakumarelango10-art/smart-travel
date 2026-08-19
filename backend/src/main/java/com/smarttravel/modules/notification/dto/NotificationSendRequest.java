package com.smarttravel.modules.notification.dto;

import com.smarttravel.modules.notification.model.NotificationChannel;
import com.smarttravel.modules.notification.model.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Internal / Admin Notification Send Request")
public class NotificationSendRequest {

    @Schema(description = "Customer User ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "66c1e101f1a2b3c4d5e6f901")
    @NotBlank(message = "User ID is required")
    private String userId;

    @Schema(description = "Booking ID", example = "66c1e101f1a2b3c4d5e6f801")
    private String bookingId;

    @Schema(description = "Flight ID", example = "66c1e101f1a2b3c4d5e6f702")
    private String flightId;

    @Schema(description = "Notification Type", requiredMode = Schema.RequiredMode.REQUIRED, example = "FLIGHT_CANCELLED")
    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @Schema(description = "Notification Delivery Channel", defaultValue = "EMAIL")
    private NotificationChannel channel = NotificationChannel.EMAIL;

    @Schema(description = "Recipient Email or Phone number", example = "sarah@smarttravel.com")
    private String recipient;

    @Schema(description = "Notification Subject", requiredMode = Schema.RequiredMode.REQUIRED, example = "Flight ST-601 Disruption Notice")
    @NotBlank(message = "Subject is required")
    private String subject;

    @Schema(description = "Notification Body Content", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Content is required")
    private String content;

    @Schema(description = "Event ID or Unique Disruption ID for idempotency", requiredMode = Schema.RequiredMode.REQUIRED, example = "evt_66c1e101f1a2b3c4d5e6fa11")
    @NotBlank(message = "Event ID is required for idempotency")
    private String eventId;

    public NotificationSendRequest() {
    }

    public NotificationSendRequest(String userId, String bookingId, String flightId,
                                   NotificationType notificationType, NotificationChannel channel,
                                   String recipient, String subject, String content, String eventId) {
        this.userId = userId;
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.notificationType = notificationType;
        this.channel = channel != null ? channel : NotificationChannel.EMAIL;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.eventId = eventId;
    }

    public static Builder builder() {
        return new Builder();
    }

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

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public static class Builder {
        private String userId;
        private String bookingId;
        private String flightId;
        private NotificationType notificationType;
        private NotificationChannel channel = NotificationChannel.EMAIL;
        private String recipient;
        private String subject;
        private String content;
        private String eventId;

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder bookingId(String bookingId) { this.bookingId = bookingId; return this; }
        public Builder flightId(String flightId) { this.flightId = flightId; return this; }
        public Builder notificationType(NotificationType notificationType) { this.notificationType = notificationType; return this; }
        public Builder channel(NotificationChannel channel) { this.channel = channel; return this; }
        public Builder recipient(String recipient) { this.recipient = recipient; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder content(String content) { this.content = content; return this; }
        public Builder eventId(String eventId) { this.eventId = eventId; return this; }

        public NotificationSendRequest build() {
            return new NotificationSendRequest(userId, bookingId, flightId, notificationType, channel, recipient, subject, content, eventId);
        }
    }
}
