package com.smarttravel.modules.flight.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lifecycle status of a physical seat on a flight.
 */
@Schema(description = "Seat Status Lifecycle")
public enum SeatStatus {
    AVAILABLE,
    HELD,
    BOOKED,
    BLOCKED
}
