package com.smarttravel.modules.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.modules.ai.dto.FlightDelayExplanationResponse;
import com.smarttravel.modules.ai.dto.TravelInsightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeminiTravelInsightServiceTest {

    private GeminiTravelInsightServiceImpl service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new GeminiTravelInsightServiceImpl(objectMapper);
    }

    @Test
    @DisplayName("Should generate deterministic fallback travel insights when API key is unconfigured")
    void testTravelInsightsFallback() {
        TravelInsightResponse response = service.generateTravelInsights("Mumbai", "LEISURE");

        assertNotNull(response);
        assertEquals("Mumbai", response.destinationCity());
        assertNotNull(response.summary());
        assertFalse(response.topAttractions().isEmpty());
        assertFalse(response.localTips().isEmpty());
        assertTrue(response.fallback());
    }

    @Test
    @DisplayName("Should return cached travel insights on repeated calls")
    void testTravelInsightsCaching() {
        TravelInsightResponse first = service.generateTravelInsights("Delhi", "BUSINESS");
        TravelInsightResponse second = service.generateTravelInsights("Delhi", "BUSINESS");

        assertNotNull(first);
        assertNotNull(second);
        assertTrue(second.cached());
    }

    @Test
    @DisplayName("Should generate deterministic flight delay explanation when API key is unconfigured")
    void testFlightDelayExplanationFallback() {
        FlightDelayExplanationResponse response = service.generateDelayExplanation(
                "6E-551", "DEL", "HYD", "Air traffic congestion at New Delhi"
        );

        assertNotNull(response);
        assertEquals("6E-551", response.flightNumber());
        assertEquals("DEL", response.origin());
        assertEquals("HYD", response.destination());
        assertNotNull(response.detailedExplanation());
        assertFalse(response.passengerAdvice().isEmpty());
        assertTrue(response.fallback());
    }
}
