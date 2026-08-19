package com.smarttravel.modules.notification.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Event categories that trigger customer communications.
 */
@Schema(description = "Customer Notification Event Type")
public enum NotificationType {
    FLIGHT_DELAYED,
    FLIGHT_CANCELLED,
    FLIGHT_RESCHEDULED,
    GATE_CHANGED,
    TERMINAL_CHANGED,
    AIRCRAFT_CHANGED,
    REFUND_REQUESTED,
    REFUND_COMPLETED,
    REFUND_FAILED,
    CHECK_IN_OPEN,
    BOARDING_REMINDER
}
