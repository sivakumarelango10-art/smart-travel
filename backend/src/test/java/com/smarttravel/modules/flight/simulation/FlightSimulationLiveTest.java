package com.smarttravel.modules.flight.simulation;

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
import com.smarttravel.modules.flight.simulation.model.FlightSimulationConfig;
import com.smarttravel.modules.flight.simulation.repository.FlightSimulationConfigRepository;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.repository.UserRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "smarttravel.flight.simulation.enabled=false" })
@AutoConfigureMockMvc
class FlightSimulationLiveTest {

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
    private FlightSimulationConfigRepository simulationConfigRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_FLIGHT = "SIM-LIVE-900";
    private String flightId;
    private String adminToken;
    private String userToken;
    private String adminUserId;
    private String standardUserId;

    @BeforeEach
    void setUp() {
        // Clean up previous test artifacts
        flightRepository.findByFlightNumber(TEST_FLIGHT).ifPresent(f -> {
            simulationConfigRepository.findByFlightId(f.getId()).ifPresent(simulationConfigRepository::delete);
            flightStatusHistoryRepository.deleteAll(flightStatusHistoryRepository.findByFlightIdOrderByChangedAtDesc(f.getId()));
            flightRepository.delete(f);
        });

        // Create and persist test users in MongoDB
        User adminUser = User.builder()
                .email("admin_sim@smarttravel.com")
                .normalizedEmail("ADMIN_SIM@SMARTTRAVEL.COM")
                .fullName("Simulation Admin")
                .passwordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .build();
        userRepository.findByNormalizedEmail("ADMIN_SIM@SMARTTRAVEL.COM").ifPresent(userRepository::delete);
        adminUser = userRepository.save(adminUser);
        adminUserId = adminUser.getId();

        User standardUser = User.builder()
                .email("user_sim@smarttravel.com")
                .normalizedEmail("USER_SIM@SMARTTRAVEL.COM")
                .fullName("Simulation User")
                .passwordHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy")
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .build();
        userRepository.findByNormalizedEmail("USER_SIM@SMARTTRAVEL.COM").ifPresent(userRepository::delete);
        standardUser = userRepository.save(standardUser);
        standardUserId = standardUser.getId();

        adminToken = jwtTokenProvider.generateTokenFromUserIdAndEmail(
                adminUserId, "admin_sim@smarttravel.com", List.of("ROLE_ADMIN", "ROLE_USER")
        );

        userToken = jwtTokenProvider.generateTokenFromUserIdAndEmail(
                standardUserId, "user_sim@smarttravel.com", List.of("ROLE_USER")
        );

        // Create test flight
        AirportDto del = AirportDto.builder().code("DEL").name("Indira Gandhi Int Airport").city("New Delhi").build();
        AirportDto bom = AirportDto.builder().code("BOM").name("CSM Int Airport").city("Mumbai").build();
        Instant dep = Instant.now().plus(2, ChronoUnit.DAYS);
        Instant arr = dep.plus(2, ChronoUnit.HOURS);

        FlightCreateRequest req = FlightCreateRequest.builder()
                .flightNumber(TEST_FLIGHT)
                .airline("Simulation Airways")
                .airlineCode("SA")
                .departureAirport(del)
                .arrivalAirport(bom)
                .departureTime(dep)
                .arrivalTime(arr)
                .aircraftModel("Airbus A350-900")
                .basePrice(new BigDecimal("7500.00"))
                .totalSeats(300)
                .availableSeats(300)
                .cabinClasses(Set.of(CabinClass.ECONOMY, CabinClass.BUSINESS))
                .status(FlightStatus.SCHEDULED)
                .build();

        FlightResponse created = flightService.createFlight(req);
        flightId = created.getId();
    }

    @AfterEach
    void tearDown() {
        if (flightId != null) {
            simulationConfigRepository.findByFlightId(flightId).ifPresent(simulationConfigRepository::delete);
            flightStatusHistoryRepository.deleteAll(flightStatusHistoryRepository.findByFlightIdOrderByChangedAtDesc(flightId));
            flightRepository.findById(flightId).ifPresent(flightRepository::delete);
        }
        if (adminUserId != null) {
            userRepository.findById(adminUserId).ifPresent(userRepository::delete);
        }
        if (standardUserId != null) {
            userRepository.findById(standardUserId).ifPresent(userRepository::delete);
        }
    }

    @Test
    @DisplayName("End-to-End Flight Simulation Suite: Security, Start, Step transitions, History persistence, and Terminal Completion")
    void testEndToEndFlightSimulationFlow() throws Exception {
        String simBaseUrl = "/api/v1/admin/flight-simulation/" + flightId;

        // 1. Unauthenticated access returns 401
        mockMvc.perform(post(simBaseUrl + "/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());

        // 2. USER role access returns 403
        mockMvc.perform(post(simBaseUrl + "/start")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        // 3. ADMIN role starts simulation -> 200 OK
        String startPayload = "{\"speedMultiplier\":120,\"delayProbability\":0.0}";
        mockMvc.perform(post(simBaseUrl + "/start")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(startPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightNumber").value(TEST_FLIGHT))
                .andExpect(jsonPath("$.data.enabled").value(true));

        // Verify MongoDB simulation config document
        FlightSimulationConfig config = simulationConfigRepository.findByFlightId(flightId).orElseThrow();
        assertTrue(config.isEnabled());
        assertFalse(config.isCompleted());
        assertEquals(120, config.getSpeedMultiplier());

        // 4. ADMIN steps simulation: SCHEDULED -> BOARDING
        mockMvc.perform(post(simBaseUrl + "/step")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newStatus").value("BOARDING"));

        Flight f1 = flightRepository.findById(flightId).orElseThrow();
        assertEquals(FlightStatus.BOARDING, f1.getStatus());

        // Verify FlightStatusHistory document created in MongoDB
        List<FlightStatusHistory> histories1 = flightStatusHistoryRepository.findByFlightIdOrderByChangedAtDesc(flightId);
        assertFalse(histories1.isEmpty());
        assertEquals(FlightStatus.BOARDING, histories1.get(0).getNewStatus());

        // 5. Step BOARDING -> ON_TIME
        mockMvc.perform(post(simBaseUrl + "/step")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newStatus").value("ON_TIME"));

        // 6. Step ON_TIME -> DEPARTED
        mockMvc.perform(post(simBaseUrl + "/step")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newStatus").value("DEPARTED"));

        // 7. Step DEPARTED -> ARRIVED (Terminal)
        mockMvc.perform(post(simBaseUrl + "/step")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newStatus").value("ARRIVED"));

        // Verify completed state on simulation config
        FlightSimulationConfig completedConfig = simulationConfigRepository.findByFlightId(flightId).orElseThrow();
        assertTrue(completedConfig.isCompleted());
        assertFalse(completedConfig.isEnabled());

        // 8. Subsequent step does nothing (terminal state protection)
        mockMvc.perform(post(simBaseUrl + "/step")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 9. Query simulation status
        mockMvc.perform(get(simBaseUrl)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("ARRIVED"))
                .andExpect(jsonPath("$.data.completed").value(true));
    }
}
