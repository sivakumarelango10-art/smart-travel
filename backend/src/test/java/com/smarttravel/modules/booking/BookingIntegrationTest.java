package com.smarttravel.modules.booking;

import com.smarttravel.common.exception.ResourceNotFoundException;

import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest;
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
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class BookingIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    private String flightId;
    private final List<String> createdBookingIds = new ArrayList<>();

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
                .totalSeats(20)
                .availableSeats(5)
                .basePrice(new BigDecimal("15000.00"))
                .taxAmount(new BigDecimal("1800.00"))
                .feeAmount(new BigDecimal("300.00"))
                .totalPrice(new BigDecimal("17100.00"))
                .build();

        FlightCreateRequest flightReq = FlightCreateRequest.builder()
                .flightNumber("TEST-BKG-" + System.currentTimeMillis())
                .airline("SmartAir")
                .airlineCode("SA")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.now().plusSeconds(172800))
                .arrivalTime(Instant.now().plusSeconds(180000))
                .aircraftModel("A321neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(120)
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
    @DisplayName("Create booking atomically reserves seats and is retrievable by ID and PNR")
    void testCreateAndRetrieveBooking() {
        PassengerDto p1 = PassengerDto.builder()
                .title("Mr")
                .firstName("Alice")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1992, 4, 10))
                .gender("FEMALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p1))
                .build();

        BookingResponse response = bookingService.createBooking(request, "user-alice", "alice@example.com");
        createdBookingIds.add(response.getId());

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotBlank();
        assertThat(response.getBookingReference()).isNotBlank();
        assertThat(response.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.getTotalAmount()).isEqualByComparingTo("5750.00");

        // Verify retrieval by ID
        BookingResponse byId = bookingService.getBookingById(response.getId(), "user-alice", false);
        assertThat(byId.getBookingReference()).isEqualTo(response.getBookingReference());

        // Verify retrieval by PNR
        BookingResponse byRef = bookingService.getBookingByReference(response.getBookingReference(), "user-alice", false);
        assertThat(byRef.getId()).isEqualTo(response.getId());

        // Verify database flight inventory decremented
        Flight updatedFlight = flightRepository.findById(flightId).orElseThrow();
        assertThat(updatedFlight.getAvailableSeats()).isEqualTo(104);
        assertThat(updatedFlight.getCabinInventories().get(0).getAvailableSeats()).isEqualTo(99);
    }

    @Test
    @DisplayName("Price snapshot immutability: Flight price increase does not alter existing booking price")
    void testFareSnapshotImmutability() {
        PassengerDto p1 = PassengerDto.builder()
                .title("Mr")
                .firstName("Bob")
                .lastName("Jones")
                .dateOfBirth(LocalDate.of(1985, 8, 20))
                .gender("MALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.BUSINESS)
                .passengers(List.of(p1))
                .build();

        BookingResponse initialBooking = bookingService.createBooking(request, "user-bob", "bob@example.com");
        createdBookingIds.add(initialBooking.getId());
        BigDecimal initialPrice = initialBooking.getTotalAmount();
        assertThat(initialPrice).isEqualByComparingTo("17100.00");

        // Mutate current flight price in catalog
        CabinInventoryDto updatedBus = CabinInventoryDto.builder()
                .cabinClass(CabinClass.BUSINESS)
                .totalSeats(20)
                .availableSeats(4)
                .basePrice(new BigDecimal("30000.00"))
                .taxAmount(new BigDecimal("3600.00"))
                .feeAmount(new BigDecimal("300.00"))
                .totalPrice(new BigDecimal("33900.00"))
                .build();

        flightService.updateFlightInventory(flightId, new FlightInventoryUpdateRequest(List.of(
                CabinInventoryDto.builder().cabinClass(CabinClass.ECONOMY).totalSeats(100).availableSeats(100).basePrice(new BigDecimal("5000.00")).totalPrice(new BigDecimal("5750.00")).build(),
                updatedBus
        )));

        // Retrieve existing booking and assert price remains unchanged
        BookingResponse retrieved = bookingService.getBookingById(initialBooking.getId(), "user-bob", false);
        assertThat(retrieved.getTotalAmount()).isEqualByComparingTo("17100.00");
        assertThat(retrieved.getFareBreakdown().getBaseFare()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("Cabin isolation & cancellation: Business booking affects only Business cabin, cancellation releases back")
    void testCabinIsolationAndCancellation() {
        PassengerDto p1 = PassengerDto.builder().title("Mr").firstName("P1").lastName("L1").dateOfBirth(LocalDate.of(1990, 1, 1)).gender("MALE").build();
        PassengerDto p2 = PassengerDto.builder().title("Mrs").firstName("P2").lastName("L2").dateOfBirth(LocalDate.of(1992, 2, 2)).gender("FEMALE").build();

        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.BUSINESS)
                .passengers(List.of(p1, p2))
                .build();

        BookingResponse response = bookingService.createBooking(request, "user-iso", "iso@example.com");
        createdBookingIds.add(response.getId());

        // Check flight availability: Economy should remain 100, Business should be 5 - 2 = 3
        Flight afterBook = flightRepository.findById(flightId).orElseThrow();
        assertThat(afterBook.getCabinInventories().stream().filter(c -> c.getCabinClass() == CabinClass.ECONOMY).findFirst().orElseThrow().getAvailableSeats()).isEqualTo(100);
        assertThat(afterBook.getCabinInventories().stream().filter(c -> c.getCabinClass() == CabinClass.BUSINESS).findFirst().orElseThrow().getAvailableSeats()).isEqualTo(3);

        // Cancel booking
        BookingResponse cancelled = bookingService.cancelBooking(response.getId(), new BookingCancelRequest("Testing cancellation"), "user-iso", false);
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        // Check flight availability: Business should be restored to 5, Economy should still be 100
        Flight afterCancel = flightRepository.findById(flightId).orElseThrow();
        assertThat(afterCancel.getCabinInventories().stream().filter(c -> c.getCabinClass() == CabinClass.ECONOMY).findFirst().orElseThrow().getAvailableSeats()).isEqualTo(100);
        assertThat(afterCancel.getCabinInventories().stream().filter(c -> c.getCabinClass() == CabinClass.BUSINESS).findFirst().orElseThrow().getAvailableSeats()).isEqualTo(5);
    }

    @Test
    @DisplayName("Ownership security: User cannot access another user's booking by ID or PNR")
    void testOwnershipSecurity() {
        PassengerDto p1 = PassengerDto.builder().title("Mr").firstName("P1").lastName("L1").dateOfBirth(LocalDate.of(1990, 1, 1)).gender("MALE").build();
        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId(flightId)
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(p1))
                .build();

        BookingResponse response = bookingService.createBooking(request, "owner-user", "owner@example.com");
        createdBookingIds.add(response.getId());

        // Attacker attempting access by ID -> 404
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingById(response.getId(), "attacker-user", false));

        // Attacker attempting access by PNR -> 404
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingByReference(response.getBookingReference(), "attacker-user", false));

        // Attacker attempting cancellation -> 404
        assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking(response.getId(), new BookingCancelRequest(), "attacker-user", false));
    }
}
