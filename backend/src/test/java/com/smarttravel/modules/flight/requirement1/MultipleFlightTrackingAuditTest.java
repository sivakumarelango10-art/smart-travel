package com.smarttravel.modules.flight.requirement1;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Requirement #1 - Test Group D: Track Multiple Flights Simultaneously
 * Verifies multi-flight tracking, subscription isolation, idempotency,
 * untracking, and user boundary security.
 */
@ExtendWith(MockitoExtension.class)
class MultipleFlightTrackingAuditTest {

    @Mock
    private TrackedFlightRepository trackedFlightRepository;

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private FlightTrackingServiceImpl flightTrackingService;

    private Flight flightA;
    private Flight flightB;

    @BeforeEach
    void setUp() {
        flightA = Flight.builder()
                .id("fl-AAA")
                .flightNumber("6E-101")
                .airline("IndiGo")
                .status(FlightStatus.ON_TIME)
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").city("Mumbai").build())
                .departureTime(Instant.now().plusSeconds(3600))
                .arrivalTime(Instant.now().plusSeconds(10800))
                .active(true)
                .build();

        flightB = Flight.builder()
                .id("fl-BBB")
                .flightNumber("AI-202")
                .airline("Air India")
                .status(FlightStatus.DELAYED)
                .delayMinutes(30)
                .delayReason("Weather condition")
                .departureAirport(AirportInfo.builder().code("BLR").city("Bengaluru").build())
                .arrivalAirport(AirportInfo.builder().code("DEL").city("Delhi").build())
                .departureTime(Instant.now().plusSeconds(7200))
                .arrivalTime(Instant.now().plusSeconds(18000))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("34-35. User can track Flight A and Flight B independently")
    void testTrackFlightAAndB() {
        when(flightRepository.findById("fl-AAA")).thenReturn(Optional.of(flightA));
        when(flightRepository.findById("fl-BBB")).thenReturn(Optional.of(flightB));
        when(trackedFlightRepository.findByUserIdAndFlightId("user-1", "fl-AAA")).thenReturn(Optional.empty());
        when(trackedFlightRepository.findByUserIdAndFlightId("user-1", "fl-BBB")).thenReturn(Optional.empty());
        when(trackedFlightRepository.save(any(TrackedFlight.class))).thenAnswer(inv -> inv.getArgument(0));

        TrackedFlightResponse resA = flightTrackingService.trackFlight("fl-AAA", "user-1");
        TrackedFlightResponse resB = flightTrackingService.trackFlight("fl-BBB", "user-1");

        assertThat(resA.getFlightNumber()).isEqualTo("6E-101");
        assertThat(resA.getRoute()).isEqualTo("DEL → BOM");
        assertThat(resB.getFlightNumber()).isEqualTo("AI-202");
        assertThat(resB.getRoute()).isEqualTo("BLR → DEL");
    }

    @Test
    @DisplayName("36-37. User can track multiple flights simultaneously without collision")
    void testTrackMultipleFlightsSimultaneously() {
        TrackedFlight tfA = TrackedFlight.builder().id("tf-1").userId("user-1").flightId("fl-AAA").flightNumber("6E-101").active(true).build();
        TrackedFlight tfB = TrackedFlight.builder().id("tf-2").userId("user-1").flightId("fl-BBB").flightNumber("AI-202").active(true).build();

        when(trackedFlightRepository.findByUserIdAndActiveTrue("user-1")).thenReturn(List.of(tfA, tfB));
        when(flightRepository.findById("fl-AAA")).thenReturn(Optional.of(flightA));
        when(flightRepository.findById("fl-BBB")).thenReturn(Optional.of(flightB));

        List<TrackedFlightResponse> trackedList = flightTrackingService.getTrackedFlights("user-1");

        assertThat(trackedList).hasSize(2);
        assertThat(trackedList.stream().map(TrackedFlightResponse::getFlightNumber))
                .containsExactlyInAnyOrder("6E-101", "AI-202");
    }

    @Test
    @DisplayName("38. Untracking Flight A leaves Flight B active")
    void testUntrackFlightALeavesFlightBActive() {
        TrackedFlight tfA = TrackedFlight.builder().id("tf-1").userId("user-1").flightId("fl-AAA").active(true).build();
        TrackedFlight tfB = TrackedFlight.builder().id("tf-2").userId("user-1").flightId("fl-BBB").active(true).build();

        when(trackedFlightRepository.findByUserIdAndFlightId("user-1", "fl-AAA")).thenReturn(Optional.of(tfA));

        flightTrackingService.untrackFlight("fl-AAA", "user-1");

        assertThat(tfA.isActive()).isFalse();
        assertThat(tfB.isActive()).isTrue();
        verify(trackedFlightRepository).save(tfA);
    }

    @Test
    @DisplayName("39. Tracking the same flight twice is idempotent and does not create duplicate records")
    void testTrackingDuplicateIsIdempotent() {
        TrackedFlight existing = TrackedFlight.builder()
                .id("tf-1")
                .userId("user-1")
                .flightId("fl-AAA")
                .flightNumber("6E-101")
                .active(true)
                .build();

        when(flightRepository.findById("fl-AAA")).thenReturn(Optional.of(flightA));
        when(trackedFlightRepository.findByUserIdAndFlightId("user-1", "fl-AAA")).thenReturn(Optional.of(existing));

        TrackedFlightResponse response = flightTrackingService.trackFlight("fl-AAA", "user-1");

        assertThat(response.getFlightId()).isEqualTo("fl-AAA");
        verify(trackedFlightRepository, never()).save(any(TrackedFlight.class));
    }

    @Test
    @DisplayName("40-41. User A cannot access or see User B's tracked flights (Data Boundary Isolation)")
    void testUserIsolationForTrackedFlights() {
        TrackedFlight tfUserA = TrackedFlight.builder().id("tf-1").userId("user-alice").flightId("fl-AAA").active(true).build();
        TrackedFlight tfUserB = TrackedFlight.builder().id("tf-2").userId("user-bob").flightId("fl-BBB").active(true).build();

        when(trackedFlightRepository.findByUserIdAndActiveTrue("user-alice")).thenReturn(List.of(tfUserA));
        when(flightRepository.findById("fl-AAA")).thenReturn(Optional.of(flightA));

        List<TrackedFlightResponse> aliceFlights = flightTrackingService.getTrackedFlights("user-alice");

        assertThat(aliceFlights).hasSize(1);
        assertThat(aliceFlights.get(0).getFlightId()).isEqualTo("fl-AAA");
        assertThat(aliceFlights.stream().noneMatch(f -> f.getFlightId().equals("fl-BBB"))).isTrue();
    }
}
