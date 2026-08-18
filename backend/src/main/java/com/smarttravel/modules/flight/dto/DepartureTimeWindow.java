package com.smarttravel.modules.flight.dto;

import java.time.LocalTime;

/**
 * Departure time window intervals for flight searches.
 */
public enum DepartureTimeWindow {
    ALL(LocalTime.MIN, LocalTime.MAX),
    EARLY_MORNING(LocalTime.of(0, 0), LocalTime.of(5, 59, 59)),    // 00:00 - 06:00
    MORNING(LocalTime.of(6, 0), LocalTime.of(11, 59, 59)),         // 06:00 - 12:00
    AFTERNOON(LocalTime.of(12, 0), LocalTime.of(17, 59, 59)),      // 12:00 - 18:00
    EVENING(LocalTime.of(18, 0), LocalTime.of(23, 59, 59));        // 18:00 - 24:00

    private final LocalTime startTime;
    private final LocalTime endTime;

    DepartureTimeWindow(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
