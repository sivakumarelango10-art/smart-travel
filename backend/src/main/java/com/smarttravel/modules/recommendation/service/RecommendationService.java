package com.smarttravel.modules.recommendation.service;

import com.smarttravel.modules.recommendation.dto.RecommendationItem;
import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;

import java.util.List;
import java.util.Map;

/**
 * Service for tracking user activity and generating personalized recommendations.
 */
public interface RecommendationService {

    /**
     * Record a user activity event.
     */
    void trackActivity(String userId, UserActivityType type, String targetId,
                       String targetType, Map<String, Object> metadata);

    /**
     * Get personalized recommendations for a user.
     * Hybrid: content-based (40%) + collaborative (35%) + popularity (15%) + preference (10%)
     */
    List<RecommendationItem> getRecommendations(String userId, int limit);

    /**
     * Get recommended flights for a user.
     */
    List<RecommendationItem> getFlightRecommendations(String userId, int limit);

    /**
     * Get recommended hotels for a user.
     */
    List<RecommendationItem> getHotelRecommendations(String userId, int limit);

    /**
     * Get popular destinations (public, no login required).
     */
    List<RecommendationItem> getPopularDestinations(int limit);
}
