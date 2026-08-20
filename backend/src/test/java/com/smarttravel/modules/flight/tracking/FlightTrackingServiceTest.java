package com.smarttravel.modules.flight.tracking;

import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.tracking.dto.TrackedFlightResponse;
import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import com.smarttravel.modules.flight.tracking.repository.TrackedFlightRepository;
import com.smarttravel.modules.flight.tracking.service.FlightTrackingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightTrackingServiceTest {

    @Mock
    private TrackedFlightRepository trackedFlightRepository;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightTrackingServiceImpl trackingService;

    private Flight sampleFlight;

    @BeforeEach
    void setUp() {
        sampleFlight = Flight.builder()
                .id("flight-101")
                .flightNumber("AI-101")
                .airline("Air India")
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").city("Mumbai").build())
                .departureTime(Instant.now().plusSeconds(3600))
                .arrivalTime(Instant.now().plusSeconds(7200))
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("trackFlight creates new active subscription when not previously tracked")
    void testTrackFlight_New() {
        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(sampleFlight));
        when(trackedFlightRepository.findByUserIdAndFlightId("user-1", "flight-101")).thenReturn(Optional.empty());
        when(trackedFlightRepository.save(any(TrackedFlight.class))).thenAnswer(inv -> {
            TrackedFlight tf = inv.getArgument(0);
            tf.setId("tracked-01");
            return tf;
        });

        TrackedFlightResponse response = trackingService.trackFlight("flight-101", "user-1");

        assertThat(response).isNotNull();
        assertThat(response.getFlightId()).isEqualTo("flight-101");
        assertThat(response.getFlightNumber()).isEqualTo("AI-101");
        assertThat(response.getRoute()).isEqualTo("DEL → BOM");
        assertThat(response.isActive()).isTrue();

        verify(trackedFlightRepository).save(any(TrackedFlight.class));
    }

    @Test
    @DisplayName("trackFlight is idempotent: reactivates if previously untracked")
    void testTrackFlight_Reactivate() {
        TrackedFlight existing = TrackedFlight.builder()
                .id("tracked-01")
                .userId("user-1")
                .flightId("flight-101")
                .active(false)
                .build();

        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(sampleFlight));
        when(trackedFlightRepository.findByUserIdAndFlightId("user-1", "flight-101")).thenReturn(Optional.of(existing));
        when(trackedFlightRepository.save(any(TrackedFlight.class))).thenReturn(existing);

        TrackedFlightResponse response = trackingService.trackFlight("flight-101", "user-1");

        assertThat(response).isNotNull();
        assertThat(existing.isActive()).isTrue();
    }

    @Test
    @DisplayName("untrackFlight marks subscription inactive")
    void testUntrackFlight() {
        TrackedFlight active = TrackedFlight.builder()
                .id("tracked-01")
                .userId("user-1")
                .flightId("flight-101")
                .active(true)
                .build();

        when(trackedFlightRepository.findByUserIdAndFlightId("user-1", "flight-101")).thenReturn(Optional.of(active));

        trackingService.untrackFlight("flight-101", "user-1");

        assertThat(active.isActive()).isFalse();
        verify(trackedFlightRepository).save(active);
    }

    @Test
    @DisplayName("getTrackedFlights populates live flight operational details")
    void testGetTrackedFlights() {
        TrackedFlight tf = TrackedFlight.builder()
                .id("tracked-01")
                .userId("user-1")
                .flightId("flight-101")
                .flightNumber("AI-101")
                .route("DEL → BOM")
                .active(true)
                .build();

        when(trackedFlightRepository.findByUserIdAndActiveTrue("user-1")).thenReturn(List.of(tf));
        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(sampleFlight));

        List<TrackedFlightResponse> list = trackingService.getTrackedFlights("user-1");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCurrentStatus()).isEqualTo(FlightStatus.SCHEDULED);
        assertThat(list.get(0).getDepartureAirportCode()).isEqualTo("DEL");
    }

    @Test
    @DisplayName("trackFlight throws ResourceNotFoundException for unknown flight")
    void testTrackFlight_NotFound() {
        when(flightRepository.findById("invalid-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trackingService.trackFlight("invalid-id", "user-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
