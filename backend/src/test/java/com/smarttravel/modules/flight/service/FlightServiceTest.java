package com.smarttravel.modules.flight.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.DuplicateResourceException;
import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.dto.FlightUpdateRequest;
import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.model.FlightStatusHistory;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.FlightStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private FlightStatusHistoryRepository flightStatusHistoryRepository;

    @Spy
    private FlightStateMachine flightStateMachine = new FlightStateMachine();

    @Spy
    private FareCalculationService fareCalculationService = new FareCalculationServiceImpl();

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight sampleFlight;
    private Instant now;
    private Instant departureTime;
    private Instant arrivalTime;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        departureTime = now.plus(2, ChronoUnit.DAYS);
        arrivalTime = departureTime.plus(2, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES);

        AirportInfo del = AirportInfo.builder()
                .code("DEL")
                .name("Indira Gandhi International Airport")
                .city("New Delhi")
                .country("India")
                .terminal("T3")
                .gate("12A")
                .build();

        AirportInfo bom = AirportInfo.builder()
                .code("BOM")
                .name("Chhatrapati Shivaji Maharaj International Airport")
                .city("Mumbai")
                .country("India")
                .terminal("T2")
                .gate("4B")
                .build();

        sampleFlight = Flight.builder()
                .id("flight-123")
                .flightNumber("AI-101")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .durationMinutes(135)
                .aircraftModel("Airbus A321neo")
                .basePrice(new BigDecimal("5400.00"))
                .totalSeats(180)
                .availableSeats(180)
                .cabinClasses(Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS))
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    @DisplayName("Create flight successfully calculates duration server-side and persists")
    void testCreateFlightSuccess() {
        AirportDto delDto = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").build();
        AirportDto bomDto = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").build();

        FlightCreateRequest request = FlightCreateRequest.builder()
                .flightNumber("ai-101")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(delDto)
                .arrivalAirport(bomDto)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .aircraftModel("Airbus A321neo")
                .basePrice(new BigDecimal("5400.00"))
                .totalSeats(180)
                .availableSeats(180)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .build();

        when(flightRepository.existsByFlightNumber("AI-101")).thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> {
            Flight f = invocation.getArgument(0);
            f.setId("generated-id-123");
            return f;
        });

        FlightResponse response = flightService.createFlight(request);

        assertNotNull(response);
        assertEquals("generated-id-123", response.getId());
        assertEquals("AI-101", response.getFlightNumber());
        assertEquals(135, response.getDurationMinutes());
        verify(flightRepository).save(any(Flight.class));
    }

    @Test
    @DisplayName("Create flight throws DuplicateResourceException if flight number already exists")
    void testCreateFlightDuplicateFlightNumber() {
        FlightCreateRequest request = FlightCreateRequest.builder()
                .flightNumber("AI-101")
                .build();

        when(flightRepository.existsByFlightNumber("AI-101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> flightService.createFlight(request));
    }

    @Test
    @DisplayName("Update flight status to DELAYED with valid reason, calculated timestamps, and audit history")
    void testUpdateFlightStatusToDelayedSuccess() {
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightStatusUpdateRequest request = FlightStatusUpdateRequest.builder()
                .status(FlightStatus.DELAYED)
                .delayMinutes(45)
                .delayReason("Severe weather conditions at origin airport")
                .build();

        FlightResponse response = flightService.updateFlightStatus("flight-123", request);

        assertNotNull(response);
        assertEquals(FlightStatus.DELAYED, response.getStatus());
        assertEquals(45, response.getDelayMinutes());
        assertEquals("Severe weather conditions at origin airport", response.getDelayReason());
        assertEquals(departureTime.plus(45, ChronoUnit.MINUTES), response.getRevisedDepartureTime());
        assertEquals(arrivalTime.plus(45, ChronoUnit.MINUTES), response.getEstimatedArrival());

        ArgumentCaptor<FlightStatusHistory> historyCaptor = ArgumentCaptor.forClass(FlightStatusHistory.class);
        verify(flightStatusHistoryRepository).save(historyCaptor.capture());
        FlightStatusHistory history = historyCaptor.getValue();
        assertEquals("flight-123", history.getFlightId());
        assertEquals("AI-101", history.getFlightNumber());
        assertEquals(FlightStatus.SCHEDULED, history.getPreviousStatus());
        assertEquals(FlightStatus.DELAYED, history.getNewStatus());
        assertEquals(45, history.getDelayMinutes());
    }

    @Test
    @DisplayName("Update flight status to DELAYED without delay reason throws BadRequestException")
    void testUpdateFlightStatusDelayedMissingReason() {
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));

        FlightStatusUpdateRequest request = FlightStatusUpdateRequest.builder()
                .status(FlightStatus.DELAYED)
                .delayMinutes(30)
                .delayReason("") // Blank reason
                .build();

        assertThrows(BadRequestException.class, () -> flightService.updateFlightStatus("flight-123", request));
    }

    @Test
    @DisplayName("Update flight status to DELAYED with negative delay minutes throws BadRequestException")
    void testUpdateFlightStatusDelayedNegativeDelay() {
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));

        FlightStatusUpdateRequest request = FlightStatusUpdateRequest.builder()
                .status(FlightStatus.DELAYED)
                .delayMinutes(-10)
                .delayReason("Gate maintenance")
                .build();

        assertThrows(BadRequestException.class, () -> flightService.updateFlightStatus("flight-123", request));
    }

    @Test
    @DisplayName("Update flight status with illegal transition (e.g. ARRIVED -> DELAYED) throws InvalidStateTransitionException")
    void testUpdateFlightStatusIllegalTransition() {
        sampleFlight.setStatus(FlightStatus.ARRIVED);
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));

        FlightStatusUpdateRequest request = FlightStatusUpdateRequest.builder()
                .status(FlightStatus.DELAYED)
                .delayMinutes(30)
                .delayReason("Technical fault")
                .build();

        assertThrows(InvalidStateTransitionException.class, () -> flightService.updateFlightStatus("flight-123", request));
    }

    @Test
    @DisplayName("Update flight status from DELAYED to BOARDING succeeds")
    void testDelayedToBoardingTransition() {
        sampleFlight.setStatus(FlightStatus.DELAYED);
        sampleFlight.setDelayMinutes(30);
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightStatusUpdateRequest request = new FlightStatusUpdateRequest(FlightStatus.BOARDING);
        FlightResponse response = flightService.updateFlightStatus("flight-123", request);

        assertEquals(FlightStatus.BOARDING, response.getStatus());
        verify(flightStatusHistoryRepository).save(any(FlightStatusHistory.class));
    }

    @Test
    @DisplayName("Update flight status to ON_TIME resets delay fields")
    void testUpdateFlightStatusToOnTimeResetsDelay() {
        sampleFlight.setStatus(FlightStatus.BOARDING);
        sampleFlight.setDelayMinutes(30);
        sampleFlight.setDelayReason("Weather");
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightStatusUpdateRequest request = new FlightStatusUpdateRequest(FlightStatus.ON_TIME);
        FlightResponse response = flightService.updateFlightStatus("flight-123", request);

        assertEquals(FlightStatus.ON_TIME, response.getStatus());
        assertEquals(0, response.getDelayMinutes());
        assertNull(response.getDelayReason());
        assertEquals(departureTime, response.getRevisedDepartureTime());
        assertEquals(arrivalTime, response.getEstimatedArrival());
    }

    @Test
    @DisplayName("Soft delete flight sets active=false")
    void testDeleteFlight() {
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        flightService.deleteFlight("flight-123");

        assertFalse(sampleFlight.isActive());
        verify(flightRepository).save(sampleFlight);
    }

    @Test
    @DisplayName("Get flight by ID returns flight response")
    void testGetFlightById() {
        when(flightRepository.findByIdAndActiveTrue("flight-123")).thenReturn(Optional.of(sampleFlight));

        FlightResponse response = flightService.getFlightById("flight-123");

        assertNotNull(response);
        assertEquals("flight-123", response.getId());
        assertEquals("AI-101", response.getFlightNumber());
    }

    @Test
    @DisplayName("Get flight by ID throws ResourceNotFoundException when not found or inactive")
    void testGetFlightById_NotFound() {
        when(flightRepository.findByIdAndActiveTrue("unknown-id")).thenReturn(Optional.empty());

        assertThrows(com.smarttravel.common.exception.ResourceNotFoundException.class,
                () -> flightService.getFlightById("unknown-id"));
    }

    @Test
    @DisplayName("Get flight by flight number normalizes whitespace and case")
    void testGetFlightByFlightNumber_Normalization() {
        when(flightRepository.findByFlightNumberAndActiveTrue("AI-101")).thenReturn(Optional.of(sampleFlight));

        FlightResponse response = flightService.getFlightByFlightNumber("  ai-101  ");

        assertNotNull(response);
        assertEquals("AI-101", response.getFlightNumber());
        verify(flightRepository).findByFlightNumberAndActiveTrue("AI-101");
    }

    @Test
    @DisplayName("Get flight by flight number throws ResourceNotFoundException when not found")
    void testGetFlightByFlightNumber_NotFound() {
        when(flightRepository.findByFlightNumberAndActiveTrue("AI-999")).thenReturn(Optional.empty());

        assertThrows(com.smarttravel.common.exception.ResourceNotFoundException.class,
                () -> flightService.getFlightByFlightNumber("AI-999"));
    }

    @Test
    @DisplayName("Search flights delegates to repository and maps to PageResponse")
    void testSearchFlights() {
        FlightSearchCriteria criteria = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("BOM")
                .build();

        Page<Flight> page = new PageImpl<>(List.of(sampleFlight));
        when(flightRepository.searchFlights(eq(criteria))).thenReturn(page);

        PageResponse<FlightResponse> result = flightService.searchFlights(criteria);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("AI-101", result.getContent().get(0).getFlightNumber());
    }

    @Test
    @DisplayName("Admin updates flight cabin inventories successfully")
    void testUpdateFlightInventory() {
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.smarttravel.modules.flight.dto.CabinInventoryDto economy = com.smarttravel.modules.flight.dto.CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(150)
                .availableSeats(120)
                .basePrice(new BigDecimal("4500.00"))
                .taxAmount(new BigDecimal("540.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5190.00"))
                .build();

        com.smarttravel.modules.flight.dto.CabinInventoryDto business = com.smarttravel.modules.flight.dto.CabinInventoryDto.builder()
                .cabinClass(CabinClass.BUSINESS)
                .totalSeats(30)
                .availableSeats(20)
                .basePrice(new BigDecimal("14000.00"))
                .taxAmount(new BigDecimal("1680.00"))
                .feeAmount(new BigDecimal("300.00"))
                .totalPrice(new BigDecimal("15980.00"))
                .build();

        com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest request =
                new com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest(List.of(economy, business));

        FlightResponse response = flightService.updateFlightInventory("flight-123", request);

        assertNotNull(response);
        assertEquals(180, response.getTotalSeats());
        assertEquals(140, response.getAvailableSeats());
        assertEquals(2, response.getCabinInventories().size());
    }

    @Test
    @DisplayName("Admin inventory update rejects available seats exceeding total seats")
    void testUpdateFlightInventory_InvalidSeats() {
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));

        com.smarttravel.modules.flight.dto.CabinInventoryDto economy = com.smarttravel.modules.flight.dto.CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(150)
                .basePrice(new BigDecimal("4500.00"))
                .build();

        com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest request =
                new com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest(List.of(economy));

        assertThrows(BadRequestException.class, () -> flightService.updateFlightInventory("flight-123", request));
    }
}
