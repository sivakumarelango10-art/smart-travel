package com.smarttravel.modules.flight.disruption.service;

import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.disruption.dto.FlightAircraftChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightCancelRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightDisruptionDto;
import com.smarttravel.modules.flight.disruption.dto.FlightGateChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.dto.FlightScheduleChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightTerminalChangeRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service managing operational flight schedule changes, cancellations, equipment/gate changes,
 * disruption history, impact evaluation, and passenger notifications.
 */
public interface FlightDisruptionService {

    /**
     * Reschedules an active flight while preserving immutable published schedule timestamps.
     */
    FlightOperationalStatusResponse rescheduleFlight(String flightId, FlightScheduleChangeRequest request, String adminUser);

    /**
     * Operationally cancels a flight, enforces FlightStateMachine rules, cancels active bookings,
     * releases physical seats, initiates eligible refunds, and alerts passengers.
     */
    FlightOperationalStatusResponse cancelFlight(String flightId, FlightCancelRequest request, String adminUser);

    /**
     * Updates departure gate for a flight and alerts affected passengers.
     */
    FlightOperationalStatusResponse updateGate(String flightId, FlightGateChangeRequest request, String adminUser);

    /**
     * Updates departure terminal for a flight and alerts affected passengers.
     */
    FlightOperationalStatusResponse updateTerminal(String flightId, FlightTerminalChangeRequest request, String adminUser);

    /**
     * Swaps aircraft equipment with cabin seat compatibility verification.
     */
    FlightOperationalStatusResponse changeAircraft(String flightId, FlightAircraftChangeRequest request, String adminUser);

    /**
     * Marks an active operational disruption as resolved.
     */
    FlightDisruptionDto resolveDisruption(String disruptionId, String adminUser);

    /**
     * Retrieves full operational status and disruption history for a flight.
     */
    FlightOperationalStatusResponse getFlightOperationalStatus(String flightId);

    /**
     * Retrieves paginated disruption history for a flight.
     */
    PageResponse<FlightDisruptionDto> getFlightDisruptions(String flightId, Pageable pageable);

    /**
     * Retrieves disruptions for a specific customer booking.
     */
    List<FlightDisruptionDto> getDisruptionsForBooking(String bookingId, String userId, boolean isAdmin);
}
