package com.smarttravel.modules.flight.disruption.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Categorization of operational flight disruptions.
 */
@Schema(description = "Flight Disruption Category")
public enum DisruptionType {
    DELAY,
    CANCELLATION,
    RESCHEDULE,
    GATE_CHANGE,
    TERMINAL_CHANGE,
    AIRCRAFT_CHANGE,
    OTHER
}
