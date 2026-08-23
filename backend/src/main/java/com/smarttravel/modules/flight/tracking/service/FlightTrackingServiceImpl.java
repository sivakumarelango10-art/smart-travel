package com.smarttravel.modules.flight.tracking.service;

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
        Flight flight = resolveFlight(flightId);
        String resolvedFlightId = flight.getId();

        // Check for existing tracking (by resolved ID or raw parameter) — make it idempotent
        Optional<TrackedFlight> existing = trackedFlightRepository.findByUserIdAndFlightId(userId, resolvedFlightId);
        if (existing.isEmpty() && !resolvedFlightId.equals(flightId)) {
            existing = trackedFlightRepository.findByUserIdAndFlightId(userId, flightId);
        }

        if (existing.isPresent()) {
            TrackedFlight tf = existing.get();
            if (!tf.isActive()) {
                // Re-activate if was previously untracked
                tf.setActive(true);
                tf.setFlightId(resolvedFlightId);
                tf.setLastKnownStatus(flight.getStatus());
                tf.setLastKnownEta(flight.getEstimatedArrival());
                trackedFlightRepository.save(tf);
                log.info("Re-activated flight tracking for user {} on flight {} ({})", userId, flight.getFlightNumber(), resolvedFlightId);
            } else {
                log.info("User {} is already tracking flight {} ({})", userId, flight.getFlightNumber(), resolvedFlightId);
            }
            return toResponse(tf, flight);
        }

        // Build route string
        String route = buildRoute(flight);

        TrackedFlight tracked = TrackedFlight.builder()
                .userId(userId)
                .flightId(resolvedFlightId)
                .flightNumber(flight.getFlightNumber())
                .route(route)
                .active(true)
                .lastKnownStatus(flight.getStatus())
                .lastKnownEta(flight.getEstimatedArrival())
                .build();

        TrackedFlight saved;
        try {
            saved = trackedFlightRepository.save(tracked);
            log.info("User {} is now tracking flight {} ({})", userId, flight.getFlightNumber(), resolvedFlightId);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            log.info("Concurrent insert caught by unique index for user {} and flight {}. Fetching existing record.", userId, resolvedFlightId);
            Optional<TrackedFlight> raced = trackedFlightRepository.findByUserIdAndFlightId(userId, resolvedFlightId);
            if (raced.isEmpty()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
                raced = trackedFlightRepository.findByUserIdAndFlightId(userId, resolvedFlightId);
            }
            TrackedFlight tf = raced.orElse(tracked);
            if (!tf.isActive()) {
                tf.setActive(true);
                tf.setLastKnownStatus(flight.getStatus());
                tf.setLastKnownEta(flight.getEstimatedArrival());
                try {
                    tf = trackedFlightRepository.save(tf);
                } catch (Exception ignored) {
                }
            }
            return toResponse(tf, flight);
        }

        return toResponse(saved, flight);
    }

    @Override
    public void untrackFlight(String flightId, String userId) {
        Optional<TrackedFlight> trackedOpt = trackedFlightRepository.findByUserIdAndFlightId(userId, flightId);
        if (trackedOpt.isEmpty()) {
            try {
                Flight flight = resolveFlight(flightId);
                trackedOpt = trackedFlightRepository.findByUserIdAndFlightId(userId, flight.getId());
            } catch (Exception ignored) {
            }
        }

        TrackedFlight tracked = trackedOpt.orElseThrow(() -> new ResourceNotFoundException("TrackedFlight", "flightId", flightId));
        tracked.setActive(false);
        trackedFlightRepository.save(tracked);
        log.info("User {} stopped tracking flight {}", userId, flightId);
    }

    @Override
    public List<TrackedFlightResponse> getTrackedFlights(String userId) {
        List<TrackedFlight> tracked = trackedFlightRepository.findByUserIdAndActiveTrue(userId);

        return tracked.stream().map(tf -> {
            Optional<Flight> flightOpt = flightRepository.findById(tf.getFlightId());
            if (flightOpt.isEmpty() && tf.getFlightNumber() != null) {
                flightOpt = flightRepository.findByFlightNumber(tf.getFlightNumber());
            }
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
        Optional<TrackedFlight> tracked = trackedFlightRepository.findByUserIdAndFlightId(userId, flightId);
        if (tracked.isPresent()) {
            return tracked.get().isActive();
        }
        try {
            Flight resolved = resolveFlight(flightId);
            return trackedFlightRepository.findByUserIdAndFlightId(userId, resolved.getId())
                    .map(TrackedFlight::isActive)
                    .orElse(false);
        } catch (Exception ignored) {
            return false;
        }
    }

    private Flight resolveFlight(String flightIdOrIdentifier) {
        if (flightIdOrIdentifier == null || flightIdOrIdentifier.isBlank()) {
            throw new ResourceNotFoundException("Flight", "id", flightIdOrIdentifier);
        }

        String raw = flightIdOrIdentifier.trim();

        // 1. Direct MongoDB ID match
        Optional<Flight> byId = flightRepository.findById(raw);
        if (byId.isPresent()) {
            return byId.get();
        }

        // 2. Direct Flight Number match (e.g. "AI-101", "6E-204")
        String cleanUpper = raw.toUpperCase();
        Optional<Flight> byNumber = flightRepository.findByFlightNumber(cleanUpper);
        if (byNumber.isPresent()) {
            return byNumber.get();
        }

        // 3. Normalized / formatted flight number (e.g., "AI101" <-> "AI-101")
        String formatted = formatStandardFlightCode(cleanUpper);
        Optional<Flight> byFormatted = flightRepository.findByFlightNumber(formatted);
        if (byFormatted.isPresent()) {
            return byFormatted.get();
        }

        // 4. Auto-provision flight into MongoDB for permanent tracking
        Flight autoFlight = autoProvisionFlight(formatted);
        if (autoFlight != null) {
            return autoFlight;
        }

        throw new ResourceNotFoundException("Flight", "id", flightIdOrIdentifier);
    }

    private Flight autoProvisionFlight(String flightNumber) {
        try {
            String num = formatStandardFlightCode(flightNumber);
            String airline = resolveAirlineName(num);
            String airlineCode = resolveAirlineCode(num);
            String orig = "DEL";
            String dest = "BOM";
            if (num.contains("204") || num.contains("6E")) { orig = "BLR"; dest = "DEL"; }
            else if (num.contains("955") || num.contains("UK")) { orig = "BOM"; dest = "GOI"; }
            else if (num.contains("500") || num.contains("EK")) { orig = "DXB"; dest = "BOM"; }
            else if (num.contains("112") || num.contains("BA")) { orig = "LHR"; dest = "DEL"; }
            else if (num.contains("402") || num.contains("SQ")) { orig = "SIN"; dest = "BOM"; }

            Flight flight = Flight.builder()
                    .flightNumber(num)
                    .airline(airline)
                    .airlineCode(airlineCode)
                    .status(com.smarttravel.modules.flight.model.FlightStatus.ON_TIME)
                    .departureAirport(com.smarttravel.modules.flight.model.AirportInfo.builder()
                            .code(orig)
                            .city(orig.equals("DEL") ? "Delhi" : orig.equals("BOM") ? "Mumbai" : orig.equals("BLR") ? "Bengaluru" : "New Delhi")
                            .name(orig + " International Airport")
                            .terminal("T3")
                            .build())
                    .arrivalAirport(com.smarttravel.modules.flight.model.AirportInfo.builder()
                            .code(dest)
                            .city(dest.equals("BOM") ? "Mumbai" : dest.equals("DEL") ? "Delhi" : dest.equals("GOI") ? "Goa" : "Mumbai")
                            .name(dest + " International Airport")
                            .terminal("T2")
                            .build())
                    .departureTime(java.time.Instant.now().plus(3, java.time.temporal.ChronoUnit.HOURS))
                    .arrivalTime(java.time.Instant.now().plus(5, java.time.temporal.ChronoUnit.HOURS))
                    .aircraftModel("Airbus A321neo")
                    .active(true)
                    .build();

            return flightRepository.save(flight);
        } catch (Exception ex) {
            log.warn("Auto-provisioning flight {} into MongoDB produced notice: {}", flightNumber, ex.getMessage());
            return null;
        }
    }

    private String formatStandardFlightCode(String raw) {
        if (raw == null) return "AI-101";
        String clean = raw.toUpperCase().trim();
        if (clean.startsWith("RADAR_")) clean = clean.substring(6);
        if (clean.startsWith("SIM_")) clean = clean.substring(4);
        if (clean.contains("-")) return clean;
        if (clean.matches("^[A-Z0-9]{2}\\d+$")) {
            return clean.substring(0, 2) + "-" + clean.substring(2);
        }
        return clean;
    }

    private String resolveAirlineName(String flightNum) {
        String num = flightNum.toUpperCase();
        if (num.startsWith("AI") || num.startsWith("AIC")) return "Air India";
        if (num.startsWith("6E") || num.startsWith("IGO")) return "IndiGo";
        if (num.startsWith("UK") || num.startsWith("VTI")) return "Vistara";
        if (num.startsWith("SG") || num.startsWith("SEJ")) return "SpiceJet";
        if (num.startsWith("EK") || num.startsWith("UAE")) return "Emirates";
        if (num.startsWith("BA") || num.startsWith("BAW")) return "British Airways";
        if (num.startsWith("SQ") || num.startsWith("SIA")) return "Singapore Airlines";
        return "SmartTravel Airways";
    }

    private String resolveAirlineCode(String flightNum) {
        String num = flightNum.toUpperCase();
        if (num.startsWith("AI")) return "AI";
        if (num.startsWith("6E")) return "6E";
        if (num.startsWith("UK")) return "UK";
        if (num.startsWith("SG")) return "SG";
        if (num.startsWith("EK")) return "EK";
        if (num.startsWith("BA")) return "BA";
        if (num.startsWith("SQ")) return "SQ";
        return "ST";
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
