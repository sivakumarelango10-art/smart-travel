package com.smarttravel.modules.flight.tracking.service;

import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.tracking.dto.TrackedFlightResponse;
import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import com.smarttravel.modules.flight.tracking.repository.TrackedFlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of FlightTrackingService managing user flight tracking subscriptions.
 */
@Service
public class FlightTrackingServiceImpl implements FlightTrackingService {

    private static final Logger log = LoggerFactory.getLogger(FlightTrackingServiceImpl.class);

    private final TrackedFlightRepository trackedFlightRepository;
    private final FlightRepository flightRepository;

    public FlightTrackingServiceImpl(TrackedFlightRepository trackedFlightRepository,
                                     FlightRepository flightRepository) {
        this.trackedFlightRepository = trackedFlightRepository;
        this.flightRepository = flightRepository;
    }

    @Override
    public TrackedFlightResponse trackFlight(String flightId, String userId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        // Check for existing tracking — make it idempotent
        Optional<TrackedFlight> existing = trackedFlightRepository.findByUserIdAndFlightId(userId, flightId);
        if (existing.isPresent()) {
            TrackedFlight tf = existing.get();
            if (!tf.isActive()) {
                // Re-activate if was previously untracked
                tf.setActive(true);
                tf.setLastKnownStatus(flight.getStatus());
                tf.setLastKnownEta(flight.getEstimatedArrival());
                trackedFlightRepository.save(tf);
                log.info("Re-activated flight tracking for user {} on flight {}", userId, flightId);
            } else {
                log.info("User {} is already tracking flight {}", userId, flightId);
            }
            return toResponse(existing.get(), flight);
        }

        // Build route string
        String route = buildRoute(flight);

        TrackedFlight tracked = TrackedFlight.builder()
                .userId(userId)
                .flightId(flightId)
                .flightNumber(flight.getFlightNumber())
                .route(route)
                .active(true)
                .lastKnownStatus(flight.getStatus())
                .lastKnownEta(flight.getEstimatedArrival())
                .build();

        TrackedFlight saved = trackedFlightRepository.save(tracked);
        log.info("User {} is now tracking flight {} ({})", userId, flight.getFlightNumber(), flightId);

        return toResponse(saved, flight);
    }

    @Override
    public void untrackFlight(String flightId, String userId) {
        TrackedFlight tracked = trackedFlightRepository.findByUserIdAndFlightId(userId, flightId)
                .orElseThrow(() -> new ResourceNotFoundException("TrackedFlight", "flightId", flightId));

        tracked.setActive(false);
        trackedFlightRepository.save(tracked);
        log.info("User {} stopped tracking flight {}", userId, flightId);
    }

    @Override
    public List<TrackedFlightResponse> getTrackedFlights(String userId) {
        List<TrackedFlight> tracked = trackedFlightRepository.findByUserIdAndActiveTrue(userId);

        return tracked.stream().map(tf -> {
            Optional<Flight> flightOpt = flightRepository.findById(tf.getFlightId());
            if (flightOpt.isPresent()) {
                Flight flight = flightOpt.get();
                // Update last known status
                if (tf.getLastKnownStatus() != flight.getStatus()) {
                    tf.setLastKnownStatus(flight.getStatus());
                    tf.setLastKnownEta(flight.getEstimatedArrival());
                    trackedFlightRepository.save(tf);
                }
                return toResponse(tf, flight);
            }
            return toResponse(tf, null);
        }).collect(Collectors.toList());
    }

    @Override
    public boolean isTracking(String flightId, String userId) {
        return trackedFlightRepository.findByUserIdAndFlightId(userId, flightId)
                .map(TrackedFlight::isActive)
                .orElse(false);
    }

    private TrackedFlightResponse toResponse(TrackedFlight tf, Flight flight) {
        TrackedFlightResponse.Builder builder = TrackedFlightResponse.builder()
                .id(tf.getId())
                .flightId(tf.getFlightId())
                .flightNumber(tf.getFlightNumber())
                .route(tf.getRoute())
                .active(tf.isActive())
                .lastKnownStatus(tf.getLastKnownStatus())
                .lastKnownEta(tf.getLastKnownEta())
                .trackedAt(tf.getTrackedAt());

        if (flight != null) {
            builder.currentStatus(flight.getStatus())
                    .delayMinutes(flight.getDelayMinutes())
                    .delayReason(flight.getDelayReason())
                    .scheduledDeparture(flight.getDepartureTime())
                    .revisedDeparture(flight.getRevisedDepartureTime())
                    .scheduledArrival(flight.getArrivalTime())
                    .estimatedArrival(flight.getEstimatedArrival());

            if (flight.getDepartureAirport() != null) {
                builder.departureAirportCode(flight.getDepartureAirport().getCode())
                        .departureAirportCity(flight.getDepartureAirport().getCity());
            }
            if (flight.getArrivalAirport() != null) {
                builder.arrivalAirportCode(flight.getArrivalAirport().getCode())
                        .arrivalAirportCity(flight.getArrivalAirport().getCity());
            }
        }

        return builder.build();
    }

    private String buildRoute(Flight flight) {
        String dep = flight.getDepartureAirport() != null ? flight.getDepartureAirport().getCode() : "???";
        String arr = flight.getArrivalAirport() != null ? flight.getArrivalAirport().getCode() : "???";
        return dep + " → " + arr;
    }
}
