package com.smarttravel.modules.flight.disruption.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Lifecycle status of a flight disruption record.
 */
@Schema(description = "Flight Disruption Status")
public enum DisruptionStatus {
    ACTIVE,
    RESOLVED,
    CANCELLED
}
