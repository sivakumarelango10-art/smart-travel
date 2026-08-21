package com.smarttravel.modules.flight.aviationstack;

import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackDataNormalizer;
import com.smarttravel.modules.flight.provider.aviationstack.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AviationstackDataNormalizerTest {

    private AviationstackDataNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new AviationstackDataNormalizer();
    }

    @Test
    @DisplayName("1. Maps Aviationstack raw statuses accurately into FlightStatus enum")
    void testStatusMapping() {
        assertEquals(FlightStatus.SCHEDULED, normalizer.mapStatus("scheduled", null));
        assertEquals(FlightStatus.DELAYED, normalizer.mapStatus("scheduled", 30));
        assertEquals(FlightStatus.DEPARTED, normalizer.mapStatus("active", null));
        assertEquals(FlightStatus.ARRIVED, normalizer.mapStatus("landed", null));
        assertEquals(FlightStatus.CANCELLED, normalizer.mapStatus("cancelled", null));
        assertEquals(FlightStatus.DIVERTED, normalizer.mapStatus("incident", null));
        assertEquals(FlightStatus.DIVERTED, normalizer.mapStatus("diverted", null));
        assertEquals(FlightStatus.DELAYED, normalizer.mapStatus("delayed", null));
        assertEquals(FlightStatus.BOARDING, normalizer.mapStatus("boarding", null));
        assertEquals(FlightStatus.SCHEDULED, normalizer.mapStatus(null, null));
        assertEquals(FlightStatus.SCHEDULED, normalizer.mapStatus("unknown_status", null));
    }

    @Test
    @DisplayName("2. Normalizes full Aviationstack flight item into FlightStatusSnapshot")
    void testToFlightStatusSnapshot() {
        AviationstackFlightItem item = new AviationstackFlightItem();
        item.setFlightStatus("active");

        AviationstackFlightInfo flight = new AviationstackFlightInfo();
        flight.setIata("AI-101");
        flight.setNumber("101");
        item.setFlight(flight);

        AviationstackAirline airline = new AviationstackAirline();
        airline.setName("Air India");
        airline.setIata("AI");
        item.setAirline(airline);

        AviationstackAirport dep = new AviationstackAirport();
        dep.setAirport("Indira Gandhi International");
        dep.setIata("DEL");
        dep.setTerminal("T3");
        dep.setGate("Gate 12");
        dep.setDelay(25);
        dep.setEstimated("2026-08-21T14:30:00+00:00");
        item.setDeparture(dep);

        AviationstackAirport arr = new AviationstackAirport();
        arr.setAirport("Chhatrapati Shivaji Maharaj");
        arr.setIata("BOM");
        arr.setTerminal("T2");
        arr.setEstimated("2026-08-21T16:45:00+00:00");
        item.setArrival(arr);

        FlightStatusSnapshot snapshot = normalizer.toFlightStatusSnapshot(item, "AI-101");

        assertNotNull(snapshot);
        assertEquals("AI-101", snapshot.flightNumber());
        assertEquals(FlightStatus.DEPARTED, snapshot.status());
        assertEquals(25, snapshot.delayMinutes());
        assertEquals("Gate 12", snapshot.gate());
        assertEquals("T3", snapshot.terminal());
        assertEquals("AVIATIONSTACK_LIVE_FEED", snapshot.updatedSource());
        assertNotNull(snapshot.revisedDepartureTime());
        assertNotNull(snapshot.revisedArrivalTime());
    }

    @Test
    @DisplayName("3. Normalizes flight item into customer FlightResponse with airline and airport metadata")
    void testToFlightResponse() {
        AviationstackFlightItem item = new AviationstackFlightItem();
        item.setFlightStatus("scheduled");

        AviationstackFlightInfo flight = new AviationstackFlightInfo();
        flight.setIata("6E-204");
        item.setFlight(flight);

        AviationstackAirline airline = new AviationstackAirline();
        airline.setName("IndiGo");
        airline.setIata("6E");
        item.setAirline(airline);

        AviationstackAirport dep = new AviationstackAirport();
        dep.setIata("DEL");
        dep.setAirport("Delhi Airport");
        dep.setScheduled("2026-08-21T10:00:00Z");
        item.setDeparture(dep);

        AviationstackAirport arr = new AviationstackAirport();
        arr.setIata("BLR");
        arr.setAirport("Bengaluru Airport");
        arr.setScheduled("2026-08-21T12:45:00Z");
        item.setArrival(arr);

        AviationstackAircraft aircraft = new AviationstackAircraft();
        aircraft.setIata("A320");
        item.setAircraft(aircraft);

        FlightResponse resp = normalizer.toFlightResponse(item, "LIVE");

        assertNotNull(resp);
        assertEquals("6E-204", resp.getFlightNumber());
        assertEquals("IndiGo", resp.getAirline());
        assertEquals("6E", resp.getAirlineCode());
        assertEquals("DEL", resp.getDepartureAirport().getCode());
        assertEquals("BLR", resp.getArrivalAirport().getCode());
        assertEquals("A320", resp.getAircraftModel());
        assertEquals(165, resp.getDurationMinutes());
        assertEquals(FlightStatus.SCHEDULED, resp.getStatus());
    }

    @Test
    @DisplayName("4. Safely handles missing/null fields and malformed timestamps without throwing exceptions")
    void testNullAndMalformedHandling() {
        AviationstackFlightItem item = new AviationstackFlightItem();
        FlightStatusSnapshot snapshot = normalizer.toFlightStatusSnapshot(item, "FALLBACK-100");

        assertNotNull(snapshot);
        assertEquals("FALLBACK-100", snapshot.flightNumber());
        assertEquals(FlightStatus.SCHEDULED, snapshot.status());
        assertNull(snapshot.delayMinutes());
        assertEquals("TBD", snapshot.gate());
        assertEquals("T1", snapshot.terminal());

        // Malformed timestamp test
        assertNull(normalizer.parseTimestamp(null));
        assertNull(normalizer.parseTimestamp("invalid_timestamp_string"));
        Instant valid = normalizer.parseTimestamp("2026-08-21T12:00:00Z");
        assertNotNull(valid);
    }
}
