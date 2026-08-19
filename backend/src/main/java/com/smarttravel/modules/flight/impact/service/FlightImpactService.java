package com.smarttravel.modules.flight.impact.service;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.flight.impact.dto.FlightImpactSummaryDto;

import java.util.List;

/**
 * Service for assessing customer booking impact during operational disruptions.
 */
public interface FlightImpactService {

    /**
     * Calculates summary metrics of affected bookings and passengers.
     *
     * @param flightId Flight MongoDB ID
     * @return Disruption impact summary
     */
    FlightImpactSummaryDto getDisruptionImpactSummary(String flightId);

    /**
     * Retrieves all active confirmed bookings affected by a disruption.
     *
     * @param flightId Flight MongoDB ID
     * @return List of affected confirmed bookings
     */
    List<Booking> getAffectedConfirmedBookings(String flightId);
}
