package com.smarttravel.modules.flight.requirement1;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.provider.MockFlightStatusProviderImpl;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightStateMachine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

/**
 * Requirement #1 - Test Group A: Mock Flight Status Provider
 * Verifies that the mock provider architecture correctly returns status, delays, reasons,
 * revised schedules, ETAs, and valid status transitions.
 */
@ExtendWith(MockitoExtension.class)
class MockFlightStatusProviderAuditTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private MockFlightStatusProviderImpl mockFlightStatusProvider;

    private final FlightStateMachine stateMachine = new FlightStateMachine();

    private Instant scheduledDep;
    private Instant scheduledArr;

    @BeforeEach
    void setUp() {
        scheduledDep = Instant.parse("2026-10-15T10:00:00Z");
        scheduledArr = Instant.parse("2026-10-15T12:30:00Z");
    }

    @Test
    @DisplayName("1. Provider returns a valid flight status snapshot")
    void testProviderReturnsValidFlightStatus() {
        Flight flight = Flight.builder()
                .flightNumber("6E-101")
                .status(FlightStatus.SCHEDULED)
                .departureAirport(AirportInfo.builder().code("DEL").terminal("T3").build())
                .build();
        when(flightRepository.findByFlightNumber("6E-101")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("6E-101", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        FlightStatusSnapshot snapshot = snapshotOpt.get();
        assertThat(snapshot.flightNumber()).isEqualTo("6E-101");
        assertThat(snapshot.status()).isEqualTo(FlightStatus.SCHEDULED);
        assertThat(snapshot.updatedSource()).isEqualTo("MOCK_INTERNAL_SIMULATION");
    }

    @Test
    @DisplayName("2. Provider can return ON_TIME status")
    void testProviderReturnsOnTimeStatus() {
        Flight flight = Flight.builder()
                .flightNumber("AI-202")
                .status(FlightStatus.ON_TIME)
                .delayMinutes(0)
                .departureTime(scheduledDep)
                .arrivalTime(scheduledArr)
                .revisedDepartureTime(scheduledDep)
                .estimatedArrival(scheduledArr)
                .build();
        when(flightRepository.findByFlightNumber("AI-202")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("AI-202", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().status()).isEqualTo(FlightStatus.ON_TIME);
        assertThat(snapshotOpt.get().delayMinutes()).isEqualTo(0);
    }

    @Test
    @DisplayName("3. Provider can return BOARDING status")
    void testProviderReturnsBoardingStatus() {
        Flight flight = Flight.builder()
                .flightNumber("UK-815")
                .status(FlightStatus.BOARDING)
                .departureAirport(AirportInfo.builder().code("BOM").terminal("T2").build())
                .build();
        when(flightRepository.findByFlightNumber("UK-815")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("UK-815", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().status()).isEqualTo(FlightStatus.BOARDING);
        assertThat(snapshotOpt.get().terminal()).isEqualTo("T2");
        assertThat(snapshotOpt.get().gate()).isNotBlank();
    }

    @Test
    @DisplayName("4. Provider can return DELAYED status")
    void testProviderReturnsDelayedStatus() {
        Flight flight = Flight.builder()
                .flightNumber("SG-404")
                .status(FlightStatus.DELAYED)
                .delayMinutes(60)
                .delayReason("Weather visibility below minimums")
                .build();
        when(flightRepository.findByFlightNumber("SG-404")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("SG-404", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().status()).isEqualTo(FlightStatus.DELAYED);
    }

    @Test
    @DisplayName("5. Delayed status contains delay duration (e.g. 60 mins)")
    void testDelayedStatusContainsDelayDuration() {
        Flight flight = Flight.builder()
                .flightNumber("6E-303")
                .status(FlightStatus.DELAYED)
                .delayMinutes(60)
                .build();
        when(flightRepository.findByFlightNumber("6E-303")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("6E-303", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().delayMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("6. Delayed status contains delay reason")
    void testDelayedStatusContainsDelayReason() {
        Flight flight = Flight.builder()
                .flightNumber("AI-505")
                .status(FlightStatus.DELAYED)
                .delayMinutes(45)
                .delayReason("Air traffic control slot restriction")
                .build();
        when(flightRepository.findByFlightNumber("AI-505")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("AI-505", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().delayReason()).isEqualTo("Air traffic control slot restriction");
    }

    @Test
    @DisplayName("7. Revised departure time is present when delayed")
    void testRevisedDepartureTimePresentWhenDelayed() {
        Instant revisedDep = scheduledDep.plus(60, ChronoUnit.MINUTES);
        Flight flight = Flight.builder()
                .flightNumber("6E-707")
                .status(FlightStatus.DELAYED)
                .delayMinutes(60)
                .departureTime(scheduledDep)
                .revisedDepartureTime(revisedDep)
                .build();
        when(flightRepository.findByFlightNumber("6E-707")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("6E-707", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().revisedDepartureTime()).isEqualTo(revisedDep);
    }

    @Test
    @DisplayName("8. Revised arrival time is present when delayed")
    void testRevisedArrivalTimePresentWhenDelayed() {
        Instant revisedArr = scheduledArr.plus(60, ChronoUnit.MINUTES);
        Flight flight = Flight.builder()
                .flightNumber("UK-909")
                .status(FlightStatus.DELAYED)
                .delayMinutes(60)
                .arrivalTime(scheduledArr)
                .estimatedArrival(revisedArr)
                .build();
        when(flightRepository.findByFlightNumber("UK-909")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("UK-909", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().revisedArrivalTime()).isEqualTo(revisedArr);
    }

    @Test
    @DisplayName("9. Dynamic estimated arrival (ETA) is calculated and present")
    void testEtaIsCalculatedAndPresent() {
        Instant eta = scheduledArr.plus(75, ChronoUnit.MINUTES);
        Flight flight = Flight.builder()
                .flightNumber("AI-111")
                .status(FlightStatus.DELAYED)
                .delayMinutes(75)
                .estimatedArrival(eta)
                .build();
        when(flightRepository.findByFlightNumber("AI-111")).thenReturn(Optional.of(flight));

        Optional<FlightStatusSnapshot> snapshotOpt = mockFlightStatusProvider.fetchLatestStatus("AI-111", scheduledDep);

        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().revisedArrivalTime()).isEqualTo(eta);
    }

    @Test
    @DisplayName("10. State transitions conform to valid operational lifecycle")
    void testValidStatusTransitions() {
        // Validate normal progression: SCHEDULED -> BOARDING -> ON_TIME -> DEPARTED -> ARRIVED
        assertThatCode(() -> {
            stateMachine.validateTransition(FlightStatus.SCHEDULED, FlightStatus.BOARDING);
            stateMachine.validateTransition(FlightStatus.BOARDING, FlightStatus.ON_TIME);
            stateMachine.validateTransition(FlightStatus.ON_TIME, FlightStatus.DEPARTED);
            stateMachine.validateTransition(FlightStatus.DEPARTED, FlightStatus.ARRIVED);
        }).doesNotThrowAnyException();

        // Validate delay progression: SCHEDULED -> DELAYED -> BOARDING -> DEPARTED
        assertThatCode(() -> {
            stateMachine.validateTransition(FlightStatus.SCHEDULED, FlightStatus.DELAYED);
            stateMachine.validateTransition(FlightStatus.DELAYED, FlightStatus.BOARDING);
        }).doesNotThrowAnyException();
    }
}
