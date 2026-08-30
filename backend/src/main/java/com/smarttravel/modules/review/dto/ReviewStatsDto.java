package com.smarttravel.modules.review.dto;

import java.util.Map;

/**
 * Aggregated statistics and star distribution for reviews on a hotel or flight.
 */
public record ReviewStatsDto(
        double averageRating,
        long totalReviews,
        long count5Stars,
        long count4Stars,
        long count3Stars,
        long count2Stars,
        long count1Star,
        double averageCleanliness,
        double averageService,
        double averageValue,
        Map<String, Long> ratingDistribution
) {}
