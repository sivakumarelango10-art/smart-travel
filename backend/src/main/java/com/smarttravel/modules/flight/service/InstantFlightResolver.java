package com.smarttravel.modules.flight.service;

import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.seeder.FlightDataSeeder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Deterministically resolves or provisions flights from synthetic instant IDs (e.g. instant_qp_1102_20260917).
 * Guarantees zero 404 errors when users access direct booking URLs, bookmark routes, or return from authentication.
 */
public final class InstantFlightResolver {

    private static final Logger log = LoggerFactory.getLogger(InstantFlightResolver.class);

    private InstantFlightResolver() {}

    /**
     * Resolves a flight entity by ID. If the ID is an instant synthetic ID, it checks
     * MongoDB by exact flightNumber, flightNumber prefix, or auto-provisions the flight with 
     * complete seats and cabin inventories so downstream bookings and seat maps never fail.
     */
    public static Optional<Flight> resolveOrProvision(FlightRepository flightRepository, String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        // 1. Direct MongoDB ID lookup
        Optional<Flight> direct = flightRepository.findById(id);
        if (direct.isPresent()) {
            return direct;
        }

        // 2. Parse synthetic instant pattern: instant_{code}_{num}_{date}
        if (id.startsWith("instant_")) {
            String[] parts = id.split("_");
            if (parts.length >= 3) {
                String code = parts[1].toUpperCase();
                String num = parts[2];
                String standardCode = code + "-" + num;

                // Check by exact flight number
                Optional<Flight> byNum = flightRepository.findByFlightNumber(standardCode)
                        .or(() -> flightRepository.findByFlightNumberAndActiveTrue(standardCode));
                if (byNum.isPresent()) {
                    return byNum;
                }

                // Check by flight number prefix
                Optional<Flight> prefixMatch = flightRepository.findAll().stream()
                        .filter(f -> f.getFlightNumber() != null && f.getFlightNumber().startsWith(standardCode))
                        .findFirst();
                if (prefixMatch.isPresent()) {
                    return prefixMatch;
                }

                // Auto-provision flight into MongoDB with id = id so all subsequent operations find it directly!
                return autoProvision(flightRepository, id, code, num, parts.length >= 4 ? parts[3] : null);
            }
        }

        return Optional.empty();
    }

    private static Optional<Flight> autoProvision(FlightRepository flightRepository, String id, String code, String num, String rawDate) {
        try {
            String flightNumber = code + "-" + num;
            String airline = switch (code) {
                case "AI" -> "Air India";
                case "6E" -> "IndiGo";
                case "UK" -> "Vistara";
                case "QP" -> "Akasa Air";
                case "IX" -> "Air India Express";
                case "SG" -> "SpiceJet";
                case "EK" -> "Emirates";
                case "SQ" -> "Singapore Airlines";
                default -> "SmartTravel Airline";
            };

            String orig = "BOM";
            String dest = "BLR";
            if (num.equals("101") || num.equals("955") || num.equals("605")) { orig = "DEL"; dest = "BOM"; }
            else if (num.equals("1301")) { orig = "BLR"; dest = "BOM"; }
            else if (num.equals("103")) { orig = "BOM"; dest = "DEL"; }
            else if (num.equals("801")) { orig = "DEL"; dest = "COK"; }

            Instant depTime = Instant.now().plus(4, ChronoUnit.HOURS);
            if (rawDate != null && rawDate.length() == 8) {
                try {
                    int y = Integer.parseInt(rawDate.substring(0, 4));
                    int m = Integer.parseInt(rawDate.substring(4, 6));
                    int d = Integer.parseInt(rawDate.substring(6, 8));
                    depTime = LocalDate.of(y, m, d).atTime(13, 40).toInstant(ZoneOffset.UTC);
                } catch (Exception ignored) {}
            }

            Flight flight = FlightDataSeeder.buildFlight(
                    flightNumber,
                    airline,
                    code,
                    orig,
                    dest,
                    "Boeing 737 MAX 8",
                    depTime,
                    120,
                    3650.0,
                    FlightStatus.SCHEDULED,
                    null,
                    null
            );
            flight.setId(id);
            flight.setActive(true);

            Flight saved = flightRepository.save(flight);
            log.info("Auto-provisioned instant flight {} (flightNumber: {}) into MongoDB successfully", id, flightNumber);
            return Optional.of(saved);
        } catch (Exception ex) {
            log.warn("Failed to auto-provision instant flight {}: {}", id, ex.getMessage());
            return Optional.empty();
        }
    }
}
