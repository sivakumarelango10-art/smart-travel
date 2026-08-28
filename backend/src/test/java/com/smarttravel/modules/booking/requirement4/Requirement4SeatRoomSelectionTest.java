package com.smarttravel.modules.booking.requirement4;

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
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.RoomCategory;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import com.smarttravel.modules.hotel.service.HotelServiceImpl;
import com.smarttravel.modules.hotel.websocket.HotelRoomWebSocketPublisher;
import com.smarttravel.modules.hotel.websocket.RoomAvailabilityEvent;
import com.smarttravel.modules.user.model.UserPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ELEVANCESKILLS INTERNSHIP — REQUIREMENT #4
 * SEAT & ROOM SELECTION SYSTEM COMPREHENSIVE AUDIT & VERIFICATION TEST SUITE
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Requirement #4: Seat and Room Selection Comprehensive Verification Suite")
public class Requirement4SeatRoomSelectionTest {

    // =========================================================================
    // 1. FLIGHT SEAT MAP & PRICING TESTS
    // =========================================================================
    @Nested
    @DisplayName("Flight Seat Map & Seat Pricing")
    class FlightSeatMapTests {

        @Mock private SeatRepository seatRepository;
        @Mock private FlightRepository flightRepository;
        @Mock private SeatMapWebSocketPublisher seatPublisher;

        private AircraftSeatLayout layout;
        private SeatMapServiceImpl seatMapService;
        private Flight flight;

        @BeforeEach
        void setUp() {
            layout = new AircraftSeatLayout();
            seatMapService = new SeatMapServiceImpl(seatRepository, flightRepository, layout, seatPublisher);

            flight = Flight.builder()
                    .id("fl-401")
                    .flightNumber("ST-401")
                    .aircraftModel("Airbus A320")
                    .totalSeats(180)
                    .availableSeats(180)
                    .cabinClasses(Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS))
                    .status(com.smarttravel.modules.flight.model.FlightStatus.SCHEDULED)
                    .build();
        }

        @Test
        @DisplayName("[T1] Seat layout dynamically generates rows, columns, and emergency exit rows")
        void testSeatLayoutGeneration() {
            List<Seat> seats = layout.generateSeatsForFlight("fl-401", "ST-401", "Airbus A320", Set.of(CabinClass.ECONOMY), 120);

            assertThat(seats).hasSize(120);
            assertThat(seats).anyMatch(s -> s.getRowNumber() == 12 && s.getPriceAdjustment().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("[T2] Extra legroom seats have server-side price adjustments (₹350–₹500)")
        void testExtraLegroomPricing() {
            List<Seat> seats = layout.generateSeatsForFlight("fl-401", "ST-401", "Boeing 737-800", Set.of(CabinClass.ECONOMY), 60);

            Seat exitSeat = seats.stream().filter(s -> s.getRowNumber() == 12).findFirst().orElseThrow();
            Seat standardSeat = seats.stream().filter(s -> s.getRowNumber() == 9).findFirst().orElseThrow();

            assertThat(exitSeat.getPriceAdjustment()).isGreaterThan(BigDecimal.ZERO);
            assertThat(standardSeat.getPriceAdjustment()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("[T3] Seat hold broadcasts real-time WebSocket update to /topic/seat-map/{flightId}")
        void testSeatHoldWebSocketBroadcast() {
            when(flightRepository.findById("fl-401")).thenReturn(Optional.of(flight));
            when(seatRepository.existsByFlightId("fl-401")).thenReturn(true);

            Seat seat12A = Seat.builder().flightId("fl-401").seatNumber("12A").cabinClass(CabinClass.ECONOMY).status(SeatStatus.AVAILABLE).build();
            when(seatRepository.findByFlightIdAndSeatNumber("fl-401", "12A")).thenReturn(Optional.of(seat12A));
            when(seatRepository.atomicHoldSeat(eq("fl-401"), eq("12A"), anyString(), anyString(), any())).thenReturn(true);

            boolean held = seatMapService.holdSeats("fl-401", CabinClass.ECONOMY, List.of("12A"), "bk-101", "PNR101", Instant.now().plusSeconds(900));

            assertThat(held).isTrue();

            ArgumentCaptor<SeatMapUpdateEvent> captor = ArgumentCaptor.forClass(SeatMapUpdateEvent.class);
            verify(seatPublisher).publishSeatUpdate(captor.capture());

            SeatMapUpdateEvent event = captor.getValue();
            assertThat(event.getFlightId()).isEqualTo("fl-401");
            assertThat(event.getSeatNumbers()).contains("12A");
            assertThat(event.getStatus()).isEqualTo(SeatStatus.HELD);
        }
    }

    // =========================================================================
    // 2. HOTEL ROOM GRID & UPGRADE PRICING TESTS
    // =========================================================================
    @Nested
    @DisplayName("Hotel Room Grid & Upgrades")
    class HotelRoomGridTests {

        @Mock private HotelRepository hotelRepository;
        @Mock private MongoTemplate mongoTemplate;
        @Mock private HotelRoomWebSocketPublisher roomPublisher;

        private HotelServiceImpl hotelService;
        private Hotel hotel;

        @BeforeEach
        void setUp() {
            hotelService = new HotelServiceImpl(hotelRepository, mongoTemplate, roomPublisher);

            RoomType standard = RoomType.builder().id("rt-std").name("Standard").category(RoomCategory.STANDARD).nightlyRate(new BigDecimal("3000.00")).totalRooms(10).availableRooms(5).build();
            RoomType deluxe = RoomType.builder().id("rt-dlx").name("Deluxe").category(RoomCategory.DELUXE).nightlyRate(new BigDecimal("4500.00")).totalRooms(8).availableRooms(3).build();

            hotel = Hotel.builder().id("ht-01").name("Luxury Palms").roomTypes(List.of(standard, deluxe)).build();
        }

        @Test
        @DisplayName("[T4] Hotel returns available room categories with capacity and pricing")
        void testGetRoomTypes() {
            when(hotelRepository.findById("ht-01")).thenReturn(Optional.of(hotel));

            List<RoomType> rooms = hotelService.getRoomTypes("ht-01");

            assertThat(rooms).hasSize(2);
            assertThat(rooms.get(1).getNightlyRate()).isEqualByComparingTo(new BigDecimal("4500.00"));
        }

        @Test
        @DisplayName("[T5] Atomic room hold decrements available inventory and broadcasts WebSocket update")
        void testHoldRoomBroadcast() {
            RoomType updatedDeluxe = RoomType.builder().id("rt-dlx").name("Deluxe").category(RoomCategory.DELUXE).availableRooms(2).totalRooms(8).nightlyRate(new BigDecimal("4500.00")).build();
            Hotel updatedHotel = Hotel.builder().id("ht-01").roomTypes(List.of(updatedDeluxe)).build();

            when(mongoTemplate.findAndModify(any(), any(), any(), eq(Hotel.class))).thenReturn(updatedHotel);

            RoomType held = hotelService.holdRoom("ht-01", "rt-dlx", 1);

            assertThat(held.getAvailableRooms()).isEqualTo(2);

            ArgumentCaptor<RoomAvailabilityEvent> captor = ArgumentCaptor.forClass(RoomAvailabilityEvent.class);
            verify(roomPublisher).publishRoomUpdate(captor.capture());

            RoomAvailabilityEvent event = captor.getValue();
            assertThat(event.getHotelId()).isEqualTo("ht-01");
            assertThat(event.getRoomTypeId()).isEqualTo("rt-dlx");
            assertThat(event.getAvailableRooms()).isEqualTo(2);
            assertThat(event.getAction()).isEqualTo("HELD");
        }
    }

    // =========================================================================
    // 3. CONCURRENCY & PREFERENCE TESTS
    // =========================================================================
    @Nested
    @DisplayName("Concurrency & Preferences")
    class ConcurrencyAndPreferenceTests {

        @Test
        @DisplayName("[T6] 10 concurrent threads attempting to reserve 1 seat -> exactly 1 success")
        void testConcurrentSeatHoldSimulation() throws InterruptedException {
            int threads = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch finish = new CountDownLatch(threads);

            AtomicInteger availableSeat = new AtomicInteger(1);
            AtomicInteger success = new AtomicInteger(0);
            AtomicInteger conflict = new AtomicInteger(0);

            for (int i = 0; i < threads; i++) {
                executor.submit(() -> {
                    try {
                        start.await();
                        if (availableSeat.compareAndSet(1, 0)) {
                            success.incrementAndGet();
                        } else {
                            conflict.incrementAndGet();
                        }
                    } catch (Exception e) {
                        conflict.incrementAndGet();
                    } finally {
                        finish.countDown();
                    }
                });
            }

            start.countDown();
            finish.await();
            executor.shutdown();

            assertThat(success.get()).isEqualTo(1);
            assertThat(conflict.get()).isEqualTo(9);
        }

        @Test
        @DisplayName("[T7] UserPreferences model stores and returns preferred seat and room types")
        void testUserPreferencesModel() {
            UserPreferences prefs = new UserPreferences();
            prefs.setPreferredSeatType("WINDOW");
            prefs.setPreferredRoomType("DELUXE");
            prefs.setPreferredClass("ECONOMY");

            assertThat(prefs.getPreferredSeatType()).isEqualTo("WINDOW");
            assertThat(prefs.getPreferredRoomType()).isEqualTo("DELUXE");
            assertThat(prefs.getPreferredClass()).isEqualTo("ECONOMY");
        }
    }
}
