package com.smarttravel.modules.booking;

import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingExpirationService;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingExpirationIntegrationTest {

    @Autowired
    private BookingExpirationService expirationService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    private String flightId;
    private String bookingId;

    @BeforeEach
    void setUp() {
        AirportDto del = AirportDto.builder().code("DEL").name("Delhi Airport").city("Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("Mumbai Airport").city("Mumbai").country("India").build();

        CabinInventoryDto econ = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(100)
                .basePrice(new BigDecimal("4000.00"))
                .taxAmount(new BigDecimal("480.00"))
                .feeAmount(new BigDecimal("120.00"))
                .totalPrice(new BigDecimal("4600.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("TEST-EXP-" + System.currentTimeMillis())
                .airline("SmartAir Expiry")
                .airlineCode("SX")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(172800))
                .arrivalTime(Instant.now().plusSeconds(180000))
                .aircraftModel("A320neo")
                .basePrice(new BigDecimal("4000.00"))
                .totalSeats(100)
                .availableSeats(100)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse flightRes = flightService.createFlight(flightReq);
        flightId = flightRes.getId();

        PassengerDto p1 = PassengerDto.builder().title("Mr").firstName("Tom").lastName("Hardy").dateOfBirth(LocalDate.of(1985, 4, 12)).gender("MALE").nationality("Indian").build();
        PassengerDto p2 = PassengerDto.builder().title("Ms").firstName("Emma").lastName("Watson").dateOfBirth(LocalDate.of(1990, 9, 21)).gender("FEMALE").nationality("Indian").build();

        BookingCreateRequest bkgReq = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p1, p2))
                .build();

        BookingResponse bkgRes = bookingService.createBooking(bkgReq, "user-tom", "tom@smarttravel.com");
        bookingId = bkgRes.getId();

        // Check that seats are reserved (100 - 2 = 98)
        Flight flightAfterBooking = flightRepository.findById(flightId).orElseThrow();
        assertThat(flightAfterBooking.getAvailableSeats()).isEqualTo(98);

        // Mark booking as overdue PENDING
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        booking.setStatus(BookingStatus.PENDING);
        booking.setExpiresAt(Instant.now().minusSeconds(120));
        bookingRepository.save(booking);
    }

    @AfterEach
    void tearDown() {
        if (bookingId != null) {
            try {
                bookingRepository.deleteById(bookingId);
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
    @DisplayName("End-to-End Expiration: Overdue booking transitions to EXPIRED and cabin seats are restored")
    void testBookingExpirationAndSeatRelease_EndToEnd() {
        int expiredCount = expirationService.expireOverdueBookings();

        assertThat(expiredCount).isGreaterThanOrEqualTo(1);

        // 1. Verify Booking in MongoDB is EXPIRED
        Booking expiredBooking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(expiredBooking.getStatus()).isEqualTo(BookingStatus.EXPIRED);

        // 2. Verify Flight seats are restored to 100 in MongoDB
        Flight flightAfterExpiry = flightRepository.findById(flightId).orElseThrow();
        assertThat(flightAfterExpiry.getAvailableSeats()).isEqualTo(100);
        assertThat(flightAfterExpiry.getCabinInventories().get(0).getAvailableSeats()).isEqualTo(100);
    }
}
