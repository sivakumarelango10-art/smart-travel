package com.smarttravel.modules.flight;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.CabinInventoryDto;
import com.smarttravel.modules.flight.dto.DepartureTimeWindow;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.model.AirportInfo;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FlightSearchIntegrationTest {

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    private String flightId1;
    private String flightId2;
    private String flightIdCancelled;
    private String flightIdLegacy;
    private LocalDate searchDate;

    @BeforeEach
    void setUp() {
        searchDate = LocalDate.now().plusDays(5);
        Instant depTimeMorning = searchDate.atTime(8, 0).atZone(java.time.ZoneOffset.UTC).toInstant();
        Instant arrTimeMorning = depTimeMorning.plus(2, ChronoUnit.HOURS);

        Instant depTimeEvening = searchDate.atTime(19, 0).atZone(java.time.ZoneOffset.UTC).toInstant();
        Instant arrTimeEvening = depTimeEvening.plus(2, ChronoUnit.HOURS);

        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").country("India").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").country("India").build();

        // 1. Flight 1: Morning flight, Economy (10 seats available) & Business (2 seats available)
        CabinInventoryDto econ1 = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(180)
                .availableSeats(10)
                .basePrice(new BigDecimal("5000.00"))
                .taxAmount(new BigDecimal("600.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5750.00"))
                .build();
        CabinInventoryDto bus1 = CabinInventoryDto.builder()
                .cabinClass(CabinClass.BUSINESS)
                .totalSeats(24)
                .availableSeats(2)
                .basePrice(new BigDecimal("18000.00"))
                .taxAmount(new BigDecimal("2160.00"))
                .feeAmount(new BigDecimal("300.00"))
                .totalPrice(new BigDecimal("20460.00"))
                .build();

        FlightCreateRequest req1 = FlightCreateRequest.builder()
                .flightNumber("TEST-SEARCH-101")
                .airline("SmartAir")
                .airlineCode("SA")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(depTimeMorning)
                .arrivalTime(arrTimeMorning)
                .aircraftModel("A321")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(204)
                .availableSeats(12)
                .cabinClasses(Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS))
                .cabinInventories(List.of(econ1, bus1))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse res1 = flightService.createFlight(req1);
        flightId1 = res1.getId();

        // 2. Flight 2: Evening flight, Economy only (50 seats available)
        CabinInventoryDto econ2 = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(180)
                .availableSeats(50)
                .basePrice(new BigDecimal("4500.00"))
                .taxAmount(new BigDecimal("540.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5190.00"))
                .build();

        FlightCreateRequest req2 = FlightCreateRequest.builder()
                .flightNumber("TEST-SEARCH-102")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(depTimeEvening)
                .arrivalTime(arrTimeEvening)
                .aircraftModel("B737")
                .basePrice(new BigDecimal("4500.00"))
                .totalSeats(180)
                .availableSeats(50)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(List.of(econ2))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse res2 = flightService.createFlight(req2);
        flightId2 = res2.getId();

        // 3. Flight 3: Cancelled flight (must be excluded from search)
        FlightCreateRequest req3 = FlightCreateRequest.builder()
                .flightNumber("TEST-SEARCH-999")
                .airline("SmartAir")
                .airlineCode("SA")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(depTimeMorning)
                .arrivalTime(arrTimeMorning)
                .aircraftModel("A320")
                .basePrice(new BigDecimal("4000.00"))
                .totalSeats(180)
                .availableSeats(180)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .status(FlightStatus.CANCELLED)
                .build();

        FlightResponse res3 = flightService.createFlight(req3);
        flightIdCancelled = res3.getId();

        // 4. Legacy Flight without cabinInventories in MongoDB
        Flight legacyFlight = Flight.builder()
                .flightNumber("TEST-LEGACY-001")
                .airline("Legacy Airways")
                .airlineCode("LA")
                .departureAirport(AirportInfo.builder().code("DEL").name("Delhi").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").name("Mumbai").city("Mumbai").build())
                .departureTime(depTimeMorning.plus(1, ChronoUnit.HOURS))
                .arrivalTime(arrTimeMorning.plus(1, ChronoUnit.HOURS))
                .durationMinutes(120)
                .aircraftModel("A320")
                .basePrice(new BigDecimal("4800.00"))
                .totalSeats(150)
                .availableSeats(150)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .cabinInventories(null)
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .build();
        Flight savedLegacy = flightRepository.save(legacyFlight);
        flightIdLegacy = savedLegacy.getId();
    }

    @AfterEach
    void tearDown() {
        if (flightId1 != null) flightRepository.deleteById(flightId1);
        if (flightId2 != null) flightRepository.deleteById(flightId2);
        if (flightIdCancelled != null) flightRepository.deleteById(flightIdCancelled);
        if (flightIdLegacy != null) flightRepository.deleteById(flightIdLegacy);
    }

    @Test
    @DisplayName("Search by route and date should return active non-cancelled flights and exclude CANCELLED")
    void testSearchByRouteAndDate() {
        FlightSearchCriteria criteria = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("BOM")
                .departureDate(searchDate)
                .build();

        PageResponse<FlightResponse> results = flightService.searchFlights(criteria);

        assertThat(results.getContent()).isNotEmpty();
        List<String> flightNumbers = results.getContent().stream().map(FlightResponse::getFlightNumber).toList();
        assertThat(flightNumbers).contains("TEST-SEARCH-101", "TEST-SEARCH-102", "TEST-LEGACY-001");
        assertThat(flightNumbers).doesNotContain("TEST-SEARCH-999");
    }

    @Test
    @DisplayName("Search with passenger count should filter out cabins with insufficient available seats")
    void testSearchWithPassengerCount_SufficientVsInsufficient() {
        // Search for 4 passengers in BUSINESS class -> flight 1 only has 2 seats available, should be excluded
        FlightSearchCriteria criteriaBus = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("BOM")
                .departureDate(searchDate)
                .cabinClass(CabinClass.BUSINESS)
                .passengers(4)
                .build();

        PageResponse<FlightResponse> busResults = flightService.searchFlights(criteriaBus);
        List<String> busFlightNumbers = busResults.getContent().stream().map(FlightResponse::getFlightNumber).toList();
        assertThat(busFlightNumbers).doesNotContain("TEST-SEARCH-101");

        // Search for 2 passengers in BUSINESS class -> flight 1 has 2 seats available, should be included
        FlightSearchCriteria criteriaBus2 = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("BOM")
                .departureDate(searchDate)
                .cabinClass(CabinClass.BUSINESS)
                .passengers(2)
                .build();

        PageResponse<FlightResponse> busResults2 = flightService.searchFlights(criteriaBus2);
        List<String> busFlightNumbers2 = busResults2.getContent().stream().map(FlightResponse::getFlightNumber).toList();
        assertThat(busFlightNumbers2).contains("TEST-SEARCH-101");

        // Verify selectedCabin multi-passenger fare calculation
        FlightResponse matched = busResults2.getContent().stream()
                .filter(f -> f.getFlightNumber().equals("TEST-SEARCH-101"))
                .findFirst().orElseThrow();
        assertThat(matched.getSelectedCabin()).isNotNull();
        assertThat(matched.getSelectedCabin().getCabinClass()).isEqualTo(CabinClass.BUSINESS);
        assertThat(matched.getSelectedCabin().getSinglePassengerFare().getTotalAmount()).isEqualByComparingTo("20460.00");
        assertThat(matched.getSelectedCabin().getTotalFare().getTotalAmount()).isEqualByComparingTo("40920.00");
        assertThat(matched.getSelectedCabin().getTotalFare().getPassengerCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Search with departure time window should filter morning vs evening flights")
    void testSearchDepartureTimeWindow() {
        FlightSearchCriteria criteriaMorning = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("BOM")
                .departureDate(searchDate)
                .departureTimeWindow(DepartureTimeWindow.MORNING)
                .build();

        PageResponse<FlightResponse> morningResults = flightService.searchFlights(criteriaMorning);
        List<String> morningNumbers = morningResults.getContent().stream().map(FlightResponse::getFlightNumber).toList();
        assertThat(morningNumbers).contains("TEST-SEARCH-101");
        assertThat(morningNumbers).doesNotContain("TEST-SEARCH-102");

        FlightSearchCriteria criteriaEvening = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("BOM")
                .departureDate(searchDate)
                .departureTimeWindow(DepartureTimeWindow.EVENING)
                .build();

        PageResponse<FlightResponse> eveningResults = flightService.searchFlights(criteriaEvening);
        List<String> eveningNumbers = eveningResults.getContent().stream().map(FlightResponse::getFlightNumber).toList();
        assertThat(eveningNumbers).contains("TEST-SEARCH-102");
        assertThat(eveningNumbers).doesNotContain("TEST-SEARCH-101");
    }

    @Test
    @DisplayName("Get flight by ID returns complete details, status, and cabin inventories")
    void testGetFlightById() {
        FlightResponse response = flightService.getFlightById(flightId1);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(flightId1);
        assertThat(response.getFlightNumber()).isEqualTo("TEST-SEARCH-101");
        assertThat(response.getCabinInventories()).hasSize(2);
        assertThat(response.getStatus()).isEqualTo(FlightStatus.SCHEDULED);
    }

    @Test
    @DisplayName("Get flight by flight number normalizes case and whitespace")
    void testGetFlightByFlightNumber_Normalized() {
        FlightResponse response = flightService.getFlightByFlightNumber("  test-search-101  ");

        assertThat(response).isNotNull();
        assertThat(response.getFlightNumber()).isEqualTo("TEST-SEARCH-101");
    }

    @Test
    @DisplayName("Get flight by flight number throws ResourceNotFoundException for unknown number")
    void testGetFlightByFlightNumber_NotFound() {
        assertThrows(ResourceNotFoundException.class, () -> flightService.getFlightByFlightNumber("UNKNOWN-999"));
    }

    @Test
    @DisplayName("Search validation rejects identical origin and destination with BadRequestException")
    void testSearchValidation_IdenticalRoute() {
        FlightSearchCriteria criteria = FlightSearchCriteria.builder()
                .origin("DEL")
                .destination("DEL")
                .build();

        assertThrows(BadRequestException.class, () -> flightService.searchFlights(criteria));
    }

    @Test
    @DisplayName("Search validation rejects invalid passenger count with BadRequestException")
    void testSearchValidation_InvalidPassengers() {
        FlightSearchCriteria criteriaZero = FlightSearchCriteria.builder()
                .passengers(0)
                .build();
        assertThrows(BadRequestException.class, () -> flightService.searchFlights(criteriaZero));

        FlightSearchCriteria criteriaExcess = FlightSearchCriteria.builder()
                .passengers(12)
                .build();
        assertThrows(BadRequestException.class, () -> flightService.searchFlights(criteriaExcess));
    }

    @Test
    @DisplayName("Search validation rejects past departure date with BadRequestException")
    void testSearchValidation_PastDepartureDate() {
        FlightSearchCriteria criteriaPast = FlightSearchCriteria.builder()
                .departureDate(LocalDate.now().minusDays(2))
                .build();

        assertThrows(BadRequestException.class, () -> flightService.searchFlights(criteriaPast));
    }

    @Test
    @DisplayName("Search validation rejects negative or inverted price range with BadRequestException")
    void testSearchValidation_InvalidPrices() {
        FlightSearchCriteria criteriaNegative = FlightSearchCriteria.builder()
                .minPrice(new BigDecimal("-100.00"))
                .build();
        assertThrows(BadRequestException.class, () -> flightService.searchFlights(criteriaNegative));

        FlightSearchCriteria criteriaInverted = FlightSearchCriteria.builder()
                .minPrice(new BigDecimal("10000.00"))
                .maxPrice(new BigDecimal("5000.00"))
                .build();
        assertThrows(BadRequestException.class, () -> flightService.searchFlights(criteriaInverted));
    }

    @Test
    @DisplayName("Legacy flight without cabinInventories returns valid default ECONOMY inventory")
    void testLegacyFlightCompatibility() {
        FlightResponse response = flightService.getFlightById(flightIdLegacy);

        assertThat(response).isNotNull();
        assertThat(response.getFlightNumber()).isEqualTo("TEST-LEGACY-001");
        assertThat(response.getCabinInventories()).isNotEmpty();
        assertThat(response.getCabinInventories().get(0).getCabinClass()).isEqualTo(CabinClass.ECONOMY);
        assertThat(response.getCabinInventories().get(0).getBasePrice()).isEqualByComparingTo("4800.00");
    }

    @Test
    @DisplayName("Admin updates cabin inventories and verifies updated capacity and pricing")
    void testAdminInventoryUpdate() {
        CabinInventoryDto updatedEcon = CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(150)
                .availableSeats(80)
                .basePrice(new BigDecimal("5500.00"))
                .taxAmount(new BigDecimal("660.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("6310.00"))
                .build();

        FlightInventoryUpdateRequest updateReq = new FlightInventoryUpdateRequest(List.of(updatedEcon));
        FlightResponse updatedResponse = flightService.updateFlightInventory(flightId1, updateReq);

        assertThat(updatedResponse.getTotalSeats()).isEqualTo(150);
        assertThat(updatedResponse.getAvailableSeats()).isEqualTo(80);
        assertThat(updatedResponse.getCabinInventories()).hasSize(1);
        assertThat(updatedResponse.getCabinInventories().get(0).getBasePrice()).isEqualByComparingTo("5500.00");
    }
}
