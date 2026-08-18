package com.smarttravel.modules.flight;

import com.smarttravel.SmartTravelApplication;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.flight.dto.AirportDto;
import com.smarttravel.modules.flight.dto.FlightCreateRequest;
import com.smarttravel.modules.flight.dto.FlightResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.model.FlightStatusHistory;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.repository.FlightStatusHistoryRepository;
import com.smarttravel.modules.flight.service.FlightService;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FlightStatusTransitionLiveTest {

    static {
        SmartTravelApplication.loadDotenv();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FlightService flightService;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private FlightStatusHistoryRepository flightStatusHistoryRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private com.smarttravel.modules.user.repository.UserRepository userRepository;

    private static final String TEST_FLIGHT = "LIVE-777";
    private String flightId;
    private String adminToken;
    private String userToken;
    private String adminUserId;
    private String standardUserId;

    @BeforeEach
    void setUp() {
        flightRepository.findByFlightNumber(TEST_FLIGHT).ifPresent(f -> {
            flightStatusHistoryRepository.deleteAll(flightStatusHistoryRepository.findByFlightIdOrderByChangedAtDesc(f.getId()));
            flightRepository.delete(f);
        });

        // 1. Create and persist users in MongoDB
        User adminUser = User.builder()
                .email("admin_live@smarttravel.com")
                .normalizedEmail("ADMIN_LIVE@SMARTTRAVEL.COM")
                .fullName("Live Admin")
                .passwordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                .accountStatus(com.smarttravel.modules.user.model.AccountStatus.ACTIVE)
                .active(true)
                .build();
        userRepository.findByNormalizedEmail("ADMIN_LIVE@SMARTTRAVEL.COM").ifPresent(userRepository::delete);
        adminUser = userRepository.save(adminUser);
        adminUserId = adminUser.getId();

        User standardUser = User.builder()
                .email("user_live@smarttravel.com")
                .normalizedEmail("USER_LIVE@SMARTTRAVEL.COM")
                .fullName("Live User")
                .passwordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(com.smarttravel.modules.user.model.AccountStatus.ACTIVE)
                .active(true)
                .build();
        userRepository.findByNormalizedEmail("USER_LIVE@SMARTTRAVEL.COM").ifPresent(userRepository::delete);
        standardUser = userRepository.save(standardUser);
        standardUserId = standardUser.getId();

        // 2. Generate tokens using real user IDs
        adminToken = jwtTokenProvider.generateTokenFromUserIdAndEmail(
                adminUserId, "admin_live@smarttravel.com", List.of("ROLE_ADMIN", "ROLE_USER")
        );

        userToken = jwtTokenProvider.generateTokenFromUserIdAndEmail(
                standardUserId, "user_live@smarttravel.com", List.of("ROLE_USER")
        );

        // 2. Create sample flight
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").build();
        Instant dep = Instant.now().plus(3, ChronoUnit.DAYS);
        Instant arr = dep.plus(2, ChronoUnit.HOURS);

        FlightCreateRequest req = FlightCreateRequest.builder()
                .flightNumber(TEST_FLIGHT)
                .airline("Live Test Airways")
                .airlineCode("LT")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(dep)
                .arrivalTime(arr)
                .aircraftModel("Boeing 737 MAX")
                .basePrice(new BigDecimal("4500.00"))
                .totalSeats(160)
                .availableSeats(160)
                .cabinClasses(Set.of(CabinClass.ECONOMY))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse created = flightService.createFlight(req);
        flightId = created.getId();
    }

    @AfterEach
    void tearDown() {
        if (flightId != null) {
            flightRepository.findById(flightId).ifPresent(flightRepository::delete);
            flightStatusHistoryRepository.deleteAll(flightStatusHistoryRepository.findByFlightIdOrderByChangedAtDesc(flightId));
        }
        if (adminUserId != null) {
            userRepository.findById(adminUserId).ifPresent(userRepository::delete);
        }
        if (standardUserId != null) {
            userRepository.findById(standardUserId).ifPresent(userRepository::delete);
        }
    }

    @Test
    @DisplayName("End-to-End Status Transition Suite: DELAYED transition, audit history, invalid 409, USER 403, and unauthenticated 401")
    void testLiveStatusTransitions() throws Exception {
        String statusUrl = "/api/v1/admin/flights/" + flightId + "/status";

        // 1. Unauthenticated PATCH -> 401
        mockMvc.perform(patch(statusUrl)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELAYED\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        // 2. USER Role PATCH -> 403
        mockMvc.perform(patch(statusUrl)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DELAYED\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // 3. ADMIN Role PATCH valid DELAYED -> 200 OK
        String delayPayload = "{\"status\":\"DELAYED\",\"delayMinutes\":45,\"delayReason\":\"Weather conditions\"}";
        mockMvc.perform(patch(statusUrl)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(delayPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DELAYED"))
                .andExpect(jsonPath("$.data.delayMinutes").value(45))
                .andExpect(jsonPath("$.data.delayReason").value("Weather conditions"));

        // Verify MongoDB persistence of delay details and history
        Flight flight = flightRepository.findById(flightId).orElseThrow();
        assertEquals(FlightStatus.DELAYED, flight.getStatus());
        assertEquals(45, flight.getDelayMinutes());
        assertEquals("Weather conditions", flight.getDelayReason());

        List<FlightStatusHistory> historyList = flightStatusHistoryRepository.findByFlightIdOrderByChangedAtDesc(flightId);
        assertFalse(historyList.isEmpty());
        FlightStatusHistory hist = historyList.get(0);
        assertEquals(FlightStatus.SCHEDULED, hist.getPreviousStatus());
        assertEquals(FlightStatus.DELAYED, hist.getNewStatus());
        assertEquals(45, hist.getDelayMinutes());
        assertEquals("Weather conditions", hist.getDelayReason());
        assertEquals("admin_live@smarttravel.com", hist.getChangedBy());

        // 4. ADMIN Role PATCH invalid transition DELAYED -> ARRIVED -> 409 Conflict
        mockMvc.perform(patch(statusUrl)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ARRIVED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Invalid flight status transition from DELAYED to ARRIVED"));
    }
}
