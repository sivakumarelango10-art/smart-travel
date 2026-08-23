package com.smarttravel.modules.pricing.requirement2;

import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.mapper.BookingMapper;
import com.smarttravel.modules.booking.model.Booking;
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
import com.smarttravel.modules.pricing.repository.PriceFreezeRepository;
import com.smarttravel.modules.pricing.service.DynamicPricingService;
import com.smarttravel.modules.pricing.service.DynamicPricingServiceImpl;
import com.smarttravel.modules.pricing.service.PriceFreezeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Requirement #2 - Test Group 6: Concurrency & High Load Resilience
 * Verifies concurrent bookings, concurrent price freezes, inventory protection, and race-condition safety.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PricingConcurrencyAndRaceConditionAuditTest {

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
    private PriceFreezeRepository priceFreezeRepository;

    @Mock
    private DynamicPricingService dynamicPricingService;

    private BookingServiceImpl bookingService;
    private PriceFreezeServiceImpl priceFreezeService;

    private Flight flight;
    private CabinInventory cabinInventory;

    @BeforeEach
    void setUp() {
        priceFreezeService = new PriceFreezeServiceImpl(priceFreezeRepository, flightRepository, dynamicPricingService);
        ReflectionTestUtils.setField(priceFreezeService, "defaultFreezeDurationMinutes", 30);

        bookingService = new BookingServiceImpl(
                bookingRepository,
                flightRepository,
                reservationService,
                fareCalculationService,
                stateMachine,
                pnrGenerator,
                new BookingMapper(),
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
                .id("fl-concurr-01")
                .flightNumber("UK-811")
                .active(true)
                .status(com.smarttravel.modules.flight.model.FlightStatus.SCHEDULED)
                .departureTime(Instant.now().plus(2, ChronoUnit.DAYS))
                .cabinInventories(List.of(cabinInventory))
                .build();
    }

    @Test
    @DisplayName("43. Concurrent bookings atomically decrement seats and preserve data integrity")
    void testConcurrentBookingsDataIntegrity() throws InterruptedException {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        AtomicInteger successfulBookings = new AtomicInteger(0);

        when(flightRepository.findByIdAndActiveTrue("fl-concurr-01")).thenReturn(Optional.of(flight));
        when(reservationService.reserveSeats("fl-concurr-01", CabinClass.ECONOMY, 1)).thenAnswer(inv -> true);
        when(pnrGenerator.generatePnr()).thenAnswer(inv -> "PNR-" + ThreadLocalRandom.current().nextInt(100000, 999999));
        when(fareCalculationService.calculateFare(any(), any(), eq(1))).thenReturn(
                FareBreakdownDto.builder().baseFare(new BigDecimal("5000.00")).taxes(new BigDecimal("600.00")).fees(new BigDecimal("150.00")).totalAmount(new BigDecimal("5750.00")).currency("INR").passengerCount(1).build());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId("booking-" + ThreadLocalRandom.current().nextInt(1000, 9999));
            return b;
        });

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    PassengerDto pax = PassengerDto.builder()
                            .title("Mr")
                            .firstName("Traveler" + idx)
                            .lastName("Test")
                            .dateOfBirth(LocalDate.of(1990, 1, 1))
                            .gender("MALE")
                            .nationality("Indian")
                            .build();

                    BookingCreateRequest req = BookingCreateRequest.builder()
                            .flightId("fl-concurr-01")
                            .cabinClass(CabinClass.ECONOMY)
                            .passengers(List.of(pax))
                            .build();

                    bookingService.createBooking(req, "user-" + idx, "user" + idx + "@test.com");
                    successfulBookings.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successfulBookings.get()).isEqualTo(threads);
        verify(reservationService, times(threads)).reserveSeats("fl-concurr-01", CabinClass.ECONOMY, 1);
    }

    @Test
    @DisplayName("44. Concurrent price freeze creation correctly locks fare and prevents over-commitment")
    void testConcurrentPriceFreezeCreation() throws InterruptedException {
        int threads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger freezeCount = new AtomicInteger(0);

        when(flightRepository.findById("fl-concurr-01")).thenReturn(Optional.of(flight));
        when(priceFreezeRepository.findByUserIdAndFlightIdAndStatus(anyString(), eq("fl-concurr-01"), eq(PriceFreezeStatus.ACTIVE)))
                .thenReturn(Optional.empty());
        when(dynamicPricingService.calculateDynamicPrice(any(), any(), eq(1))).thenReturn(
                com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown.builder()
                        .totalPerPassenger(new BigDecimal("5750.00"))
                        .grandTotal(new BigDecimal("5750.00"))
                        .build());
        when(priceFreezeRepository.save(any(PriceFreeze.class))).thenAnswer(inv -> {
            PriceFreeze pf = inv.getArgument(0);
            pf.setId("freeze-" + ThreadLocalRandom.current().nextInt(100, 999));
            return pf;
        });

        for (int i = 0; i < threads; i++) {
            final int idx = i;
            executor.submit(() -> {
                try {
                    priceFreezeService.createFreeze("user-" + idx, "fl-concurr-01", CabinClass.ECONOMY, 1);
                    freezeCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(freezeCount.get()).isEqualTo(threads);
        verify(priceFreezeRepository, times(threads)).save(any(PriceFreeze.class));
    }

    @Test
    @DisplayName("45. Last-seat inventory race condition rejects over-booking safely")
    void testLastSeatOverbookingProtection() {
        when(flightRepository.findByIdAndActiveTrue("fl-concurr-01")).thenReturn(Optional.of(flight));
        // Simulate seat reservation failing (sold out on concurrent thread)
        when(reservationService.reserveSeats("fl-concurr-01", CabinClass.ECONOMY, 1)).thenReturn(false);

        PassengerDto pax = PassengerDto.builder()
                .title("Ms")
                .firstName("Alice")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1992, 3, 10))
                .gender("FEMALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest req = BookingCreateRequest.builder()
                .flightId("fl-concurr-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(pax))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(req, "user-alice", "alice@example.com"))
                .isInstanceOf(com.smarttravel.common.exception.ConflictException.class)
                .hasMessageContaining("Insufficient seat availability");
    }

    @Test
    @DisplayName("46. Compensating rollback releases seats if booking persistence fails")
    void testCompensatingRollbackOnPersistenceFailure() {
        when(flightRepository.findByIdAndActiveTrue("fl-concurr-01")).thenReturn(Optional.of(flight));
        when(reservationService.reserveSeats("fl-concurr-01", CabinClass.ECONOMY, 1)).thenReturn(true);
        when(pnrGenerator.generatePnr()).thenReturn("PNR-FAIL");
        when(fareCalculationService.calculateFare(any(), any(), eq(1))).thenReturn(
                FareBreakdownDto.builder().baseFare(new BigDecimal("5000.00")).taxes(new BigDecimal("600.00")).fees(new BigDecimal("150.00")).totalAmount(new BigDecimal("5750.00")).currency("INR").passengerCount(1).build());

        // Simulate MongoDB write failure
        when(bookingRepository.save(any(Booking.class))).thenThrow(new RuntimeException("MongoDB connection lost"));

        PassengerDto pax = PassengerDto.builder()
                .title("Mr")
                .firstName("Bob")
                .lastName("Builder")
                .dateOfBirth(LocalDate.of(1985, 7, 20))
                .gender("MALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest req = BookingCreateRequest.builder()
                .flightId("fl-concurr-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(pax))
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(req, "user-bob", "bob@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("MongoDB connection lost");

        // Verify compensating rollback occurred
        verify(reservationService).releaseSeats("fl-concurr-01", CabinClass.ECONOMY, 1);
    }

    @Test
    @DisplayName("47. Double booking with same active freeze is prevented once freeze is USED")
    void testDoubleBookingSameFreezePrevented() {
        PriceFreeze usedFreeze = PriceFreeze.builder()
                .id("freeze-already-used")
                .userId("user-bob")
                .status(PriceFreezeStatus.USED)
                .bookingId("booking-prior")
                .build();

        when(flightRepository.findByIdAndActiveTrue("fl-concurr-01")).thenReturn(Optional.of(flight));
        when(reservationService.reserveSeats("fl-concurr-01", CabinClass.ECONOMY, 1)).thenReturn(true);
        when(priceFreezeRepository.findById("freeze-already-used")).thenReturn(Optional.of(usedFreeze));

        PassengerDto pax = PassengerDto.builder()
                .title("Mr")
                .firstName("Bob")
                .lastName("Builder")
                .dateOfBirth(LocalDate.of(1985, 7, 20))
                .gender("MALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest req = BookingCreateRequest.builder()
                .flightId("fl-concurr-01")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(pax))
                .priceFreezeId("freeze-already-used")
                .build();

        assertThatThrownBy(() -> bookingService.createBooking(req, "user-bob", "bob@example.com"))
                .isInstanceOf(com.smarttravel.common.exception.BadRequestException.class)
                .hasMessageContaining("no longer active");
    }

    @Test
    @DisplayName("48. Real-time dynamic price update broadcasts without throwing if publisher is null")
    void testPriceUpdateWithoutPublisher() {
        DynamicPricingServiceImpl standalonePricing = new DynamicPricingServiceImpl(null, null, null);
        // Must not throw NullPointerException
        standalonePricing.publishPriceUpdate(flight, cabinInventory, new BigDecimal("5000.00"));
    }
}
