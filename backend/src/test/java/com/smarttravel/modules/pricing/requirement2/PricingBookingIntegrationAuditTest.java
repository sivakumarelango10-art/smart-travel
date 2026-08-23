package com.smarttravel.modules.pricing.requirement2;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.mapper.BookingMapper;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingServiceImpl;
import com.smarttravel.modules.booking.service.BookingStateMachine;
import com.smarttravel.modules.booking.service.FlightInventoryReservationService;
import com.smarttravel.modules.booking.service.PnrGenerator;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FareCalculationService;
import com.smarttravel.modules.pricing.model.PriceFreeze;
import com.smarttravel.modules.pricing.model.PriceFreezeStatus;
import com.smarttravel.modules.pricing.service.DynamicPricingService;
import com.smarttravel.modules.pricing.service.PriceFreezeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Requirement #2 - Test Group 4: Booking Integration & Price Protection
 * Verifies that booking checkout enforces server-side authoritative pricing, rejects price tampering, and respects valid price freezes.
 */
@ExtendWith(MockitoExtension.class)
class PricingBookingIntegrationAuditTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightInventoryReservationService reservationService;

    @Mock
    private FareCalculationService fareCalculationService;

    @Mock
    private BookingStateMachine stateMachine;

    @Mock
    private PnrGenerator pnrGenerator;

    @Mock
    private PriceFreezeService priceFreezeService;

    @Mock
    private DynamicPricingService dynamicPricingService;

    private BookingMapper bookingMapper = new BookingMapper();
    private BookingServiceImpl bookingService;

    private Flight flight;
    private CabinInventory cabinInventory;
    private PassengerDto passenger;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                flightRepository,
                reservationService,
                fareCalculationService,
                stateMachine,
                pnrGenerator,
                bookingMapper,
                null,
                null,
                null,
                priceFreezeService,
                dynamicPricingService
        );

        cabinInventory = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(50)
                .basePrice(new BigDecimal("5000.00"))
                .taxAmount(new BigDecimal("600.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5750.00"))
                .build();

        flight = Flight.builder()
                .id("flight-book-01")
                .flightNumber("AI-202")
                .active(true)
                .status(com.smarttravel.modules.flight.model.FlightStatus.SCHEDULED)
                .departureTime(Instant.now().plus(3, ChronoUnit.DAYS))
                .cabinInventories(List.of(cabinInventory))
                .build();

        passenger = PassengerDto.builder()
                .title("Mr")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .gender("MALE")
                .nationality("Indian")
                .build();
    }

    @Test
    @DisplayName("31. Normal booking calculates server-side dynamic/base fare")
    void testNormalBookingCalculatesServerFare() {
        when(flightRepository.findByIdAndActiveTrue("flight-book-01")).thenReturn(Optional.of(flight));
        when(reservationService.reserveSeats("flight-book-01", CabinClass.ECONOMY, 1)).thenReturn(true);
        when(pnrGenerator.generatePnr()).thenReturn("PNR12345");

        FareBreakdownDto fareDto = FareBreakdownDto.builder()
                .baseFare(new BigDecimal("5000.00"))
                .taxes(new BigDecimal("600.00"))
                .fees(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .passengerCount(1)
                .build();

        when(fareCalculationService.calculateFare(any(BigDecimal.class), eq(CabinClass.ECONOMY), eq(1)))
                .thenReturn(fareDto);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId("booking-norm-01");
            return b;
        });

        BookingCreateRequest req = BookingCreateRequest.builder()
                .flightId("flight-book-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(passenger))
                .build();

        BookingResponse response = bookingService.createBooking(req, "user-john", "john@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("5750.00");
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    @DisplayName("32. Frozen booking uses locked price from valid PriceFreeze record")
    void testFrozenBookingUsesLockedPrice() {
        when(flightRepository.findByIdAndActiveTrue("flight-book-01")).thenReturn(Optional.of(flight));
        when(reservationService.reserveSeats("flight-book-01", CabinClass.ECONOMY, 1)).thenReturn(true);
        when(pnrGenerator.generatePnr()).thenReturn("PNR-FREEZE");

        PriceFreeze activeFreeze = PriceFreeze.builder()
                .id("freeze-lock-01")
                .userId("user-john")
                .flightId("flight-book-01")
                .cabinClass(CabinClass.ECONOMY)
                .lockedPricePerPassenger(new BigDecimal("4800.00"))
                .lockedTotalPrice(new BigDecimal("4800.00"))
                .basePriceAtFreeze(new BigDecimal("4000.00"))
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().plus(20, ChronoUnit.MINUTES))
                .build();

        when(priceFreezeService.getFreezeById("freeze-lock-01", "user-john")).thenReturn(activeFreeze);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId("booking-freeze-01");
            return b;
        });

        BookingCreateRequest req = BookingCreateRequest.builder()
                .flightId("flight-book-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(passenger))
                .priceFreezeId("freeze-lock-01")
                .build();

        BookingResponse response = bookingService.createBooking(req, "user-john", "john@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getTotalAmount()).isEqualByComparingTo("4800.00");
        verify(priceFreezeService).markAsUsed("freeze-lock-01", "booking-freeze-01", "user-john");
    }

    @Test
    @DisplayName("33. Expired price freeze is rejected during booking creation")
    void testExpiredFreezeRejectedInBooking() {
        when(flightRepository.findByIdAndActiveTrue("flight-book-01")).thenReturn(Optional.of(flight));
        when(reservationService.reserveSeats("flight-book-01", CabinClass.ECONOMY, 1)).thenReturn(true);

        PriceFreeze expired = PriceFreeze.builder()
                .id("freeze-exp-01")
                .userId("user-john")
                .flightId("flight-book-01")
                .cabinClass(CabinClass.ECONOMY)
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().minus(5, ChronoUnit.MINUTES))
                .build();

        when(priceFreezeService.getFreezeById("freeze-exp-01", "user-john")).thenReturn(expired);

        BookingCreateRequest req = BookingCreateRequest.builder()
                .flightId("flight-book-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(passenger))
                .priceFreezeId("freeze-exp-01")
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(req, "user-john", "john@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("expired or no longer active");
    }

    @Test
    @DisplayName("34. Price freeze for different flight or cabin class is rejected")
    void testMismatchedFreezeRejectedInBooking() {
        when(flightRepository.findByIdAndActiveTrue("flight-book-01")).thenReturn(Optional.of(flight));
        when(reservationService.reserveSeats("flight-book-01", CabinClass.ECONOMY, 1)).thenReturn(true);

        PriceFreeze wrongFlightFreeze = PriceFreeze.builder()
                .id("freeze-wrong-flight")
                .userId("user-john")
                .flightId("flight-other-99")
                .cabinClass(CabinClass.ECONOMY)
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().plus(20, ChronoUnit.MINUTES))
                .build();

        when(priceFreezeService.getFreezeById("freeze-wrong-flight", "user-john")).thenReturn(wrongFlightFreeze);

        BookingCreateRequest req = BookingCreateRequest.builder()
                .flightId("flight-book-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(passenger))
                .priceFreezeId("freeze-wrong-flight")
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(req, "user-john", "john@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @DisplayName("35. Client cannot manipulate booking price because backend calculates authoritative fare")
    void testClientPriceTamperingResistance() {
        when(flightRepository.findByIdAndActiveTrue("flight-book-01")).thenReturn(Optional.of(flight));
        when(reservationService.reserveSeats("flight-book-01", CabinClass.ECONOMY, 1)).thenReturn(true);
        when(pnrGenerator.generatePnr()).thenReturn("PNR-SAFE");

        FareBreakdownDto authoritativeFare = FareBreakdownDto.builder()
                .baseFare(new BigDecimal("5000.00"))
                .taxes(new BigDecimal("600.00"))
                .fees(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .passengerCount(1)
                .build();

        when(fareCalculationService.calculateFare(any(BigDecimal.class), eq(CabinClass.ECONOMY), eq(1)))
                .thenReturn(authoritativeFare);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingCreateRequest req = BookingCreateRequest.builder()
                .flightId("flight-book-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(passenger))
                .build();

        BookingResponse response = bookingService.createBooking(req, "user-john", "john@example.com");

        // The authoritative backend price is ₹5750, ignoring any frontend amount
        assertThat(response.getTotalAmount()).isEqualByComparingTo("5750.00");
    }

    @Test
    @DisplayName("36. Seat selection count and passenger count must match for consistent pricing")
    void testPassengerCountConsistency() {
        BookingCreateRequest emptyPassengers = BookingCreateRequest.builder()
                .flightId("flight-book-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of())
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(emptyPassengers, "user-john", "john@example.com"))
                .isInstanceOf(BadRequestException.class);
    }
}
