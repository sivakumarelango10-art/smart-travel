package com.smarttravel.modules.flight.model;

/**
 * Standard flight operational lifecycle status.
 */
public enum FlightStatus {
    SCHEDULED,
    BOARDING,
    ON_TIME,
    DELAYED,
    DEPARTED,
    ARRIVED,
    CANCELLED,
    DIVERTED
}
