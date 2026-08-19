package com.smarttravel.modules.flight.disruption.service;

import com.smarttravel.common.exception.BadRequestException;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingStateMachine;

import com.smarttravel.modules.flight.disruption.dto.FlightCancelRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightDisruptionDto;
import com.smarttravel.modules.flight.disruption.dto.FlightGateChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightOperationalStatusResponse;
import com.smarttravel.modules.flight.disruption.dto.FlightScheduleChangeRequest;
import com.smarttravel.modules.flight.disruption.dto.FlightTerminalChangeRequest;
import com.smarttravel.modules.flight.disruption.model.DisruptionStatus;

import com.smarttravel.modules.flight.disruption.model.FlightDisruption;
import com.smarttravel.modules.flight.disruption.repository.FlightDisruptionRepository;
import com.smarttravel.modules.flight.impact.service.FlightImpactService;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.FlightStatusHistoryRepository;
import com.smarttravel.modules.flight.service.FlightStateMachine;
import com.smarttravel.modules.flight.service.SeatMapService;
import com.smarttravel.modules.notification.service.NotificationService;
import com.smarttravel.modules.payment.refund.model.RefundReason;
import com.smarttravel.modules.payment.refund.service.RefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightDisruptionServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightDisruptionRepository disruptionRepository;

    @Mock
    private FlightStatusHistoryRepository statusHistoryRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightStateMachine flightStateMachine;

    @Mock
    private BookingStateMachine bookingStateMachine;

    @Mock
    private FlightImpactService flightImpactService;

    @Mock
    private RefundService refundService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private SeatMapService seatMapService;

    @InjectMocks
    private FlightDisruptionServiceImpl disruptionService;

    private Flight testFlight;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        AirportInfo dep = AirportInfo.builder()
                .code("DEL").name("Indira Gandhi").city("Delhi").country("India").terminal("T3").gate("12A").build();
        AirportInfo arr = AirportInfo.builder()
                .code("BOM").name("Chhatrapati Shivaji").city("Mumbai").country("India").build();

        testFlight = Flight.builder()
                .id("flight-101")
                .flightNumber("ST-101")
                .airline("SmartTravel")
                .departureAirport(dep)
                .arrivalAirport(arr)
                .departureTime(now.plus(10, ChronoUnit.HOURS))
                .arrivalTime(now.plus(12, ChronoUnit.HOURS))
                .aircraftModel("Boeing 737 MAX 8")
                .totalSeats(150)
                .status(FlightStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("Should successfully reschedule flight while preserving original schedule timestamps")
    void shouldRescheduleFlightSuccessfully() {
        Instant newDep = now.plus(14, ChronoUnit.HOURS);
        Instant newArr = now.plus(16, ChronoUnit.HOURS);
        FlightScheduleChangeRequest req = new FlightScheduleChangeRequest(newDep, newArr, "ATC Ground Stop", "Severe Weather");

        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(testFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));
        when(disruptionRepository.save(any(FlightDisruption.class))).thenAnswer(i -> {
            FlightDisruption d = i.getArgument(0);
            d.setId("disrupt-1");
            return d;
        });

        FlightOperationalStatusResponse res = disruptionService.rescheduleFlight("flight-101", req, "admin@smarttravel.com");

        assertThat(res).isNotNull();
        assertThat(res.getRevisedDepartureTime()).isEqualTo(newDep);
        assertThat(res.getEstimatedArrivalTime()).isEqualTo(newArr);
        // Ensure original schedule is preserved
        assertThat(res.getScheduledDepartureTime()).isEqualTo(testFlight.getDepartureTime());
        assertThat(res.getScheduledArrivalTime()).isEqualTo(testFlight.getArrivalTime());

        verify(disruptionRepository).save(any(FlightDisruption.class));
    }

    @Test
    @DisplayName("Should reject reschedule when new arrival is before new departure")
    void shouldRejectRescheduleWithInvalidTimes() {
        Instant newDep = now.plus(14, ChronoUnit.HOURS);
        Instant newArr = now.plus(13, ChronoUnit.HOURS); // Before departure
        FlightScheduleChangeRequest req = new FlightScheduleChangeRequest(newDep, newArr, "ATC Ground Stop", "Invalid");

        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(testFlight));

        assertThatThrownBy(() -> disruptionService.rescheduleFlight("flight-101", req, "admin@smarttravel.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("strictly after new departure time");
    }

    @Test
    @DisplayName("Should cancel flight, update bookings, release seats, and trigger refunds")
    void shouldCancelFlightSuccessfully() {
        FlightCancelRequest req = new FlightCancelRequest("Cyclone Warning", "Airport shut down", true);
        Booking booking = Booking.builder()
                .id("book-1")
                .flightId("flight-101")
                .userId("user-1")
                .bookingReference("PNR123")
                .status(BookingStatus.CONFIRMED)
                .build();

        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(testFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));
        when(disruptionRepository.save(any(FlightDisruption.class))).thenAnswer(i -> {
            FlightDisruption d = i.getArgument(0);
            d.setId("disrupt-2");
            return d;
        });
        when(bookingRepository.findByFlightId("flight-101")).thenReturn(List.of(booking));

        FlightOperationalStatusResponse res = disruptionService.cancelFlight("flight-101", req, "admin@smarttravel.com");

        assertThat(res.getStatus()).isEqualTo(FlightStatus.CANCELLED);
        verify(flightStateMachine).validateTransition(FlightStatus.SCHEDULED, FlightStatus.CANCELLED);
        verify(seatMapService).releaseSeats("book-1");
        verify(refundService).processDisruptionRefundsForFlight("flight-101", RefundReason.FLIGHT_CANCELLED, "admin@smarttravel.com");
    }

    @Test
    @DisplayName("Should update gate and persist disruption record")
    void shouldUpdateGateSuccessfully() {
        FlightGateChangeRequest req = new FlightGateChangeRequest("Gate 22B", "Remote stand assigned");

        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(testFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));
        when(disruptionRepository.save(any(FlightDisruption.class))).thenAnswer(i -> {
            FlightDisruption d = i.getArgument(0);
            d.setId("disrupt-3");
            return d;
        });

        FlightOperationalStatusResponse res = disruptionService.updateGate("flight-101", req, "admin@smarttravel.com");

        assertThat(res.getGate()).isEqualTo("Gate 22B");
        verify(disruptionRepository).save(any(FlightDisruption.class));
    }

    @Test
    @DisplayName("Should update terminal and persist disruption record")
    void shouldUpdateTerminalSuccessfully() {
        FlightTerminalChangeRequest req = new FlightTerminalChangeRequest("T1", "Terminal maintenance");

        when(flightRepository.findById("flight-101")).thenReturn(Optional.of(testFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(i -> i.getArgument(0));
        when(disruptionRepository.save(any(FlightDisruption.class))).thenAnswer(i -> {
            FlightDisruption d = i.getArgument(0);
            d.setId("disrupt-4");
            return d;
        });

        FlightOperationalStatusResponse res = disruptionService.updateTerminal("flight-101", req, "admin@smarttravel.com");

        assertThat(res.getTerminal()).isEqualTo("T1");
        verify(disruptionRepository).save(any(FlightDisruption.class));
    }

    @Test
    @DisplayName("Should resolve disruption successfully")
    void shouldResolveDisruptionSuccessfully() {
        FlightDisruption disruption = FlightDisruption.builder()
                .id("disrupt-1")
                .flightId("flight-101")
                .status(DisruptionStatus.ACTIVE)
                .build();

        when(disruptionRepository.findById("disrupt-1")).thenReturn(Optional.of(disruption));
        when(disruptionRepository.save(any(FlightDisruption.class))).thenAnswer(i -> i.getArgument(0));

        FlightDisruptionDto res = disruptionService.resolveDisruption("disrupt-1", "admin@smarttravel.com");

        assertThat(res.getStatus()).isEqualTo(DisruptionStatus.RESOLVED);
        assertThat(res.getResolvedAt()).isNotNull();
    }
}
