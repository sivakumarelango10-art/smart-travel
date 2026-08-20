package com.smarttravel.modules.flight.tracking.service;

import com.smarttravel.modules.flight.tracking.dto.TrackedFlightResponse;

import java.util.List;

/**
 * Service for managing user flight tracking subscriptions.
 */
public interface FlightTrackingService {

    /**
     * Track a flight for the current user. Idempotent — tracking an already tracked
     * flight returns the existing record.
     */
    TrackedFlightResponse trackFlight(String flightId, String userId);

    /**
     * Stop tracking a flight.
     */
    void untrackFlight(String flightId, String userId);

    /**
     * Get all currently tracked flights for a user, populated with live flight status.
     */
    List<TrackedFlightResponse> getTrackedFlights(String userId);

    /**
     * Check whether a user is tracking a specific flight.
     */
    boolean isTracking(String flightId, String userId);
}
