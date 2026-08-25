package com.smarttravel.modules.ai.service;

import com.smarttravel.modules.ai.dto.FlightDelayExplanationResponse;
import com.smarttravel.modules.ai.dto.TravelInsightResponse;

public interface GeminiTravelInsightService {

    /**
     * Generates intelligent, structured destination travel insights.
     */
    TravelInsightResponse generateTravelInsights(String destinationCity, String travelType);

    /**
     * Generates a traveler-friendly explanation and passenger advice for a flight delay.
     */
    FlightDelayExplanationResponse generateDelayExplanation(String flightNumber, String origin, String destination, String standardReason);
}
