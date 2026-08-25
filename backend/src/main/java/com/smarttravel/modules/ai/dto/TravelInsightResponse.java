package com.smarttravel.modules.ai.dto;

import java.util.List;

/**
 * AI-generated destination travel insights and recommendations.
 */
public record TravelInsightResponse(
        String destinationCity,
        String summary,
        String bestTimeToVisit,
        List<String> topAttractions,
        List<String> localTips,
        String weatherInsight,
        String aiModel,
        boolean cached,
        boolean fallback
) {}
