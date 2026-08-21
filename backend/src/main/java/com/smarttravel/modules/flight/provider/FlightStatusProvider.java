package com.smarttravel.modules.flight.provider;

import com.smarttravel.modules.flight.model.FlightStatus;

import java.time.Instant;
import java.util.Optional;

/**
 * Architectural abstraction for flight status providers.
 * Decouples mock status simulation from future live aviation data providers (e.g. FlightAware, AviationStack).
 */
public interface FlightStatusProvider {

    /**
     * Human-readable identifier for the provider (e.g. "MOCK_SIMULATOR", "AVIATION_STACK").
     */
    String getProviderName();

    /**
     * Indicates whether this provider connects to live external aviation feeds or internal simulation.
     */
    boolean isLiveProvider();

    /**
     * Queries the provider for latest flight tracking state.
     */
    Optional<FlightStatusSnapshot> fetchLatestStatus(String flightNumber, Instant scheduledDeparture);

    record FlightStatusSnapshot(
            String flightNumber,
            FlightStatus status,
            Integer delayMinutes,
            String delayReason,
            Instant revisedDepartureTime,
            Instant revisedArrivalTime,
            String gate,
            String terminal,
            String updatedSource,
            String airline,
            String airlineCode,
            String originCode,
            String originCity,
            String originName,
            String destCode,
            String destCity,
            String destName,
            Instant scheduledDeparture,
            Instant scheduledArrival,
            String aircraftModel,
            Integer altitudeFeet,
            Integer groundSpeedKmph,
            Double progressPercent,
            String baggageCarousel,
            Double originLat,
            Double originLng,
            Double destLat,
            Double destLng,
            Double currentLat,
            Double currentLng,
            String flightId
    ) {
        public FlightStatusSnapshot(
                String flightNumber,
                FlightStatus status,
                Integer delayMinutes,
                String delayReason,
                Instant revisedDepartureTime,
                Instant revisedArrivalTime,
                String gate,
                String terminal,
                String updatedSource
        ) {
            this(flightNumber, status, delayMinutes, delayReason, revisedDepartureTime, revisedArrivalTime,
                    gate, terminal, updatedSource, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null);
        }
    }
}
