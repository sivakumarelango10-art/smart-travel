package com.smarttravel.modules.flight.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.DuplicateResourceException;
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
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @InjectMocks
    private FlightServiceImpl flightService;

    private Flight sampleFlight;
    private Instant now;
    private Instant departureTime;
    private Instant arrivalTime;

    @BeforeEach
    void setUp() {
        now = Instant.now();
        departureTime = now.plus(1, ChronoUnit.DAYS);
        arrivalTime = departureTime.plus(2, ChronoUnit.HOURS);

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
                .durationMinutes(120)
                .aircraftModel("Airbus A321neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(180)
                .availableSeats(180)
                .cabinClasses(Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS))
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Create flight calculates durationMinutes server-side and saves entity")
    void testCreateFlightSuccess() {
        AirportDto delDto = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").build();
        AirportDto bomDto = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").build();

        FlightCreateRequest req = FlightCreateRequest.builder()
                .flightNumber("AI-101")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(delDto)
                .arrivalAirport(bomDto)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .aircraftModel("Airbus A321neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(180)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .build();

        when(flightRepository.existsByFlightNumber("AI-101")).thenReturn(false);
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> {
            Flight f = inv.getArgument(0);
            f.setId("generated-id-123");
            return f;
        });

        FlightResponse res = flightService.createFlight(req);
        assertNotNull(res);
        assertEquals("generated-id-123", res.getId());
        assertEquals("AI-101", res.getFlightNumber());
        assertEquals(120, res.getDurationMinutes());
        assertEquals(FlightStatus.SCHEDULED, res.getStatus());
        assertTrue(res.isActive());
    }

    @Test
    @DisplayName("Create flight rejects duplicate flight number with DuplicateResourceException")
    void testCreateFlightDuplicateNumber() {
        FlightCreateRequest req = FlightCreateRequest.builder()
                .flightNumber("AI-101")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(AirportDto.builder().code("DEL").name("DEL").city("Delhi").build())
                .arrivalAirport(AirportDto.builder().code("BOM").name("BOM").city("Mumbai").build())
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .aircraftModel("A321")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(180)
                .build();

        when(flightRepository.existsByFlightNumber("AI-101")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> flightService.createFlight(req));
    }

    @Test
    @DisplayName("Create flight rejects invalid times (arrival before departure)")
    void testCreateFlightInvalidTimes() {
        FlightCreateRequest req = FlightCreateRequest.builder()
                .flightNumber("AI-102")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(AirportDto.builder().code("DEL").name("DEL").city("Delhi").build())
                .arrivalAirport(AirportDto.builder().code("BOM").name("BOM").city("Mumbai").build())
                .departureTime(departureTime)
                .arrivalTime(departureTime.minus(1, ChronoUnit.HOURS))
                .aircraftModel("A321")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(180)
                .build();

        when(flightRepository.existsByFlightNumber("AI-102")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> flightService.createFlight(req));
    }

    @Test
    @DisplayName("Update flight modifies entity and recalculates duration")
    void testUpdateFlightSuccess() {
        Instant newArrival = arrivalTime.plus(30, ChronoUnit.MINUTES);
        FlightUpdateRequest updateReq = FlightUpdateRequest.builder()
                .basePrice(new BigDecimal("5500.00"))
                .arrivalTime(newArrival)
                .build();

        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        FlightResponse res = flightService.updateFlight("flight-123", updateReq);
        assertEquals(new BigDecimal("5500.00"), res.getBasePrice());
        assertEquals(150, res.getDurationMinutes());
    }

    @Test
    @DisplayName("Delete flight performs soft-delete by setting active=false")
    void testDeleteFlightSuccess() {
        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        flightService.deleteFlight("flight-123");
        assertFalse(sampleFlight.isActive());
        verify(flightRepository).save(sampleFlight);
    }

    @Test
    @DisplayName("Update flight status changes status cleanly")
    void testUpdateFlightStatusSuccess() {
        FlightStatusUpdateRequest req = new FlightStatusUpdateRequest(FlightStatus.DELAYED);

        when(flightRepository.findById("flight-123")).thenReturn(Optional.of(sampleFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        FlightResponse res = flightService.updateFlightStatus("flight-123", req);
        assertEquals(FlightStatus.DELAYED, res.getStatus());
    }

    @Test
    @DisplayName("Get flight by ID returns flight when active")
    void testGetFlightByIdSuccess() {
        when(flightRepository.findByIdAndActiveTrue("flight-123")).thenReturn(Optional.of(sampleFlight));

        FlightResponse res = flightService.getFlightById("flight-123");
        assertNotNull(res);
        assertEquals("AI-101", res.getFlightNumber());
    }

    @Test
    @DisplayName("Get flight by ID throws ResourceNotFoundException when not found")
    void testGetFlightByIdNotFound() {
        when(flightRepository.findByIdAndActiveTrue("non-existent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> flightService.getFlightById("non-existent"));
    }

    @Test
    @DisplayName("Get flight by flight number returns matching flight")
    void testGetFlightByNumberSuccess() {
        when(flightRepository.findByFlightNumberAndActiveTrue("AI-101")).thenReturn(Optional.of(sampleFlight));

        FlightResponse res = flightService.getFlightByFlightNumber("ai-101");
        assertNotNull(res);
        assertEquals("AI-101", res.getFlightNumber());
    }

    @Test
    @DisplayName("Search flights returns paginated response")
    void testSearchFlightsSuccess() {
        FlightSearchCriteria criteria = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("BOM")
                .page(0)
                .size(10)
                .build();

        Page<Flight> page = new PageImpl<>(List.of(sampleFlight));
        when(flightRepository.searchFlights(eq(criteria))).thenReturn(page);

        PageResponse<FlightResponse> res = flightService.searchFlights(criteria);
        assertNotNull(res);
        assertEquals(1, res.getContent().size());
        assertEquals("AI-101", res.getContent().get(0).getFlightNumber());
        assertEquals(1, res.getTotalElements());
    }
}
