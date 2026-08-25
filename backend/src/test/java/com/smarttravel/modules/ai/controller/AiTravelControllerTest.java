package com.smarttravel.modules.ai.controller;
 
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.ai.dto.FlightDelayExplanationResponse;
import com.smarttravel.modules.ai.dto.TravelInsightResponse;
import com.smarttravel.modules.ai.service.GeminiTravelInsightService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AiTravelController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
class AiTravelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GeminiTravelInsightService geminiTravelInsightService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /v1/ai/insights returns 200 OK with travel insights")
    void testGetTravelInsights() throws Exception {
        TravelInsightResponse mockResponse = new TravelInsightResponse(
                "Mumbai",
                "Financial capital of India",
                "November to February",
                List.of("Gateway of India", "Marine Drive"),
                List.of("Use local trains", "Try coastal cuisine"),
                "Pleasant winter weather",
                "gemini-1.5-flash",
                false,
                false
        );

        when(geminiTravelInsightService.generateTravelInsights(anyString(), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/v1/ai/insights")
                        .param("destination", "Mumbai")
                        .param("travelType", "LEISURE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.destinationCity").value("Mumbai"))
                .andExpect(jsonPath("$.data.topAttractions").isArray());
    }

    @Test
    @DisplayName("GET /v1/ai/delay-explanation returns 200 OK with passenger advice")
    void testGetDelayExplanation() throws Exception {
        FlightDelayExplanationResponse mockResponse = new FlightDelayExplanationResponse(
                "AI-101",
                "DEL",
                "BOM",
                "Air traffic hold",
                "Flight is waiting for runway clearance",
                List.of("Remain in gate area", "Check boarding pass"),
                "30 mins",
                false
        );

        when(geminiTravelInsightService.generateDelayExplanation(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/v1/ai/delay-explanation")
                        .param("flightNumber", "AI-101")
                        .param("origin", "DEL")
                        .param("destination", "BOM")
                        .param("reason", "Air traffic hold")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flightNumber").value("AI-101"))
                .andExpect(jsonPath("$.data.primaryReason").value("Air traffic hold"));
    }
}
