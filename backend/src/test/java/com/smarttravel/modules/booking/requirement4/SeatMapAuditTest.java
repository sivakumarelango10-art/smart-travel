package com.smarttravel.modules.booking.requirement4;

import com.smarttravel.modules.flight.config.AircraftSeatLayout;
import com.smarttravel.modules.flight.dto.SeatMapResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.SeatRepository;
import com.smarttravel.modules.flight.service.SeatMapServiceImpl;
import com.smarttravel.modules.flight.websocket.SeatMapWebSocketPublisher;
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
import static org.mockito.Mockito.*;

/**
 * Requirement #4 — Flight Seat Map & Layout Generation Audit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Requirement #4: Flight Seat Map & Layout Audit")
class SeatMapAuditTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private SeatMapWebSocketPublisher webSocketPublisher;

    private AircraftSeatLayout seatLayout;
    private SeatMapServiceImpl seatMapService;
    private Flight testFlight;

    @BeforeEach
    void setUp() {
        seatLayout = new AircraftSeatLayout();
        seatMapService = new SeatMapServiceImpl(seatRepository, flightRepository, seatLayout, webSocketPublisher);

        testFlight = Flight.builder()
                .id("fl-seat-test-01")
                .flightNumber("ST-401")
                .airline("SmartTravel Airlines")
                .aircraftModel("Boeing 737-800")
                .totalSeats(180)
                .availableSeats(180)
                .basePrice(new BigDecimal("4500.00"))
                .cabinClasses(Set.of(CabinClass.BUSINESS, CabinClass.ECONOMY))
                .status(com.smarttravel.modules.flight.model.FlightStatus.SCHEDULED)
                .departureTime(Instant.now().plusSeconds(86400 * 3))
                .arrivalTime(Instant.now().plusSeconds(86400 * 3 + 7200))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("[SM-1] Generates realistic seat layout with rows, columns (A-F), and aisles")
    void testDynamicSeatMapGeneration() {
        List<Seat> generated = seatLayout.generateSeatsForFlight(
                testFlight.getId(), testFlight.getFlightNumber(), testFlight.getAircraftModel(),
                testFlight.getCabinClasses(), 180
        );

        assertThat(generated).isNotEmpty();
        assertThat(generated.size()).isEqualTo(180);

        // Verify row and column patterns
        assertThat(generated).anyMatch(s -> s.getSeatNumber().equals("1A") && s.getCabinClass() == CabinClass.BUSINESS);
        assertThat(generated).anyMatch(s -> s.getSeatNumber().equals("12A") && s.getCabinClass() == CabinClass.ECONOMY);
        assertThat(generated).allMatch(s -> s.getStatus() == SeatStatus.AVAILABLE);
    }

    @Test
    @DisplayName("[SM-2] Seat map API returns categorized cabin groups and availability counts")
    void testGetFlightSeatMapResponse() {
        when(flightRepository.findById("fl-seat-test-01")).thenReturn(Optional.of(testFlight));
        when(seatRepository.existsByFlightId("fl-seat-test-01")).thenReturn(true);

        Seat seat1A = Seat.builder().seatNumber("1A").rowNumber(1).column("A").cabinClass(CabinClass.BUSINESS).status(SeatStatus.AVAILABLE).build();
        Seat seat1B = Seat.builder().seatNumber("1B").rowNumber(1).column("B").cabinClass(CabinClass.BUSINESS).status(SeatStatus.BOOKED).build();
        Seat seat12A = Seat.builder().seatNumber("12A").rowNumber(12).column("A").cabinClass(CabinClass.ECONOMY).status(SeatStatus.AVAILABLE).build();

        when(seatRepository.findByFlightIdOrderByRowNumberAscColumnAsc("fl-seat-test-01"))
                .thenReturn(List.of(seat1A, seat1B, seat12A));

        SeatMapResponse response = seatMapService.getFlightSeatMap("fl-seat-test-01");

        assertThat(response).isNotNull();
        assertThat(response.getFlightId()).isEqualTo("fl-seat-test-01");
        assertThat(response.getTotalSeats()).isEqualTo(3);
        assertThat(response.getAvailableSeatsCount()).isEqualTo(2);
        assertThat(response.getCabinSeats()).containsKey(CabinClass.BUSINESS);
        assertThat(response.getCabinSeats()).containsKey(CabinClass.ECONOMY);
    }

    @Test
    @DisplayName("[SM-3] Extra legroom seats (Row 7 front & Row 12 exit row) have price adjustments")
    void testExtraLegroomPricingAdjustments() {
        List<Seat> generated = seatLayout.generateSeatsForFlight(
                testFlight.getId(), testFlight.getFlightNumber(), "Boeing 737-800",
                Set.of(CabinClass.ECONOMY), 60
        );

        Seat frontRowSeat = generated.stream().filter(s -> s.getRowNumber() == 7).findFirst().orElseThrow();
        Seat exitRowSeat = generated.stream().filter(s -> s.getRowNumber() == 12).findFirst().orElseThrow();
        Seat standardSeat = generated.stream().filter(s -> s.getRowNumber() == 9).findFirst().orElseThrow();

        assertThat(frontRowSeat.getPriceAdjustment()).isGreaterThan(BigDecimal.ZERO);
        assertThat(exitRowSeat.getPriceAdjustment()).isGreaterThan(BigDecimal.ZERO);
        assertThat(standardSeat.getPriceAdjustment()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
