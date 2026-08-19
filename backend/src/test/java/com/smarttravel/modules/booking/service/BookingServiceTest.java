package com.smarttravel.modules.booking.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.mapper.BookingMapper;
import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.service.FareCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightInventoryReservationService reservationService;

    @Mock
    private FareCalculationService fareCalculationService;

    private final BookingStateMachine stateMachine = new BookingStateMachine();

    @Mock
    private PnrGenerator pnrGenerator;

    private final BookingMapper bookingMapper = new BookingMapper();

    private BookingService bookingService;

    private Flight sampleFlight;
    private Booking sampleBooking;
    private PassengerDto samplePassengerDto;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                flightRepository,
                reservationService,
                fareCalculationService,
                stateMachine,
                pnrGenerator,
                bookingMapper
        );

        CabinInventory econInventory = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(180)
                .availableSeats(20)
                .basePrice(new BigDecimal("5000.00"))
                .taxAmount(new BigDecimal("600.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5750.00"))
                .build();

        sampleFlight = Flight.builder()
                .id("fl-123")
                .flightNumber("AI-101")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(AirportInfo.builder().code("DEL").name("Delhi").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").name("Mumbai").city("Mumbai").build())
                .departureTime(Instant.now().plusSeconds(86400))
                .arrivalTime(Instant.now().plusSeconds(93600))
                .durationMinutes(120)
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(180)
                .availableSeats(20)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econInventory))
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .build();

        samplePassengerDto = PassengerDto.builder()
                .title("Mr")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .nationality("Indian")
                .build();

        sampleBooking = Booking.builder()
                .id("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-1")
                .userEmail("john.doe@example.com")
                .flightId("fl-123")
                .flightNumber("AI-101")
                .airline("Air India")
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(1)
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .status(BookingStatus.CONFIRMED)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Create booking successfully reserves seats, generates PNR, snapshots fare and persists")
    void testCreateBooking_Success() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId("fl-123")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(samplePassengerDto))
                .build();

        FareBreakdownDto fareDto = FareBreakdownDto.builder()
                .baseFare(new BigDecimal("5000.00"))
                .taxes(new BigDecimal("600.00"))
                .fees(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .passengerCount(1)
                .build();

        when(flightRepository.findByIdAndActiveTrue("fl-123")).thenReturn(Optional.of(sampleFlight));
        when(reservationService.reserveSeats("fl-123", CabinClass.ECONOMY, 1)).thenReturn(true);
        when(fareCalculationService.calculateFare(any(BigDecimal.class), eq(CabinClass.ECONOMY), eq(1))).thenReturn(fareDto);
        when(pnrGenerator.generatePnr()).thenReturn("ST8K4P2Q");
        when(bookingRepository.existsByBookingReference("ST8K4P2Q")).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId("bk-100");
            return b;
        });

        BookingResponse response = bookingService.createBooking(request, "user-1", "john.doe@example.com");

        assertNotNull(response);
        assertEquals("bk-100", response.getId());
        assertEquals("ST8K4P2Q", response.getBookingReference());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals(new BigDecimal("5750.00"), response.getTotalAmount());
        assertEquals(1, response.getPassengerCount());
        verify(reservationService).reserveSeats("fl-123", CabinClass.ECONOMY, 1);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    @DisplayName("Create booking throws ResourceNotFoundException if flight does not exist or is inactive")
    void testCreateBooking_FlightNotFound() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId("fl-unknown")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(samplePassengerDto))
                .build();

        when(flightRepository.findByIdAndActiveTrue("fl-unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBooking(request, "user-1", "john.doe@example.com"));
        verify(reservationService, never()).reserveSeats(anyString(), any(CabinClass.class), anyInt());
    }

    @Test
    @DisplayName("Create booking throws BadRequestException if flight status is not bookable")
    void testCreateBooking_NonBookableStatus() {
        sampleFlight.setStatus(FlightStatus.CANCELLED);
        when(flightRepository.findByIdAndActiveTrue("fl-123")).thenReturn(Optional.of(sampleFlight));

        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId("fl-123")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(samplePassengerDto))
                .build();

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(request, "user-1", "john.doe@example.com"));
        verify(reservationService, never()).reserveSeats(anyString(), any(CabinClass.class), anyInt());
    }

    @Test
    @DisplayName("Create booking throws ConflictException when atomic seat reservation fails")
    void testCreateBooking_InsufficientInventory() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId("fl-123")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(samplePassengerDto))
                .build();

        when(flightRepository.findByIdAndActiveTrue("fl-123")).thenReturn(Optional.of(sampleFlight));
        when(reservationService.reserveSeats("fl-123", CabinClass.ECONOMY, 1)).thenReturn(false);

        assertThrows(ConflictException.class,
                () -> bookingService.createBooking(request, "user-1", "john.doe@example.com"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    @DisplayName("Create booking executes compensating seat release if booking persistence throws exception")
    void testCreateBooking_CompensatingSeatReleaseOnSaveFailure() {
        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId("fl-123")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(samplePassengerDto))
                .build();

        FareBreakdownDto fareDto = FareBreakdownDto.builder()
                .baseFare(new BigDecimal("5000.00"))
                .taxes(new BigDecimal("600.00"))
                .fees(new BigDecimal("150.00"))
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .passengerCount(1)
                .build();

        when(flightRepository.findByIdAndActiveTrue("fl-123")).thenReturn(Optional.of(sampleFlight));
        when(reservationService.reserveSeats("fl-123", CabinClass.ECONOMY, 1)).thenReturn(true);
        when(fareCalculationService.calculateFare(any(BigDecimal.class), eq(CabinClass.ECONOMY), eq(1))).thenReturn(fareDto);
        when(pnrGenerator.generatePnr()).thenReturn("ST8K4P2Q");
        when(bookingRepository.save(any(Booking.class))).thenThrow(new RuntimeException("Database disk failure"));

        assertThrows(RuntimeException.class,
                () -> bookingService.createBooking(request, "user-1", "john.doe@example.com"));

        // Crucial: Must execute compensating release so seats are not leaked
        verify(reservationService).releaseSeats("fl-123", CabinClass.ECONOMY, 1);
    }

    @Test
    @DisplayName("Get booking by ID enforces user ownership")
    void testGetBookingById_Ownership() {
        when(bookingRepository.findByIdAndUserId("bk-100", "user-1")).thenReturn(Optional.of(sampleBooking));

        BookingResponse response = bookingService.getBookingById("bk-100", "user-1", false);
        assertNotNull(response);
        assertEquals("bk-100", response.getId());

        when(bookingRepository.findByIdAndUserId("bk-100", "user-2")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingById("bk-100", "user-2", false));
    }

    @Test
    @DisplayName("Admin can retrieve any booking by ID")
    void testGetBookingById_Admin() {
        when(bookingRepository.findById("bk-100")).thenReturn(Optional.of(sampleBooking));

        BookingResponse response = bookingService.getBookingById("bk-100", null, true);
        assertNotNull(response);
        assertEquals("bk-100", response.getId());
    }

    @Test
    @DisplayName("Get booking by PNR reference enforces user ownership")
    void testGetBookingByReference_Ownership() {
        when(bookingRepository.findByBookingReferenceAndUserId("ST8K4P2Q", "user-1")).thenReturn(Optional.of(sampleBooking));

        BookingResponse response = bookingService.getBookingByReference("  st8k4p2q  ", "user-1", false);
        assertNotNull(response);
        assertEquals("ST8K4P2Q", response.getBookingReference());

        when(bookingRepository.findByBookingReferenceAndUserId("ST8K4P2Q", "user-2")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.getBookingByReference("ST8K4P2Q", "user-2", false));
    }

    @Test
    @DisplayName("Cancel booking updates status to CANCELLED and atomically releases reserved seats")
    void testCancelBooking_Success() {
        when(bookingRepository.findByIdAndUserId("bk-100", "user-1")).thenReturn(Optional.of(sampleBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(reservationService.releaseSeats("fl-123", CabinClass.ECONOMY, 1)).thenReturn(true);

        BookingCancelRequest cancelReq = new BookingCancelRequest("Change of travel plans");
        BookingResponse response = bookingService.cancelBooking("bk-100", cancelReq, "user-1", false);

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals("Change of travel plans", response.getCancellationReason());
        verify(reservationService).releaseSeats("fl-123", CabinClass.ECONOMY, 1);
        verify(bookingRepository).save(sampleBooking);
    }

    @Test
    @DisplayName("Cancel booking throws InvalidStateTransitionException if booking is already CANCELLED")
    void testCancelBooking_AlreadyCancelled() {
        sampleBooking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByIdAndUserId("bk-100", "user-1")).thenReturn(Optional.of(sampleBooking));

        BookingCancelRequest cancelReq = new BookingCancelRequest("Already cancelled");
        assertThrows(InvalidStateTransitionException.class,
                () -> bookingService.cancelBooking("bk-100", cancelReq, "user-1", false));

        verify(reservationService, never()).releaseSeats(anyString(), any(CabinClass.class), anyInt());
    }

    @Test
    @DisplayName("Get user bookings returns paginated responses")
    void testGetUserBookings() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Booking> page = new PageImpl<>(List.of(sampleBooking), pageable, 1);
        when(bookingRepository.findByUserId("user-1", pageable)).thenReturn(page);

        PageResponse<BookingResponse> response = bookingService.getUserBookings("user-1", pageable);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("ST8K4P2Q", response.getContent().get(0).getBookingReference());
    }
}
