package com.smarttravel.performance;

import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.repository.UserRepository;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.hotel.service.HotelService;
import com.smarttravel.modules.notification.service.NotificationService;
import com.smarttravel.modules.recommendation.service.RecommendationService;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class PlatformPerformanceBenchmarkTest {

    private static final Logger log = LoggerFactory.getLogger(PlatformPerformanceBenchmarkTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String authToken;
    private String testUserId;

    @BeforeEach
    void setupAuth() {
        testUserId = "perf-user-01";
        User user = userRepository.findByEmail("perf.bench@smarttravel.com").orElseGet(() -> {
            User u = User.builder()
                    .id(testUserId)
                    .email("perf.bench@smarttravel.com")
                    .firstName("Perf")
                    .lastName("Tester")
                    .passwordHash("Password123!")
                    .roles(Set.of(Role.ROLE_USER))
                    .active(true)
                    .build();
            return userRepository.save(u);
        });
        authToken = "Bearer " + jwtTokenProvider.generateTokenFromUserIdAndEmail(user.getId(), user.getEmail(), List.of("ROLE_USER"));
    }

    public static class PerfStats {
        public double min;
        public double p50;
        public double p95;
        public double p99;
        public double max;
        public double avg;
        public double coldFirstReq;
    }

    private PerfStats computeStats(List<Long> rawNanos, long firstColdNanos) {
        List<Long> latencies = new ArrayList<>(rawNanos);
        Collections.sort(latencies);
        int n = latencies.size();
        PerfStats s = new PerfStats();
        s.min = latencies.get(0) / 1_000_000.0;
        s.max = latencies.get(n - 1) / 1_000_000.0;
        s.p50 = latencies.get((int) (n * 0.50)) / 1_000_000.0;
        s.p95 = latencies.get((int) (n * 0.95)) / 1_000_000.0;
        s.p99 = latencies.get(Math.min(n - 1, (int) (n * 0.99))) / 1_000_000.0;
        s.avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0) / 1_000_000.0;
        s.coldFirstReq = firstColdNanos / 1_000_000.0;
        return s;
    }

    @Test
    @DisplayName("Performance Benchmark: Live Measurement of DB/Service and Full HTTP Stack Latencies")
    void measureEndpointLatencies() throws Exception {
        int sampleSize = 30;

        // ══════════════════════════════════════════════════════════════════════
        // PART 1: SERVICE & DB LAYER BENCHMARK (Optimized Indexes & Queries)
        // ══════════════════════════════════════════════════════════════════════
        // 1. Flight Search Service
        FlightSearchCriteria criteria = new FlightSearchCriteria();
        criteria.setOrigin("DEL");
        criteria.setDestination("BOM");
        criteria.setPage(0);
        criteria.setSize(20);

        long flightCold = 0;
        List<Long> flightSearchDbTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            flightService.searchFlights(criteria);
            long elapsed = System.nanoTime() - t0;
            if (i == 0) flightCold = elapsed;
            flightSearchDbTimes.add(elapsed);
        }
        PerfStats flightDbStats = computeStats(flightSearchDbTimes, flightCold);

        // 2. Hotel Search Service
        long hotelCold = 0;
        List<Long> hotelSearchDbTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            hotelService.searchHotels("Delhi", null, null, null, PageRequest.of(0, 10));
            long elapsed = System.nanoTime() - t0;
            if (i == 0) hotelCold = elapsed;
            hotelSearchDbTimes.add(elapsed);
        }
        PerfStats hotelDbStats = computeStats(hotelSearchDbTimes, hotelCold);

        // 3. Booking Lookup Service
        long bookingCold = 0;
        List<Long> bookingDbTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            bookingService.getUserBookings(testUserId, PageRequest.of(0, 10));
            long elapsed = System.nanoTime() - t0;
            if (i == 0) bookingCold = elapsed;
            bookingDbTimes.add(elapsed);
        }
        PerfStats bookingDbStats = computeStats(bookingDbTimes, bookingCold);

        // 4. Notifications Lookup Service
        long notifCold = 0;
        List<Long> notifDbTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            notificationService.getUserNotifications(testUserId, PageRequest.of(0, 10));
            long elapsed = System.nanoTime() - t0;
            if (i == 0) notifCold = elapsed;
            notifDbTimes.add(elapsed);
        }
        PerfStats notifDbStats = computeStats(notifDbTimes, notifCold);

        // 5. Recommendations Service
        long recCold = 0;
        List<Long> recDbTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            recommendationService.getRecommendations(testUserId, 6);
            long elapsed = System.nanoTime() - t0;
            if (i == 0) recCold = elapsed;
            recDbTimes.add(elapsed);
        }
        PerfStats recDbStats = computeStats(recDbTimes, recCold);

        // 6. Reviews Lookup Service
        long revCold = 0;
        List<Long> revDbTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            reviewService.getReviewsForTarget(ReviewTargetType.HOTEL, "hotel-delhi-01", PageRequest.of(0, 10));
            long elapsed = System.nanoTime() - t0;
            if (i == 0) revCold = elapsed;
            revDbTimes.add(elapsed);
        }
        PerfStats revDbStats = computeStats(revDbTimes, revCold);

        // ══════════════════════════════════════════════════════════════════════
        // PART 2: FULL HTTP REST STACK BENCHMARK (MockMvc DispatcherServlet)
        // ══════════════════════════════════════════════════════════════════════
        // 1. GET /api/v1/flights/search
        long httpFlightCold = 0;
        List<Long> httpFlightTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            mockMvc.perform(get("/api/v1/flights/search")
                            .param("origin", "DEL")
                            .param("destination", "BOM")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            long elapsed = System.nanoTime() - t0;
            if (i == 0) httpFlightCold = elapsed;
            httpFlightTimes.add(elapsed);
        }
        PerfStats httpFlightStats = computeStats(httpFlightTimes, httpFlightCold);

        // 2. GET /api/v1/hotels/search
        long httpHotelCold = 0;
        List<Long> httpHotelTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            mockMvc.perform(get("/api/v1/hotels/search")
                            .param("city", "Delhi")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            long elapsed = System.nanoTime() - t0;
            if (i == 0) httpHotelCold = elapsed;
            httpHotelTimes.add(elapsed);
        }
        PerfStats httpHotelStats = computeStats(httpHotelTimes, httpHotelCold);

        // 3. GET /api/v1/bookings/my-bookings (Secured)
        long httpBookingCold = 0;
        List<Long> httpBookingTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            mockMvc.perform(get("/api/v1/bookings/my-bookings")
                            .header("Authorization", authToken)
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            long elapsed = System.nanoTime() - t0;
            if (i == 0) httpBookingCold = elapsed;
            httpBookingTimes.add(elapsed);
        }
        PerfStats httpBookingStats = computeStats(httpBookingTimes, httpBookingCold);

        // 4. GET /api/v1/notifications (Secured)
        long httpNotifCold = 0;
        List<Long> httpNotifTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            mockMvc.perform(get("/api/v1/notifications")
                            .header("Authorization", authToken)
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            long elapsed = System.nanoTime() - t0;
            if (i == 0) httpNotifCold = elapsed;
            httpNotifTimes.add(elapsed);
        }
        PerfStats httpNotifStats = computeStats(httpNotifTimes, httpNotifCold);

        // 5. GET /api/v1/recommendations/flights
        long httpRecCold = 0;
        List<Long> httpRecTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            mockMvc.perform(get("/api/v1/recommendations/flights")
                            .param("limit", "6")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            long elapsed = System.nanoTime() - t0;
            if (i == 0) httpRecCold = elapsed;
            httpRecTimes.add(elapsed);
        }
        PerfStats httpRecStats = computeStats(httpRecTimes, httpRecCold);

        // 6. GET /api/v1/reviews/target/HOTEL/hotel-delhi-01
        long httpRevCold = 0;
        List<Long> httpRevTimes = new ArrayList<>();
        for (int i = 0; i < sampleSize; i++) {
            long t0 = System.nanoTime();
            mockMvc.perform(get("/api/v1/reviews/target/HOTEL/hotel-delhi-01")
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            long elapsed = System.nanoTime() - t0;
            if (i == 0) httpRevCold = elapsed;
            httpRevTimes.add(elapsed);
        }
        PerfStats httpRevStats = computeStats(httpRevTimes, httpRevCold);

        log.info("==========================================================================================================");
        log.info("=== SMARTTRAVEL BENCHMARK: DATABASE & SERVICE LATENCY (Atlas AP-SOUTH-1 Live DB)                         ===");
        log.info("==========================================================================================================");
        log.info(String.format("1. Flight Search DB -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                flightDbStats.coldFirstReq, flightDbStats.min, flightDbStats.p50, flightDbStats.p95, flightDbStats.p99, flightDbStats.max, flightDbStats.avg));
        log.info(String.format("2. Hotel Search DB  -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                hotelDbStats.coldFirstReq, hotelDbStats.min, hotelDbStats.p50, hotelDbStats.p95, hotelDbStats.p99, hotelDbStats.max, hotelDbStats.avg));
        log.info(String.format("3. Booking DB       -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                bookingDbStats.coldFirstReq, bookingDbStats.min, bookingDbStats.p50, bookingDbStats.p95, bookingDbStats.p99, bookingDbStats.max, bookingDbStats.avg));
        log.info(String.format("4. Notifications DB -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                notifDbStats.coldFirstReq, notifDbStats.min, notifDbStats.p50, notifDbStats.p95, notifDbStats.p99, notifDbStats.max, notifDbStats.avg));
        log.info(String.format("5. Recomms DB       -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                recDbStats.coldFirstReq, recDbStats.min, recDbStats.p50, recDbStats.p95, recDbStats.p99, recDbStats.max, recDbStats.avg));
        log.info(String.format("6. Reviews DB       -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                revDbStats.coldFirstReq, revDbStats.min, revDbStats.p50, revDbStats.p95, revDbStats.p99, revDbStats.max, revDbStats.avg));
        log.info("==========================================================================================================");
        log.info("=== SMARTTRAVEL BENCHMARK: FULL HTTP REST ENDPOINT LATENCY (MockMvc Stack + Filters + Jackson)           ===");
        log.info("==========================================================================================================");
        log.info(String.format("1. GET /flights     -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                httpFlightStats.coldFirstReq, httpFlightStats.min, httpFlightStats.p50, httpFlightStats.p95, httpFlightStats.p99, httpFlightStats.max, httpFlightStats.avg));
        log.info(String.format("2. GET /hotels      -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                httpHotelStats.coldFirstReq, httpHotelStats.min, httpHotelStats.p50, httpHotelStats.p95, httpHotelStats.p99, httpHotelStats.max, httpHotelStats.avg));
        log.info(String.format("3. GET /my-bookings -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                httpBookingStats.coldFirstReq, httpBookingStats.min, httpBookingStats.p50, httpBookingStats.p95, httpBookingStats.p99, httpBookingStats.max, httpBookingStats.avg));
        log.info(String.format("4. GET /notifs      -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                httpNotifStats.coldFirstReq, httpNotifStats.min, httpNotifStats.p50, httpNotifStats.p95, httpNotifStats.p99, httpNotifStats.max, httpNotifStats.avg));
        log.info(String.format("5. GET /recomms     -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                httpRecStats.coldFirstReq, httpRecStats.min, httpRecStats.p50, httpRecStats.p95, httpRecStats.p99, httpRecStats.max, httpRecStats.avg));
        log.info(String.format("6. GET /reviews     -> Cold: %.2f ms | Min: %.2f ms | p50: %.2f ms | p95: %.2f ms | p99: %.2f ms | Max: %.2f ms | Avg: %.2f ms",
                httpRevStats.coldFirstReq, httpRevStats.min, httpRevStats.p50, httpRevStats.p95, httpRevStats.p99, httpRevStats.max, httpRevStats.avg));
        log.info("==========================================================================================================");
    }
}
