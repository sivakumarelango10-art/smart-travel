package com.smarttravel.modules.flight.aviationstack;

import com.smarttravel.modules.flight.config.AviationstackProperties;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackFlightDataProvider;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import com.smarttravel.modules.flight.tracking.repository.TrackedFlightRepository;
import com.smarttravel.modules.flight.tracking.service.LiveFlightTrackingSyncService;
import com.smarttravel.modules.flight.websocket.FlightStatusEvent;
import com.smarttravel.modules.flight.websocket.FlightStatusWebSocketPublisher;
import com.smarttravel.modules.notification.service.WebPushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveFlightTrackingSyncServiceTest {

    @Mock
    private TrackedFlightRepository trackedFlightRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private AviationstackFlightDataProvider aviationstackProvider;

    @Mock
    private FlightStatusWebSocketPublisher webSocketPublisher;

    @Mock
    private WebPushService webPushService;

    private AviationstackProperties properties;
    private LiveFlightTrackingSyncService syncService;

    @BeforeEach
    void setUp() {
        properties = new AviationstackProperties();
        properties.setProvider("AVIATIONSTACK");
        properties.setEnabled(true);

        syncService = new LiveFlightTrackingSyncService(
                properties,
                trackedFlightRepository,
                flightRepository,
                aviationstackProvider,
                webSocketPublisher,
                webPushService
        );
    }

    @Test
    @DisplayName("1. Syncs active tracked flight when Aviationstack detects operational status transition")
    void testSyncStatusTransition() {
        TrackedFlight tf = TrackedFlight.builder()
                .id("tf-1")
                .flightId("flight-101")
                .flightNumber("AI-101")
                .userId("user-1")
                .active(true)
                .lastKnownStatus(FlightStatus.SCHEDULED)
                .build();

        Flight flight = Flight.builder()
                .id("flight-101")
                .flightNumber("AI-101")
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightStatusSnapshot liveSnapshot = new FlightStatusSnapshot(
                "AI-101",
                FlightStatus.DELAYED,
                45,
                "Air traffic congestion",
                Instant.now().plusSeconds(2700),
                Instant.now().plusSeconds(10800),
                "Gate 14",
                "T3",
                "AVIATIONSTACK"
        );

        when(aviationstackProvider.fetchLatestStatus("AI-101", null)).thenReturn(Optional.of(liveSnapshot));
        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(flight));

        boolean synced = syncService.syncSingleFlight(tf);

        assertTrue(synced);
        assertEquals(FlightStatus.DELAYED, tf.getLastKnownStatus());

        // Verify WebSocket event broadcast
        ArgumentCaptor<FlightStatusEvent> eventCaptor = ArgumentCaptor.forClass(FlightStatusEvent.class);
        verify(webSocketPublisher, times(1)).publish(eventCaptor.capture());
        FlightStatusEvent event = eventCaptor.getValue();
        assertEquals(FlightStatus.SCHEDULED, event.getPreviousStatus());
        assertEquals(FlightStatus.DELAYED, event.getStatus());
        assertEquals(45, event.getDelayMinutes());
        assertEquals("Gate 14", event.getGate());

        // Verify Push Notification dispatched
        verify(webPushService, times(1)).sendPushForFlight(
                eq("flight-101"),
                contains("Flight AI-101"),
                contains("delayed by 45 minutes"),
                eq("/tracked-flights"),
                eq("FLIGHT_STATUS_UPDATE")
        );
    }

    @Test
    @DisplayName("2. Suppresses duplicate WebSocket and push notifications when flight status is unchanged")
    void testSuppressDuplicateUpdates() {
        Instant eta = Instant.parse("2026-08-21T18:00:00Z");
        TrackedFlight tf = TrackedFlight.builder()
                .id("tf-2")
                .flightId("flight-202")
                .flightNumber("6E-202")
                .userId("user-2")
                .active(true)
                .lastKnownStatus(FlightStatus.SCHEDULED)
                .lastKnownEta(eta)
                .build();

        FlightStatusSnapshot unchangedSnapshot = new FlightStatusSnapshot(
                "6E-202",
                FlightStatus.SCHEDULED,
                0,
                null,
                Instant.parse("2026-08-21T16:00:00Z"),
                eta,
                "Gate 3",
                "T1",
                "AVIATIONSTACK"
        );

        when(aviationstackProvider.fetchLatestStatus("6E-202", null)).thenReturn(Optional.of(unchangedSnapshot));

        boolean synced = syncService.syncSingleFlight(tf);

        assertFalse(synced, "No sync event should be triggered when status and ETA are identical");
        verify(webSocketPublisher, never()).publish(any());
        verify(webPushService, never()).sendPushForFlight(any(), any(), any(), any(), any());
    }
}
