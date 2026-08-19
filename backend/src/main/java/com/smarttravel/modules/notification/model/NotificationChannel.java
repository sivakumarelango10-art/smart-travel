package com.smarttravel.modules.notification.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Supported outbound customer delivery communication channels.
 */
@Schema(description = "Notification Delivery Channel")
public enum NotificationChannel {
    EMAIL,
    SMS,
    WHATSAPP,
    PUSH
}
