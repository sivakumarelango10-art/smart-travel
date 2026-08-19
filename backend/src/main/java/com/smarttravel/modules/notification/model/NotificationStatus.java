package com.smarttravel.modules.notification.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dispatch and delivery status of a customer notification.
 */
@Schema(description = "Notification Delivery Status")
public enum NotificationStatus {
    PENDING,
    SENT,
    FAILED,
    DELIVERED
}
