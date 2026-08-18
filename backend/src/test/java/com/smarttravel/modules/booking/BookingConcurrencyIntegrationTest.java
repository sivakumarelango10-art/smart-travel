package com.smarttravel.modules.booking;

import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingConcurrencyIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    private String flightId;
    private final List<String> createdBookingIds = Collections.synchronizedList(new ArrayList<>());

    @BeforeEach
    void setUp() {
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").country("India").build();

        CabinInventoryDto econ = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(100)
                .basePrice(new BigDecimal("5000.00"))
                .taxAmount(new BigDecimal("600.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5750.00"))
                .build();

        CabinInventoryDto bus = CabinInventoryDto.builder()
                .cabinClass(CabinClass.BUSINESS)
                .totalSeats(10)
                .availableSeats(5) // Exactly 5 seats available
                .basePrice(new BigDecimal("15000.00"))
                .taxAmount(new BigDecimal("1800.00"))
                .feeAmount(new BigDecimal("300.00"))
                .totalPrice(new BigDecimal("17100.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("TEST-CONCUR-" + System.currentTimeMillis())
                .airline("SmartAir")
                .airlineCode("SA")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(172800))
                .arrivalTime(Instant.now().plusSeconds(180000))
                .aircraftModel("A321neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(110)
                .availableSeats(105)
                .cabinClasses(Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS))
                .cabinInventories(List.of(econ, bus))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse flightRes = flightService.createFlight(flightReq);
        flightId = flightRes.getId();
    }

    @AfterEach
    void tearDown() {
        for (String id : createdBookingIds) {
            try {
                bookingRepository.deleteById(id);
            } catch (Exception ignored) {
            }
        }
        if (flightId != null) {
            try {
                flightRepository.deleteById(flightId);
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("Concurrency Test: 10 concurrent requests for 5 available seats -> exactly 5 succeed, 5 fail with Conflict, final seats = 0")
    void testConcurrentBookings_ExactCapacityLimit() throws InterruptedException {
        int totalRequests = 10;
        int expectedSuccess = 5;
        int expectedFailures = 5;

        ExecutorService executor = Executors.newFixedThreadPool(totalRequests);
        CountDownLatch readyLatch = new CountDownLatch(totalRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        AtomicInteger otherErrorCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await(); // Wait for all threads to be ready to fire simultaneously

                    PassengerDto p = PassengerDto.builder()
                            .title("Mr")
                            .firstName("Passenger" + index)
                            .lastName("Concurrent")
                            .dateOfBirth(LocalDate.of(1990, 1, 1))
                            .gender("MALE")
                            .nationality("Indian")
                            .build();

                    BookingCreateRequest request = BookingCreateRequest.builder()
                            .flightId(flightId)
                            .cabinClass(CabinClass.BUSINESS)
                            .passengers(List.of(p))
                            .build();

                    BookingResponse res = bookingService.createBooking(request, "user-" + index, "user" + index + "@smarttravel.com");
                    createdBookingIds.add(res.getId());
                    successCount.incrementAndGet();
                } catch (ConflictException ex) {
                    conflictCount.incrementAndGet();
                } catch (Exception ex) {
                    otherErrorCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown(); // Fire all 10 requests simultaneously
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(expectedSuccess);
        assertThat(conflictCount.get()).isEqualTo(expectedFailures);
        assertThat(otherErrorCount.get()).isEqualTo(0);

        // Verify MongoDB database state: Business availableSeats must be exactly 0, Economy must be unchanged (100)
        Flight updatedFlight = flightRepository.findById(flightId).orElseThrow();
        int remainingBusiness = updatedFlight.getCabinInventories().stream()
                .filter(c -> c.getCabinClass() == CabinClass.BUSINESS)
                .findFirst()
                .orElseThrow()
                .getAvailableSeats();

        int remainingEconomy = updatedFlight.getCabinInventories().stream()
                .filter(c -> c.getCabinClass() == CabinClass.ECONOMY)
                .findFirst()
                .orElseThrow()
                .getAvailableSeats();

        assertThat(remainingBusiness).isEqualTo(0);
        assertThat(remainingEconomy).isEqualTo(100);
        assertThat(updatedFlight.getAvailableSeats()).isEqualTo(100);
    }

    @Test
    @DisplayName("Concurrency Test: 2 concurrent requests for 2 passengers each when availableSeats = 3 -> exactly 1 succeeds, 1 fails with Conflict, remaining seats = 1")
    void testConcurrentBookings_MultiPassengerContention() throws InterruptedException {
        // Reset Business availableSeats to 3
        Flight flight = flightRepository.findById(flightId).orElseThrow();
        flight.getCabinInventories().stream()
                .filter(c -> c.getCabinClass() == CabinClass.BUSINESS)
                .findFirst()
                .orElseThrow()
                .setAvailableSeats(3);
        flight.setAvailableSeats(103);
        flightRepository.save(flight);

        int totalRequests = 2;
        ExecutorService executor = Executors.newFixedThreadPool(totalRequests);
        CountDownLatch readyLatch = new CountDownLatch(totalRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            executor.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();

                    PassengerDto p1 = PassengerDto.builder()
                            .title("Mr")
                            .firstName("PaxA" + index)
                            .lastName("Contention")
                            .dateOfBirth(LocalDate.of(1990, 1, 1))
                            .gender("MALE")
                            .nationality("Indian")
                            .build();

                    PassengerDto p2 = PassengerDto.builder()
                            .title("Mrs")
                            .firstName("PaxB" + index)
                            .lastName("Contention")
                            .dateOfBirth(LocalDate.of(1992, 2, 2))
                            .gender("FEMALE")
                            .nationality("Indian")
                            .build();

                    BookingCreateRequest request = BookingCreateRequest.builder()
                            .flightId(flightId)
                            .cabinClass(CabinClass.BUSINESS)
                            .passengers(List.of(p1, p2))
                            .build();

                    BookingResponse res = bookingService.createBooking(request, "contender-" + index, "contender" + index + "@smarttravel.com");
                    createdBookingIds.add(res.getId());
                    successCount.incrementAndGet();
                } catch (ConflictException ex) {
                    conflictCount.incrementAndGet();
                } catch (Exception ex) {
                    // unexpected
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        readyLatch.await(5, TimeUnit.SECONDS);
        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);

        // Verify remaining inventory is exactly 3 - 2 = 1
        Flight updatedFlight = flightRepository.findById(flightId).orElseThrow();
        int remainingBusiness = updatedFlight.getCabinInventories().stream()
                .filter(c -> c.getCabinClass() == CabinClass.BUSINESS)
                .findFirst()
                .orElseThrow()
                .getAvailableSeats();

        assertThat(remainingBusiness).isEqualTo(1);
    }
}
