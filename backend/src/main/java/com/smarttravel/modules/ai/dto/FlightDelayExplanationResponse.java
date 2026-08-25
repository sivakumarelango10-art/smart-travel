package com.smarttravel.modules.ai.dto;

import java.util.List;

/**
 * AI-generated flight delay explanation with traveler guidance.
 */
public record FlightDelayExplanationResponse(
        String flightNumber,
        String origin,
        String destination,
        String primaryReason,
        String detailedExplanation,
        List<String> passengerAdvice,
        String estimatedImpact,
        boolean fallback
) {}
