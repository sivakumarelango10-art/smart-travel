package com.smarttravel.modules.flight;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.Seat;
import com.smarttravel.modules.flight.model.SeatStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.SeatRepository;
import com.smarttravel.modules.flight.service.SeatMapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class SeatConcurrencyIntegrationTest {

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private SeatMapService seatMapService;

    private String flightId;

    @BeforeEach
    void setUp() {
        long ts = System.currentTimeMillis();
        Flight flight = Flight.builder()
                .flightNumber("CONCUR-SEAT-" + ts)
                .airline("SmartTravel Concurrency")
                .airlineCode("STC")
                .aircraftModel("Boeing 737 MAX 8")
                .totalSeats(150)
                .availableSeats(150)
                .basePrice(new BigDecimal("4500.00"))
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .status(com.smarttravel.modules.flight.model.FlightStatus.SCHEDULED)
                .departureTime(Instant.now().plusSeconds(86400 * 2))
                .arrivalTime(Instant.now().plusSeconds(86400 * 2 + 7200))
                .active(true)
                .build();

        Flight savedFlight = flightRepository.save(flight);
        flightId = savedFlight.getId();

        seatMapService.initializeSeatsForFlight(savedFlight);
    }

    @Test
    @DisplayName("10 concurrent threads attempting to reserve the EXACT SAME seat -> exactly 1 success, 9 conflicts")
    void testConcurrentSeatReservation() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        String targetSeat = "12A";

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    boolean held = seatMapService.holdSeats(
                            flightId,
                            CabinClass.ECONOMY,
                            List.of(targetSeat),
                            "bk-concur-" + index,
                            "PNR" + index,
                            Instant.now().plusSeconds(900)
                    );
                    if (held) {
                        successCount.incrementAndGet();
                    } else {
                        conflictCount.incrementAndGet();
                    }
                } catch (Exception ex) {
                    conflictCount.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(9);

        Seat seatInDb = seatRepository.findByFlightIdAndSeatNumber(flightId, targetSeat).orElseThrow();
        assertThat(seatInDb.getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(seatInDb.getBookingId()).startsWith("bk-concur-");
    }
}
