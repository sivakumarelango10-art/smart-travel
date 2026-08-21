package com.smarttravel.modules.flight.provider.aviationstack;

import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackFlightItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Normalization engine converting raw Aviationstack responses into standard SmartTravel domain DTOs.
 */
@Component
public class AviationstackDataNormalizer {

    private static final Logger log = LoggerFactory.getLogger(AviationstackDataNormalizer.class);

    /**
     * Maps an Aviationstack raw status string into the SmartTravel FlightStatus enum.
     */
    public FlightStatus mapStatus(String rawStatus, Integer depDelayMinutes) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return (depDelayMinutes != null && depDelayMinutes > 15) ? FlightStatus.DELAYED : FlightStatus.SCHEDULED;
        }

        String normalized = rawStatus.trim().toLowerCase();
        return switch (normalized) {
            case "scheduled" -> (depDelayMinutes != null && depDelayMinutes > 15) ? FlightStatus.DELAYED : FlightStatus.SCHEDULED;
            case "active" -> FlightStatus.DEPARTED;
            case "landed" -> FlightStatus.ARRIVED;
            case "cancelled" -> FlightStatus.CANCELLED;
            case "incident", "diverted" -> FlightStatus.DIVERTED;
            case "delayed" -> FlightStatus.DELAYED;
            case "boarding" -> FlightStatus.BOARDING;
            default -> {
                log.debug("Unrecognized Aviationstack status '{}'. Falling back to SCHEDULED.", rawStatus);
                yield (depDelayMinutes != null && depDelayMinutes > 15) ? FlightStatus.DELAYED : FlightStatus.SCHEDULED;
            }
        };
    }

    /**
     * Converts a single Aviationstack flight item into a FlightStatusSnapshot.
     */
    public FlightStatusSnapshot toFlightStatusSnapshot(AviationstackFlightItem item, String fallbackFlightNumber) {
        if (item == null) {
            return null;
        }

        String flightNum = resolveFlightNumber(item, fallbackFlightNumber);
        Integer depDelay = (item.getDeparture() != null) ? item.getDeparture().getDelay() : null;
        FlightStatus status = mapStatus(item.getFlightStatus(), depDelay);

        String delayReason = null;
        if (depDelay != null && depDelay > 0) {
            delayReason = "Air traffic control or airport operational delay";
        }

        Instant revisedDeparture = parseTimestamp(item.getDeparture() != null ? item.getDeparture().getEstimated() : null);
        if (revisedDeparture == null && item.getDeparture() != null) {
            revisedDeparture = parseTimestamp(item.getDeparture().getActual());
        }

        Instant estimatedArrival = parseTimestamp(item.getArrival() != null ? item.getArrival().getEstimated() : null);
        if (estimatedArrival == null && item.getArrival() != null) {
            estimatedArrival = parseTimestamp(item.getArrival().getActual());
        }

        String gate = (item.getDeparture() != null && item.getDeparture().getGate() != null)
                ? item.getDeparture().getGate()
                : "TBD";

        String terminal = (item.getDeparture() != null && item.getDeparture().getTerminal() != null)
                ? item.getDeparture().getTerminal()
                : "T1";

        return new FlightStatusSnapshot(
                flightNum,
                status,
                depDelay,
                delayReason,
                revisedDeparture,
                estimatedArrival,
                gate,
                terminal,
                "AVIATIONSTACK_LIVE_FEED"
        );
    }

    /**
     * Converts a single Aviationstack flight item into a normalized FlightResponse for search / catalog APIs.
     */
    public FlightResponse toFlightResponse(AviationstackFlightItem item, String dataSource) {
        if (item == null) {
            return null;
        }

        String flightNumber = resolveFlightNumber(item, "FLIGHT");
        String airlineName = (item.getAirline() != null && item.getAirline().getName() != null)
                ? item.getAirline().getName()
                : "Global Airways";
        String airlineCode = (item.getAirline() != null && item.getAirline().getIata() != null)
                ? item.getAirline().getIata()
                : "GA";

        // Departure Airport
        AirportDto departureAirport = new AirportDto();
        if (item.getDeparture() != null) {
            departureAirport.setCode(item.getDeparture().getIata() != null ? item.getDeparture().getIata() : "DEL");
            departureAirport.setName(item.getDeparture().getAirport() != null ? item.getDeparture().getAirport() : "Departure Airport");
            departureAirport.setCity(item.getDeparture().getAirport() != null ? item.getDeparture().getAirport() : "Origin City");
            departureAirport.setCountry("India");
            departureAirport.setTerminal(item.getDeparture().getTerminal() != null ? item.getDeparture().getTerminal() : "T3");
        }

        // Arrival Airport
        AirportDto arrivalAirport = new AirportDto();
        if (item.getArrival() != null) {
            arrivalAirport.setCode(item.getArrival().getIata() != null ? item.getArrival().getIata() : "BOM");
            arrivalAirport.setName(item.getArrival().getAirport() != null ? item.getArrival().getAirport() : "Arrival Airport");
            arrivalAirport.setCity(item.getArrival().getAirport() != null ? item.getArrival().getAirport() : "Destination City");
            arrivalAirport.setCountry("India");
            arrivalAirport.setTerminal(item.getArrival().getTerminal() != null ? item.getArrival().getTerminal() : "T2");
        }

        Instant depTime = parseTimestamp(item.getDeparture() != null ? item.getDeparture().getScheduled() : null);
        if (depTime == null) depTime = Instant.now().plus(Duration.ofHours(2));

        Instant arrTime = parseTimestamp(item.getArrival() != null ? item.getArrival().getScheduled() : null);
        if (arrTime == null) arrTime = depTime.plus(Duration.ofMinutes(135));

        int duration = (int) Math.max(30, Duration.between(depTime, arrTime).toMinutes());

        String aircraftModel = (item.getAircraft() != null && item.getAircraft().getIata() != null)
                ? item.getAircraft().getIata()
                : "Airbus A321neo";

        Integer depDelay = (item.getDeparture() != null) ? item.getDeparture().getDelay() : null;
        FlightStatus status = mapStatus(item.getFlightStatus(), depDelay);

        Instant revisedDep = parseTimestamp(item.getDeparture() != null ? item.getDeparture().getEstimated() : null);
        Instant estArr = parseTimestamp(item.getArrival() != null ? item.getArrival().getEstimated() : null);

        Set<CabinClass> cabins = new HashSet<>();
        cabins.add(CabinClass.ECONOMY);
        cabins.add(CabinClass.BUSINESS);

        FlightResponse resp = new FlightResponse();
        resp.setId("avstack_" + flightNumber.replace("-", "").toLowerCase());
        resp.setFlightNumber(flightNumber);
        resp.setAirline(airlineName);
        resp.setAirlineCode(airlineCode);
        resp.setDepartureAirport(departureAirport);
        resp.setArrivalAirport(arrivalAirport);
        resp.setDepartureTime(depTime);
        resp.setArrivalTime(arrTime);
        resp.setDurationMinutes(duration);
        resp.setAircraftModel(aircraftModel);
        resp.setBasePrice(BigDecimal.valueOf(5400.00));
        resp.setTotalSeats(180);
        resp.setAvailableSeats(45);
        resp.setCabinClasses(cabins);
        resp.setStatus(status);
        resp.setDelayMinutes(depDelay);
        resp.setDelayReason(depDelay != null && depDelay > 0 ? "Air traffic & operational delay" : null);
        resp.setRevisedDepartureTime(revisedDep);
        resp.setEstimatedArrival(estArr);
        resp.setLastStatusUpdated(Instant.now());
        resp.setActive(true);
        resp.setCreatedAt(Instant.now());
        resp.setUpdatedAt(Instant.now());

        return resp;
    }

    public String resolveFlightNumber(AviationstackFlightItem item, String fallback) {
        if (item != null && item.getFlight() != null) {
            if (item.getFlight().getIata() != null && !item.getFlight().getIata().isBlank()) {
                return item.getFlight().getIata().trim().toUpperCase();
            }
            if (item.getFlight().getNumber() != null && !item.getFlight().getNumber().isBlank()) {
                String airlineCode = (item.getAirline() != null && item.getAirline().getIata() != null)
                        ? item.getAirline().getIata().toUpperCase()
                        : "FL";
                return airlineCode + "-" + item.getFlight().getNumber().trim();
            }
        }
        return fallback != null ? fallback.trim().toUpperCase() : "UNKNOWN";
    }

    public Instant parseTimestamp(String timestampStr) {
        if (timestampStr == null || timestampStr.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(timestampStr.trim());
        } catch (Exception ex) {
            try {
                return OffsetDateTime.parse(timestampStr.trim(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
