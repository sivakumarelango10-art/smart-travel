package com.smarttravel.modules.flight.requirement1;

import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.flight.tracking.dto.TrackedFlightResponse;
import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import com.smarttravel.modules.flight.tracking.repository.TrackedFlightRepository;
import com.smarttravel.modules.flight.tracking.service.FlightTrackingService;
import com.smarttravel.modules.flight.websocket.FlightStatusWebSocketPublisher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Requirement #1 - Parts 5 & 6: Concurrency & Real-Time Lifecycle Flow Integration Test
 * Verifies high-concurrency tracking operations, simultaneous multi-flight updates with topic isolation,
 * and complete real-time lifecycle sequence (ON_TIME -> DELAYED -> BOARDING -> ETA Change).
 */
@SpringBootTest
class FlightTrackingConcurrencyAndLiveFlowIntegrationTest {

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightTrackingService flightTrackingService;

    @Autowired
    private TrackedFlightRepository trackedFlightRepository;

    @MockBean
    private FlightStatusWebSocketPublisher webSocketPublisher;

    private String flightIdA;
    private String flightIdB;
    private String flightIdC;

    @BeforeEach
    void setUp() {
        String uid = UUID.randomUUID().toString().substring(0, 6);

        Flight fA = Flight.builder()
                .flightNumber("CC-101-" + uid)
                .airline("IndiGo")
                .status(FlightStatus.SCHEDULED)
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").terminal("T3").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").city("Mumbai").terminal("T2").build())
                .departureTime(Instant.now().plus(2, ChronoUnit.HOURS))
                .arrivalTime(Instant.now().plus(4, ChronoUnit.HOURS))
                .active(true)
                .build();
        flightIdA = flightRepository.save(fA).getId();

        Flight fB = Flight.builder()
                .flightNumber("CC-202-" + uid)
                .airline("Air India")
                .status(FlightStatus.SCHEDULED)
                .departureAirport(AirportInfo.builder().code("BLR").city("Bengaluru").terminal("T1").build())
                .arrivalAirport(AirportInfo.builder().code("DEL").city("Delhi").terminal("T3").build())
                .departureTime(Instant.now().plus(3, ChronoUnit.HOURS))
                .arrivalTime(Instant.now().plus(6, ChronoUnit.HOURS))
                .active(true)
                .build();
        flightIdB = flightRepository.save(fB).getId();

        Flight fC = Flight.builder()
                .flightNumber("CC-303-" + uid)
                .airline("Vistara")
                .status(FlightStatus.SCHEDULED)
                .departureAirport(AirportInfo.builder().code("MAA").city("Chennai").terminal("T1").build())
                .arrivalAirport(AirportInfo.builder().code("DEL").city("Delhi").terminal("T3").build())
                .departureTime(Instant.now().plus(5, ChronoUnit.HOURS))
                .arrivalTime(Instant.now().plus(8, ChronoUnit.HOURS))
                .active(true)
                .build();
        flightIdC = flightRepository.save(fC).getId();
    }

    @AfterEach
    void tearDown() {
        flightRepository.deleteById(flightIdA);
        flightRepository.deleteById(flightIdB);
        flightRepository.deleteById(flightIdC);
        trackedFlightRepository.deleteByUserIdAndFlightId("test-concurrent-user", flightIdA);
    }

    @Test
    @DisplayName("PART 5 - Concurrency: 10 concurrent track requests for the same user+flight all resolve to 1 record with zero exceptions")
    void testConcurrentFlightTrackingIdempotency() throws Exception {
        int threads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        String userId = "concur-user-" + UUID.randomUUID().toString().substring(0, 6);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    TrackedFlightResponse res = flightTrackingService.trackFlight(flightIdA, userId);
                    if (res != null && res.isActive() && res.getFlightId().equals(flightIdA)) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    // Exceptions should not occur with resilient concurrency handling
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // All 10 concurrent threads must successfully receive a valid response
        assertThat(successCount.get()).isEqualTo(10);

        // Verify exactly one active tracking record exists in DB for this user + flight
        List<TrackedFlight> tracked = trackedFlightRepository.findByUserIdAndActiveTrue(userId);
        assertThat(tracked).hasSize(1);
        assertThat(tracked.get(0).getFlightId()).isEqualTo(flightIdA);
    }

    @Test
    @DisplayName("PART 5 - Concurrency: User A + Flight X, User A + Flight Y, User B + Flight X all succeed independently")
    void testMultiUserMultiFlightDistinctTracking() {
        String userA = "user-alice-" + UUID.randomUUID().toString().substring(0, 5);
        String userB = "user-bob-" + UUID.randomUUID().toString().substring(0, 5);

        TrackedFlightResponse resAX = flightTrackingService.trackFlight(flightIdA, userA);
        TrackedFlightResponse resAY = flightTrackingService.trackFlight(flightIdB, userA);
        TrackedFlightResponse resBX = flightTrackingService.trackFlight(flightIdA, userB);

        assertThat(resAX.getFlightId()).isEqualTo(flightIdA);
        assertThat(resAY.getFlightId()).isEqualTo(flightIdB);
        assertThat(resBX.getFlightId()).isEqualTo(flightIdA);

        List<TrackedFlight> aliceTracked = trackedFlightRepository.findByUserIdAndActiveTrue(userA);
        List<TrackedFlight> bobTracked = trackedFlightRepository.findByUserIdAndActiveTrue(userB);

        assertThat(aliceTracked).hasSize(2);
        assertThat(bobTracked).hasSize(1);
    }

    @Test
    @DisplayName("PART 5 - Multi-Flight Concurrency: Simultaneous status events across Flight A, B, and C remain isolated")
    void testConcurrentMultiFlightStatusUpdatesIsolation() throws Exception {
        int threads = 3;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        executor.submit(() -> {
            try {
                startLatch.await();
                flightService.updateFlightStatus(flightIdA, FlightStatusUpdateRequest.builder()
                        .status(FlightStatus.DELAYED)
                        .delayMinutes(60)
                        .delayReason("Weather hold")
                        .build());
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                flightService.updateFlightStatus(flightIdB, FlightStatusUpdateRequest.builder()
                        .status(FlightStatus.BOARDING)
                        .build());
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                startLatch.await();
                flightService.updateFlightStatus(flightIdC, FlightStatusUpdateRequest.builder()
                        .status(FlightStatus.CANCELLED)
                        .delayReason("Operational constraints")
                        .build());
            } catch (Exception ignored) {
            } finally {
                doneLatch.countDown();
            }
        });

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        Flight fA = flightRepository.findById(flightIdA).orElseThrow();
        Flight fB = flightRepository.findById(flightIdB).orElseThrow();
        Flight fC = flightRepository.findById(flightIdC).orElseThrow();

        assertThat(fA.getStatus()).isEqualTo(FlightStatus.DELAYED);
        assertThat(fA.getDelayMinutes()).isEqualTo(60);

        assertThat(fB.getStatus()).isEqualTo(FlightStatus.BOARDING);

        assertThat(fC.getStatus()).isEqualTo(FlightStatus.CANCELLED);
    }

    @Test
    @DisplayName("PART 6 - Real-Time Behavior: Full sequence (SCHEDULED -> DELAYED -> BOARDING -> ON_TIME -> DEPARTED -> ARRIVED)")
    void testFullOperationalLifecycleSequence() {
        Flight flight = flightRepository.findById(flightIdA).orElseThrow();
        Instant originalDep = flight.getDepartureTime();
        Instant originalArr = flight.getArrivalTime();

        // 1. Initial State: SCHEDULED
        assertThat(flight.getStatus()).isEqualTo(FlightStatus.SCHEDULED);

        // 2. Simulate DELAYED by 1h with WEATHER reason
        FlightResponse delayedResp = flightService.updateFlightStatus(flightIdA, FlightStatusUpdateRequest.builder()
                .status(FlightStatus.DELAYED)
                .delayMinutes(60)
                .delayReason("Severe storm system across flight corridor")
                .revisedDepartureTime(originalDep.plus(60, ChronoUnit.MINUTES))
                .estimatedArrival(originalArr.plus(60, ChronoUnit.MINUTES))
                .build());

        assertThat(delayedResp.getStatus()).isEqualTo(FlightStatus.DELAYED);
        assertThat(delayedResp.getDelayMinutes()).isEqualTo(60);
        assertThat(delayedResp.getDelayReason()).isEqualTo("Severe storm system across flight corridor");
        assertThat(delayedResp.getRevisedDepartureTime()).isEqualTo(originalDep.plus(60, ChronoUnit.MINUTES));
        assertThat(delayedResp.getEstimatedArrival()).isEqualTo(originalArr.plus(60, ChronoUnit.MINUTES));

        // 3. Simulate BOARDING
        FlightResponse boardingResp = flightService.updateFlightStatus(flightIdA, FlightStatusUpdateRequest.builder()
                .status(FlightStatus.BOARDING)
                .build());

        assertThat(boardingResp.getStatus()).isEqualTo(FlightStatus.BOARDING);

        // 4. Simulate ON_TIME (Cleared for departure)
        FlightResponse onTimeResp = flightService.updateFlightStatus(flightIdA, FlightStatusUpdateRequest.builder()
                .status(FlightStatus.ON_TIME)
                .build());

        assertThat(onTimeResp.getStatus()).isEqualTo(FlightStatus.ON_TIME);

        // 5. Simulate DEPARTED
        FlightResponse departedResp = flightService.updateFlightStatus(flightIdA, FlightStatusUpdateRequest.builder()
                .status(FlightStatus.DEPARTED)
                .build());

        assertThat(departedResp.getStatus()).isEqualTo(FlightStatus.DEPARTED);

        // 6. Simulate ARRIVED
        FlightResponse arrivedResp = flightService.updateFlightStatus(flightIdA, FlightStatusUpdateRequest.builder()
                .status(FlightStatus.ARRIVED)
                .build());

        assertThat(arrivedResp.getStatus()).isEqualTo(FlightStatus.ARRIVED);
    }
}
