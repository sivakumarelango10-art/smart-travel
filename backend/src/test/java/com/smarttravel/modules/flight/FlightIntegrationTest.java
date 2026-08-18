package com.smarttravel.modules.flight;

import com.smarttravel.SmartTravelApplication;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
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
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FlightIntegrationTest {

    static {
        SmartTravelApplication.loadDotenv();
    }

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    private static final String TEST_FLIGHT_NUM = "IT-999";

    @BeforeEach
    @AfterEach
    void cleanup() {
        flightRepository.findByFlightNumber(TEST_FLIGHT_NUM).ifPresent(flightRepository::delete);
    }

    @Test
    @DisplayName("End-to-End Flight Flow: Create, MongoDB persistence verification, multi-criteria search, status update, and soft delete")
    void testEndToEndFlightFlow() {
        Instant departure = LocalDate.now(ZoneOffset.UTC).plusDays(3).atTime(10, 0).toInstant(ZoneOffset.UTC);
        Instant arrival = departure.plus(2, ChronoUnit.HOURS).plus(15, ChronoUnit.MINUTES);

        AirportDto del = AirportDto.builder()
                .code("DEL")
                .name("Indira Gandhi International Airport")
                .city("New Delhi")
                .country("India")
                .terminal("T3")
                .gate("12A")
                .build();

        AirportDto bom = AirportDto.builder()
                .code("BOM")
                .name("Chhatrapati Shivaji Maharaj International Airport")
                .city("Mumbai")
                .country("India")
                .terminal("T2")
                .gate("4B")
                .build();

        FlightCreateRequest createReq = FlightCreateRequest.builder()
                .flightNumber(TEST_FLIGHT_NUM)
                .airline("Integration Airways")
                .airlineCode("IT")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(departure)
                .arrivalTime(arrival)
                .aircraftModel("Boeing 787-9")
                .basePrice(new BigDecimal("6200.00"))
                .totalSeats(250)
                .availableSeats(250)
                .cabinClasses(Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS))
                .status(FlightStatus.SCHEDULED)
                .build();

        // 1. Create Flight
        FlightResponse created = flightService.createFlight(createReq);
        assertNotNull(created.getId());
        assertEquals(TEST_FLIGHT_NUM, created.getFlightNumber());
        assertEquals(135, created.getDurationMinutes()); // 2h 15m = 135 min

        // 2. Verify MongoDB Persistence
        Optional<Flight> persistedOpt = flightRepository.findById(created.getId());
        assertTrue(persistedOpt.isPresent());
        Flight persisted = persistedOpt.get();
        assertEquals("Integration Airways", persisted.getAirline());
        assertEquals("DEL", persisted.getDepartureAirport().getCode());
        assertEquals("BOM", persisted.getArrivalAirport().getCode());
        assertEquals(135, persisted.getDurationMinutes());
        assertTrue(persisted.isActive());

        // 3. Multi-Criteria MongoDB Search
        FlightSearchCriteria criteria = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("BOM")
                .departureDate(LocalDate.now(ZoneOffset.UTC).plusDays(3))
                .cabinClass(CabinClass.BUSINESS)
                .build();

        PageResponse<FlightResponse> searchResults = flightService.searchFlights(criteria);
        assertNotNull(searchResults);
        assertTrue(searchResults.getContent().stream().anyMatch(f -> f.getFlightNumber().equals(TEST_FLIGHT_NUM)));

        // 4. Update Status to DELAYED
        FlightStatusUpdateRequest statusReq = new FlightStatusUpdateRequest(FlightStatus.DELAYED);
        FlightResponse statusUpdated = flightService.updateFlightStatus(created.getId(), statusReq);
        assertEquals(FlightStatus.DELAYED, statusUpdated.getStatus());

        // 5. Delete (soft delete)
        flightService.deleteFlight(created.getId());
        Optional<Flight> afterDeleteOpt = flightRepository.findById(created.getId());
        assertTrue(afterDeleteOpt.isPresent());
        assertFalse(afterDeleteOpt.get().isActive());
    }
}
