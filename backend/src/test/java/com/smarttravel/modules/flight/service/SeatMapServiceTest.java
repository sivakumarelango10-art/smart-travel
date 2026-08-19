package com.smarttravel.modules.flight.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.modules.flight.config.AircraftSeatLayout;
import com.smarttravel.modules.flight.dto.SeatMapResponse;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatMapServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private FlightRepository flightRepository;

    private AircraftSeatLayout aircraftSeatLayout;
    private SeatMapService seatMapService;

    private Flight testFlight;

    @BeforeEach
    void setUp() {
        aircraftSeatLayout = new AircraftSeatLayout();
        seatMapService = new SeatMapServiceImpl(seatRepository, flightRepository, aircraftSeatLayout);

        testFlight = Flight.builder()
                .id("fl-test-100")
                .flightNumber("ST-101")
                .airline("SmartTravel")
                .aircraftModel("Boeing 737 MAX 8")
                .totalSeats(150)
                .cabinClasses(Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS))
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").city("Mumbai").build())
                .departureTime(Instant.now().plusSeconds(86400))
                .build();
    }

    @Test
    @DisplayName("getFlightSeatMap returns complete structured layout without passenger PII")
    void testGetFlightSeatMap() {
        when(flightRepository.findById("fl-test-100")).thenReturn(Optional.of(testFlight));
        when(seatRepository.existsByFlightId("fl-test-100")).thenReturn(true);

        Seat seat1 = Seat.builder().flightId("fl-test-100").seatNumber("1A").rowNumber(1).column("A")
                .cabinClass(CabinClass.BUSINESS).status(SeatStatus.AVAILABLE).build();
        Seat seat2 = Seat.builder().flightId("fl-test-100").seatNumber("12B").rowNumber(12).column("B")
                .cabinClass(CabinClass.ECONOMY).status(SeatStatus.BOOKED).build();

        when(seatRepository.findByFlightIdOrderByRowNumberAscColumnAsc("fl-test-100"))
                .thenReturn(List.of(seat1, seat2));

        SeatMapResponse response = seatMapService.getFlightSeatMap("fl-test-100");

        assertThat(response).isNotNull();
        assertThat(response.getFlightNumber()).isEqualTo("ST-101");
        assertThat(response.getTotalSeats()).isEqualTo(2);
        assertThat(response.getAvailableSeatsCount()).isEqualTo(1);
        assertThat(response.getSeats()).hasSize(2);
        assertThat(response.getCabinSeats()).containsKey(CabinClass.BUSINESS);
        assertThat(response.getCabinSeats()).containsKey(CabinClass.ECONOMY);
    }

    @Test
    @DisplayName("holdSeats succeeds for available seats in matching cabin")
    void testHoldSeatsSuccess() {
        when(flightRepository.findById("fl-test-100")).thenReturn(Optional.of(testFlight));
        when(seatRepository.existsByFlightId("fl-test-100")).thenReturn(true);

        Seat seat = Seat.builder().flightId("fl-test-100").seatNumber("12A").cabinClass(CabinClass.ECONOMY)
                .status(SeatStatus.AVAILABLE).build();
        when(seatRepository.findByFlightIdAndSeatNumber("fl-test-100", "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.atomicHoldSeat(eq("fl-test-100"), eq("12A"), eq("bk-1"), eq("PNR1"), any(Instant.class)))
                .thenReturn(true);

        boolean result = seatMapService.holdSeats("fl-test-100", CabinClass.ECONOMY, List.of("12A"), "bk-1", "PNR1", Instant.now().plusSeconds(900));

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("holdSeats throws BadRequestException if seat cabin does not match")
    void testHoldSeatsCabinMismatch() {
        when(flightRepository.findById("fl-test-100")).thenReturn(Optional.of(testFlight));
        when(seatRepository.existsByFlightId("fl-test-100")).thenReturn(true);

        Seat seat = Seat.builder().flightId("fl-test-100").seatNumber("1A").cabinClass(CabinClass.BUSINESS)
                .status(SeatStatus.AVAILABLE).build();
        when(seatRepository.findByFlightIdAndSeatNumber("fl-test-100", "1A")).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> seatMapService.holdSeats("fl-test-100", CabinClass.ECONOMY, List.of("1A"), "bk-1", "PNR1", Instant.now().plusSeconds(900)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("belongs to cabin BUSINESS, not ECONOMY");
    }

    @Test
    @DisplayName("holdSeats throws ConflictException if atomic hold fails")
    void testHoldSeatsConflict() {
        when(flightRepository.findById("fl-test-100")).thenReturn(Optional.of(testFlight));
        when(seatRepository.existsByFlightId("fl-test-100")).thenReturn(true);

        Seat seat = Seat.builder().flightId("fl-test-100").seatNumber("12A").cabinClass(CabinClass.ECONOMY)
                .status(SeatStatus.AVAILABLE).build();
        when(seatRepository.findByFlightIdAndSeatNumber("fl-test-100", "12A")).thenReturn(Optional.of(seat));
        when(seatRepository.atomicHoldSeat(eq("fl-test-100"), eq("12A"), eq("bk-1"), eq("PNR1"), any(Instant.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> seatMapService.holdSeats("fl-test-100", CabinClass.ECONOMY, List.of("12A"), "bk-1", "PNR1", Instant.now().plusSeconds(900)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("is no longer available");
    }
}
