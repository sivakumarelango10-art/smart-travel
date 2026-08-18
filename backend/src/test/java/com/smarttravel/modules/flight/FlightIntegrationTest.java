package com.smarttravel.modules.flight;

import com.smarttravel.SmartTravelApplication;
import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.model.FlightStatusHistory;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.FlightStatusHistoryRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Autowired
    private FlightStatusHistoryRepository flightStatusHistoryRepository;

    private static final String TEST_FLIGHT_NUM = "IT-999";

    @BeforeEach
    @AfterEach
    void cleanup() {
        flightRepository.findByFlightNumber(TEST_FLIGHT_NUM).ifPresent(f -> {
            flightStatusHistoryRepository.deleteAll(flightStatusHistoryRepository.findByFlightIdOrderByChangedAtDesc(f.getId()));
            flightRepository.delete(f);
        });
    }

    @Test
    @DisplayName("End-to-End Flight Flow: Create, persistence, multi-criteria search, state machine transitions, audit history, and soft delete")
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
        assertEquals(departure, persisted.getDepartureTime()); // Original schedule intact
        assertEquals(arrival, persisted.getArrivalTime());     // Original schedule intact
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

        // 4. Update Status to DELAYED with delay details
        FlightStatusUpdateRequest delayReq = FlightStatusUpdateRequest.builder()
                .status(FlightStatus.DELAYED)
                .delayMinutes(40)
                .delayReason("Technical inspection at gate")
                .build();

        FlightResponse statusUpdated = flightService.updateFlightStatus(created.getId(), delayReq);
        assertEquals(FlightStatus.DELAYED, statusUpdated.getStatus());
        assertEquals(40, statusUpdated.getDelayMinutes());
        assertEquals("Technical inspection at gate", statusUpdated.getDelayReason());
        assertEquals(departure.plus(40, ChronoUnit.MINUTES), statusUpdated.getRevisedDepartureTime());
        assertEquals(arrival.plus(40, ChronoUnit.MINUTES), statusUpdated.getEstimatedArrival());

        // Verify original schedule remains intact on persisted flight document
        Flight flightAfterDelay = flightRepository.findById(created.getId()).orElseThrow();
        assertEquals(departure, flightAfterDelay.getDepartureTime());
        assertEquals(arrival, flightAfterDelay.getArrivalTime());

        // 5. Verify Status History persistence in MongoDB
        List<FlightStatusHistory> histories = flightStatusHistoryRepository.findByFlightIdOrderByChangedAtDesc(created.getId());
        assertFalse(histories.isEmpty());
        FlightStatusHistory latestHistory = histories.get(0);
        assertEquals(FlightStatus.SCHEDULED, latestHistory.getPreviousStatus());
        assertEquals(FlightStatus.DELAYED, latestHistory.getNewStatus());
        assertEquals(40, latestHistory.getDelayMinutes());
        assertEquals("Technical inspection at gate", latestHistory.getDelayReason());
        assertNotNull(latestHistory.getChangedBy());

        // 6. Transition to BOARDING (from DELAYED) -> valid
        FlightStatusUpdateRequest boardingReq = new FlightStatusUpdateRequest(FlightStatus.BOARDING);
        FlightResponse boardingResp = flightService.updateFlightStatus(created.getId(), boardingReq);
        assertEquals(FlightStatus.BOARDING, boardingResp.getStatus());

        // 7. Transition to ARRIVED directly from BOARDING -> Invalid transition!
        FlightStatusUpdateRequest illegalReq = new FlightStatusUpdateRequest(FlightStatus.ARRIVED);
        assertThrows(InvalidStateTransitionException.class, () -> flightService.updateFlightStatus(created.getId(), illegalReq));

        // 8. Delete (soft delete)
        flightService.deleteFlight(created.getId());
        Optional<Flight> afterDeleteOpt = flightRepository.findById(created.getId());
        assertTrue(afterDeleteOpt.isPresent());
        assertFalse(afterDeleteOpt.get().isActive());
    }
}
