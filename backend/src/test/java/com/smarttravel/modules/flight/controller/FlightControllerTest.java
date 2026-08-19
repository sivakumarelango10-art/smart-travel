package com.smarttravel.modules.flight.controller;

import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightSearchCriteria;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.service.FlightService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlightController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class FlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightService flightService;

    @MockBean
    private com.smarttravel.common.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.smarttravel.common.security.CustomUserDetailsService customUserDetailsService;

    private FlightResponse sampleResponse;

    @BeforeEach
    void setUp() {
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").build();

        sampleResponse = FlightResponse.builder()
                .id("66c1e101f1a2b3c4d5e6f702")
                .flightNumber("AI-101")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.parse("2026-08-20T18:30:00Z"))
                .arrivalTime(Instant.parse("2026-08-20T20:30:00Z"))
                .durationMinutes(120)
                .aircraftModel("Airbus A321neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(180)
                .availableSeats(180)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .status(FlightStatus.SCHEDULED)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/flights returns paginated search results")
    void testSearchFlights() throws Exception {
        PageResponse<FlightResponse> pageResponse = PageResponse.from(new org.springframework.data.domain.PageImpl<>(List.of(sampleResponse)));
        when(flightService.searchFlights(any(FlightSearchCriteria.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/flights")
                        .param("origin", "DEL")
                        .param("destination", "BOM")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].flightNumber").value("AI-101"))
                .andExpect(jsonPath("$.data.content[0].durationMinutes").value(120))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /v1/flights/search invokes searchFlights and DOES NOT route to getFlightById('search')")
    void testSearchFlightsExplicitSearchPath() throws Exception {
        PageResponse<FlightResponse> pageResponse = PageResponse.from(new org.springframework.data.domain.PageImpl<>(List.of(sampleResponse)));
        when(flightService.searchFlights(any(FlightSearchCriteria.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/v1/flights/search")
                        .param("origin", "BOM")
                        .param("destination", "DXB")
                        .param("departureDate", "2026-08-20")
                        .param("cabinClass", "ECONOMY")
                        .param("passengers", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flights retrieved successfully"))
                .andExpect(jsonPath("$.data.content[0].flightNumber").value("AI-101"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // CRITICAL REGRESSION ASSERTION: getFlightById("search") must NEVER be called!
        org.mockito.Mockito.verify(flightService, org.mockito.Mockito.never()).getFlightById("search");
        org.mockito.Mockito.verify(flightService, org.mockito.Mockito.times(1)).searchFlights(any(FlightSearchCriteria.class));
    }

    @Test
    @DisplayName("GET /v1/flights/search returns HTTP 200 with empty list when no flights found")
    void testSearchFlightsEmptyResults() throws Exception {
        PageResponse<FlightResponse> emptyPage = PageResponse.from(new org.springframework.data.domain.PageImpl<>(List.of()));
        when(flightService.searchFlights(any(FlightSearchCriteria.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/v1/flights/search")
                        .param("origin", "BOM")
                        .param("destination", "DXB")
                        .param("departureDate", "2026-08-20")
                        .param("cabinClass", "ECONOMY")
                        .param("passengers", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/flights/{id} returns flight details")
    void testGetFlightById() throws Exception {
        when(flightService.getFlightById("66c1e101f1a2b3c4d5e6f702")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/flights/66c1e101f1a2b3c4d5e6f702")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("66c1e101f1a2b3c4d5e6f702"))
                .andExpect(jsonPath("$.data.flightNumber").value("AI-101"))
                .andExpect(jsonPath("$.data.departureAirport.code").value("DEL"));
    }

    @Test
    @DisplayName("GET /v1/flights/{id} returns flight details")
    void testGetFlightByIdV1Path() throws Exception {
        when(flightService.getFlightById("66c1e101f1a2b3c4d5e6f702")).thenReturn(sampleResponse);

        mockMvc.perform(get("/v1/flights/66c1e101f1a2b3c4d5e6f702")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("66c1e101f1a2b3c4d5e6f702"));
    }

    @Test
    @DisplayName("GET /api/v1/flights/{id} returns 404 when not found")
    void testGetFlightByIdNotFound() throws Exception {
        when(flightService.getFlightById("non-existent")).thenThrow(new ResourceNotFoundException("Flight", "id", "non-existent"));

        mockMvc.perform(get("/api/v1/flights/non-existent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/flights/number/{flightNumber} returns flight details")
    void testGetFlightByFlightNumber() throws Exception {
        when(flightService.getFlightByFlightNumber("AI-101")).thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/flights/number/AI-101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightNumber").value("AI-101"));
    }

    @Test
    @DisplayName("GET /api/v1/flights/number/{flightNumber} returns 404 when flight not found")
    void testGetFlightByFlightNumberNotFound() throws Exception {
        when(flightService.getFlightByFlightNumber("UNKNOWN")).thenThrow(new ResourceNotFoundException("Flight", "flightNumber", "UNKNOWN"));

        mockMvc.perform(get("/api/v1/flights/number/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/flights returns 400 Bad Request on invalid search parameters")
    void testSearchFlightsValidationFailure() throws Exception {
        when(flightService.searchFlights(any(FlightSearchCriteria.class)))
                .thenThrow(new com.smarttravel.common.exception.BadRequestException("Origin and destination airport/city must not be identical"));

        mockMvc.perform(get("/api/v1/flights")
                        .param("origin", "DEL")
                        .param("destination", "DEL")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Origin and destination airport/city must not be identical"));
    }
}
