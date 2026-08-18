package com.smarttravel.modules.flight.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.dto.FlightStatusUpdateRequest;
import com.smarttravel.modules.flight.dto.FlightUpdateRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminFlightController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminFlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /api/v1/admin/flights creates new flight and returns 201 Created")
    void testCreateFlightSuccess() throws Exception {
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").build();

        FlightCreateRequest req = FlightCreateRequest.builder()
                .flightNumber("AI-101")
                .airline("Air India")
                .airlineCode("AI")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(Instant.parse("2026-08-20T18:30:00Z"))
                .arrivalTime(Instant.parse("2026-08-20T20:30:00Z"))
                .aircraftModel("Airbus A321neo")
                .basePrice(new BigDecimal("5000.00"))
                .totalSeats(180)
                .build();

        when(flightService.createFlight(any(FlightCreateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/admin/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("66c1e101f1a2b3c4d5e6f702"))
                .andExpect(jsonPath("$.data.flightNumber").value("AI-101"))
                .andExpect(jsonPath("$.data.durationMinutes").value(120));
    }

    @Test
    @DisplayName("POST /api/v1/admin/flights rejects invalid input with 400 Bad Request")
    void testCreateFlightValidationFailure() throws Exception {
        FlightCreateRequest req = FlightCreateRequest.builder()
                .flightNumber("") // Blank flight number
                .build();

        mockMvc.perform(post("/api/v1/admin/flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/flights/{id} updates flight details and returns 200 OK")
    void testUpdateFlightSuccess() throws Exception {
        FlightUpdateRequest req = FlightUpdateRequest.builder()
                .basePrice(new BigDecimal("5500.00"))
                .build();

        sampleResponse.setBasePrice(new BigDecimal("5500.00"));
        when(flightService.updateFlight(eq("66c1e101f1a2b3c4d5e6f702"), any(FlightUpdateRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/admin/flights/66c1e101f1a2b3c4d5e6f702")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.basePrice").value(5500.00));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/flights/{id} deletes flight and returns 200 OK")
    void testDeleteFlightSuccess() throws Exception {
        doNothing().when(flightService).deleteFlight("66c1e101f1a2b3c4d5e6f702");

        mockMvc.perform(delete("/api/v1/admin/flights/66c1e101f1a2b3c4d5e6f702")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Flight deleted successfully"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/flights/{id}/status updates flight status and returns 200 OK")
    void testUpdateFlightStatusSuccess() throws Exception {
        FlightStatusUpdateRequest req = new FlightStatusUpdateRequest(FlightStatus.DELAYED);
        sampleResponse.setStatus(FlightStatus.DELAYED);

        when(flightService.updateFlightStatus(eq("66c1e101f1a2b3c4d5e6f702"), any(FlightStatusUpdateRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(patch("/api/v1/admin/flights/66c1e101f1a2b3c4d5e6f702/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DELAYED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/flights/{id}/status returns 409 Conflict on invalid state transition")
    void testUpdateFlightStatusConflict() throws Exception {
        FlightStatusUpdateRequest req = new FlightStatusUpdateRequest(FlightStatus.SCHEDULED);

        when(flightService.updateFlightStatus(eq("66c1e101f1a2b3c4d5e6f702"), any(FlightStatusUpdateRequest.class)))
                .thenThrow(new com.smarttravel.common.exception.InvalidStateTransitionException("Invalid flight status transition from ARRIVED to SCHEDULED"));

        mockMvc.perform(patch("/api/v1/admin/flights/66c1e101f1a2b3c4d5e6f702/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Invalid flight status transition from ARRIVED to SCHEDULED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/flights/{id}/status returns 400 Bad Request on invalid delay payload")
    void testUpdateFlightStatusBadRequest() throws Exception {
        FlightStatusUpdateRequest req = FlightStatusUpdateRequest.builder()
                .status(FlightStatus.DELAYED)
                .delayMinutes(-5)
                .build();

        mockMvc.perform(patch("/api/v1/admin/flights/66c1e101f1a2b3c4d5e6f702/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/flights/{id}/inventory updates cabin inventories and returns 200 OK")
    void testUpdateFlightInventorySuccess() throws Exception {
        com.smarttravel.modules.flight.dto.CabinInventoryDto economy = com.smarttravel.modules.flight.dto.CabinInventoryDto.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(180)
                .availableSeats(150)
                .basePrice(new BigDecimal("5200.00"))
                .taxAmount(new BigDecimal("624.00"))
                .feeAmount(new BigDecimal("150.00"))
                .totalPrice(new BigDecimal("5974.00"))
                .build();

        com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest req =
                new com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest(List.of(economy));

        sampleResponse.setCabinInventories(List.of(economy));
        when(flightService.updateFlightInventory(eq("66c1e101f1a2b3c4d5e6f702"), any(com.smarttravel.modules.flight.dto.FlightInventoryUpdateRequest.class)))
                .thenReturn(sampleResponse);

        mockMvc.perform(put("/api/v1/admin/flights/66c1e101f1a2b3c4d5e6f702/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cabinInventories[0].cabinClass").value("ECONOMY"))
                .andExpect(jsonPath("$.data.cabinInventories[0].totalSeats").value(180));
    }
}
