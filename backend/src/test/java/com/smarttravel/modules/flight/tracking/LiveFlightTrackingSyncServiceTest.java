package com.smarttravel.modules.flight.tracking;

import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import com.smarttravel.modules.flight.tracking.repository.TrackedFlightRepository;
import com.smarttravel.modules.flight.tracking.service.LiveFlightTrackingSyncService;
import com.smarttravel.modules.flight.websocket.FlightStatusWebSocketPublisher;
import com.smarttravel.modules.notification.service.WebPushService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveFlightTrackingSyncServiceTest {

    @Mock
    private TrackedFlightRepository trackedFlightRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightStatusProvider flightStatusProvider;

    @Mock
    private FlightStatusWebSocketPublisher webSocketPublisher;

    @Mock
    private WebPushService webPushService;

    private LiveFlightTrackingSyncService syncService;

    @BeforeEach
    void setUp() {
        syncService = new LiveFlightTrackingSyncService(
                trackedFlightRepository,
                flightRepository,
                flightStatusProvider,
                webSocketPublisher,
                webPushService
        );
    }

    @Test
    @DisplayName("1. Syncs active tracked flight when status transition is detected")
    void testSyncSingleFlightStatusTransition() {
        TrackedFlight tf = new TrackedFlight();
        tf.setId("tf-101");
        tf.setFlightId("fl-101");
        tf.setFlightNumber("AI-101");
        tf.setActive(true);
        tf.setLastKnownStatus(FlightStatus.ON_TIME);

        FlightStatusSnapshot liveSnapshot = new FlightStatusSnapshot(
                "AI-101",
                FlightStatus.BOARDING,
                0,
                null,
                Instant.now().plus(30, ChronoUnit.MINUTES),
                Instant.now().plus(2, ChronoUnit.HOURS),
                "Gate 12",
                "T3",
                "SIMULATED"
        );

        when(flightStatusProvider.fetchLatestStatus("AI-101", null)).thenReturn(Optional.of(liveSnapshot));

        boolean updated = syncService.syncSingleFlight(tf);

        assertThat(updated).isTrue();
        assertThat(tf.getLastKnownStatus()).isEqualTo(FlightStatus.BOARDING);
        verify(trackedFlightRepository).save(tf);
        verify(webSocketPublisher).publish(any());
    }

    @Test
    @DisplayName("2. Does not publish redundant events if status and ETA are unchanged")
    void testSyncSingleFlightNoChange() {
        Instant eta = Instant.now().plus(2, ChronoUnit.HOURS);
        TrackedFlight tf = new TrackedFlight();
        tf.setId("tf-202");
        tf.setFlightId("fl-202");
        tf.setFlightNumber("6E-202");
        tf.setActive(true);
        tf.setLastKnownStatus(FlightStatus.ON_TIME);
        tf.setLastKnownEta(eta);

        FlightStatusSnapshot unchangedSnapshot = new FlightStatusSnapshot(
                "6E-202",
                FlightStatus.ON_TIME,
                0,
                null,
                Instant.now().plus(1, ChronoUnit.HOURS),
                eta,
                "Gate 4",
                "T2",
                "SIMULATED"
        );

        when(flightStatusProvider.fetchLatestStatus("6E-202", null)).thenReturn(Optional.of(unchangedSnapshot));

        boolean updated = syncService.syncSingleFlight(tf);

        assertThat(updated).isFalse();
        verify(trackedFlightRepository, never()).save(tf);
        verify(webSocketPublisher, never()).publish(any());
    }
}
