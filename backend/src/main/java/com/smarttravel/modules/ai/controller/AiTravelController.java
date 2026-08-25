package com.smarttravel.modules.ai.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.ai.dto.FlightDelayExplanationResponse;
import com.smarttravel.modules.ai.dto.TravelInsightResponse;
import com.smarttravel.modules.ai.service.GeminiTravelInsightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/v1/ai", "/api/v1/ai", "/api/ai"})
@Tag(name = "AI Travel Assistant", description = "Server-side Gemini AI Travel Insights and Delay Explanations")
public class AiTravelController {

    private final GeminiTravelInsightService geminiTravelInsightService;

    public AiTravelController(GeminiTravelInsightService geminiTravelInsightService) {
        this.geminiTravelInsightService = geminiTravelInsightService;
    }

    @GetMapping("/insights")
    @Operation(summary = "Get destination travel insights and recommendations")
    public ResponseEntity<ApiResponse<TravelInsightResponse>> getTravelInsights(
            @RequestParam(required = false, defaultValue = "Mumbai") String destination,
            @RequestParam(required = false, defaultValue = "LEISURE") String travelType) {
        TravelInsightResponse response = geminiTravelInsightService.generateTravelInsights(destination, travelType);
        return ResponseEntity.ok(ApiResponse.success("Travel insights retrieved successfully", response));
    }

    @GetMapping("/delay-explanation")
    @Operation(summary = "Get intelligent traveler-friendly flight delay explanation")
    public ResponseEntity<ApiResponse<FlightDelayExplanationResponse>> getDelayExplanation(
            @RequestParam(required = false, defaultValue = "AI-101") String flightNumber,
            @RequestParam(required = false, defaultValue = "DEL") String origin,
            @RequestParam(required = false, defaultValue = "BOM") String destination,
            @RequestParam(required = false, defaultValue = "Air traffic control hold") String reason) {
        FlightDelayExplanationResponse response = geminiTravelInsightService.generateDelayExplanation(flightNumber, origin, destination, reason);
        return ResponseEntity.ok(ApiResponse.success("Delay explanation retrieved successfully", response));
    }
}
