package com.smarttravel.modules.booking.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Status of online check-in operation.
 */
@Schema(description = "Check-in Status Lifecycle")
public enum CheckInStatus {
    COMPLETED,
    CANCELLED
}
