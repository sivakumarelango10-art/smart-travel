package com.smarttravel.modules.flight.aviationstack;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackClient;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackDataNormalizer;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackFlightDataProvider;
import com.smarttravel.modules.flight.provider.aviationstack.dto.*;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AviationstackFlightDataProviderTest {

    @Mock
    private AviationstackClient aviationstackClient;

    @Mock
    private AviationstackDataNormalizer normalizer;

    @Mock
    private FlightRepository flightRepository;

    private AviationstackFlightDataProvider provider;

    @BeforeEach
    void setUp() {
        provider = new AviationstackFlightDataProvider(aviationstackClient, normalizer, flightRepository);
    }

    @Test
    @DisplayName("1. Provider reports correct name and live status capabilities")
    void testProviderCapabilities() {
        assertEquals("AVIATIONSTACK", provider.getProviderName());
        assertTrue(provider.isLiveProvider());
    }

    @Test
    @DisplayName("2. Successfully queries and normalizes live Aviationstack flight telemetry")
    void testLiveTelemetryFetchSuccess() {
        AviationstackFlightResponse liveResp = new AviationstackFlightResponse();
        AviationstackFlightItem item = new AviationstackFlightItem();
        item.setFlightStatus("active");
        liveResp.setData(List.of(item));

        FlightStatusSnapshot expectedSnapshot = new FlightStatusSnapshot(
                "AI-101",
                FlightStatus.DEPARTED,
                0,
                null,
                Instant.now(),
                Instant.now().plusSeconds(7200),
                "Gate 4",
                "T3",
                "AVIATIONSTACK"
        );

        when(aviationstackClient.getFlightStatus("AI-101")).thenReturn(Optional.of(liveResp));
        when(normalizer.toFlightStatusSnapshot(eq(item), eq("AI-101"), anyString())).thenReturn(expectedSnapshot);

        Optional<FlightStatusSnapshot> snapshotOpt = provider.fetchLatestStatus("AI-101", null);

        assertTrue(snapshotOpt.isPresent());
        FlightStatusSnapshot snapshot = snapshotOpt.get();
        assertEquals("AI-101", snapshot.flightNumber());
        assertEquals(FlightStatus.DEPARTED, snapshot.status());
        assertEquals("Gate 4", snapshot.gate());
        assertEquals("AVIATIONSTACK", snapshot.updatedSource());
    }

    @Test
    @DisplayName("3. Gracefully falls back to local database when Aviationstack is unavailable or returns empty")
    void testFallbackToLocalDatabase() {
        when(aviationstackClient.getFlightStatus("AI-999")).thenReturn(Optional.empty());

        Flight localFlight = Flight.builder()
                .flightNumber("AI-999")
                .status(FlightStatus.BOARDING)
                .departureAirport(AirportInfo.builder().code("DEL").terminal("T3").build())
                .delayMinutes(15)
                .delayReason("Traffic")
                .build();

        when(flightRepository.findByFlightNumber("AI-999")).thenReturn(Optional.of(localFlight));

        Optional<FlightStatusSnapshot> snapshotOpt = provider.fetchLatestStatus("AI-999", null);

        assertTrue(snapshotOpt.isPresent());
        FlightStatusSnapshot snapshot = snapshotOpt.get();
        assertEquals("AI-999", snapshot.flightNumber());
        assertEquals(FlightStatus.BOARDING, snapshot.status());
        assertEquals("SIMULATED", snapshot.updatedSource());
        assertEquals("T3", snapshot.terminal());
    }
}
