package com.smarttravel.modules.booking.requirement4;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.modules.flight.config.AircraftSeatLayout;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.SeatRepository;
import com.smarttravel.modules.flight.service.SeatMapServiceImpl;
import com.smarttravel.modules.flight.websocket.SeatMapUpdateEvent;
import com.smarttravel.modules.flight.websocket.SeatMapWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Requirement #4 — Seat Hold, Confirmation, Release, and Real-time WebSocket Broadcast Audit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Requirement #4: Seat Reservation & Real-time Broadcast Audit")
class SeatReservationAuditTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private SeatMapWebSocketPublisher webSocketPublisher;

    private SeatMapServiceImpl seatMapService;
    private Flight flight;

    @BeforeEach
    void setUp() {
        seatMapService = new SeatMapServiceImpl(seatRepository, flightRepository, new AircraftSeatLayout(), webSocketPublisher);

        flight = Flight.builder()
                .id("fl-res-01")
                .flightNumber("ST-505")
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .status(com.smarttravel.modules.flight.model.FlightStatus.SCHEDULED)
                .build();
    }

    @Test
    @DisplayName("[SR-1] Atomic hold reserves seat and broadcasts WebSocket event")
    void testHoldSeatsSuccessAndBroadcast() {
        when(flightRepository.findById("fl-res-01")).thenReturn(Optional.of(flight));
        when(seatRepository.existsByFlightId("fl-res-01")).thenReturn(true);

        Seat seat = Seat.builder()
                .flightId("fl-res-01")
                .seatNumber("12A")
                .cabinClass(CabinClass.ECONOMY)
                .status(SeatStatus.AVAILABLE)
                .build();

        when(seatRepository.findByFlightIdAndSeatNumber("fl-res-01", "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.atomicHoldSeat(eq("fl-res-01"), eq("12A"), anyString(), anyString(), any())).thenReturn(true);

        boolean held = seatMapService.holdSeats(
                "fl-res-01", CabinClass.ECONOMY, List.of("12A"), "bk-001", "STPNR01", Instant.now().plusSeconds(900));

        assertThat(held).isTrue();

        // Verify WebSocket event broadcast
        ArgumentCaptor<SeatMapUpdateEvent> captor = ArgumentCaptor.forClass(SeatMapUpdateEvent.class);
        verify(webSocketPublisher).publishSeatUpdate(captor.capture());

        SeatMapUpdateEvent event = captor.getValue();
        assertThat(event.getFlightId()).isEqualTo("fl-res-01");
        assertThat(event.getSeatNumbers()).contains("12A");
        assertThat(event.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(event.getAction()).isEqualTo("HELD");
    }

    @Test
    @DisplayName("[SR-2] Releasing seats resets state and broadcasts AVAILABLE event")
    void testReleaseSeatsBroadcast() {
        Seat heldSeat = Seat.builder()
                .flightId("fl-res-01")
                .seatNumber("12A")
                .bookingId("bk-001")
                .status(SeatStatus.HELD)
                .build();

        when(seatRepository.findByBookingId("bk-001")).thenReturn(List.of(heldSeat));
        when(seatRepository.releaseSeatsForBooking("bk-001")).thenReturn(1L);

        seatMapService.releaseSeats("bk-001");

        verify(seatRepository).releaseSeatsForBooking("bk-001");

        ArgumentCaptor<SeatMapUpdateEvent> captor = ArgumentCaptor.forClass(SeatMapUpdateEvent.class);
        verify(webSocketPublisher).publishSeatUpdate(captor.capture());

        SeatMapUpdateEvent event = captor.getValue();
        assertThat(event.getFlightId()).isEqualTo("fl-res-01");
        assertThat(event.getSeatNumbers()).contains("12A");
        assertThat(event.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(event.getAction()).isEqualTo("RELEASED");
    }

    @Test
    @DisplayName("[SR-3] Attempting to hold blocked or already-held seat throws ConflictException")
    void testHoldUnavailableSeatThrowsConflict() {
        when(flightRepository.findById("fl-res-01")).thenReturn(Optional.of(flight));
        when(seatRepository.existsByFlightId("fl-res-01")).thenReturn(true);

        Seat blockedSeat = Seat.builder()
                .flightId("fl-res-01")
                .seatNumber("14B")
                .cabinClass(CabinClass.ECONOMY)
                .status(SeatStatus.BLOCKED)
                .build();

        when(seatRepository.findByFlightIdAndSeatNumber("fl-res-01", "14B")).thenReturn(Optional.of(blockedSeat));

        assertThatThrownBy(() ->
                seatMapService.holdSeats("fl-res-01", CabinClass.ECONOMY, List.of("14B"), "bk-002", "STPNR02", Instant.now().plusSeconds(900))
        ).isInstanceOf(ConflictException.class);
    }
}
