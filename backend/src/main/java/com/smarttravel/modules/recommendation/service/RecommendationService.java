package com.smarttravel.modules.recommendation.service;

import com.smarttravel.modules.recommendation.dto.RecommendationItem;
import com.smarttravel.modules.recommendation.dto.UserPreferenceProfileDto;
import com.smarttravel.modules.recommendation.model.RecommendationFeedback;
import com.smarttravel.modules.recommendation.model.RecommendationFeedbackType;
import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;

import java.util.List;
import java.util.Map;

/**
 * Service for tracking user activity, recording feedback, and generating explainable personalized recommendations.
 */
public interface RecommendationService {

    /**
     * Record a user activity event.
     */
    void trackActivity(String userId, UserActivityType type, String targetId,
                       String targetType, Map<String, Object> metadata);

    /**
     * Get personalized recommendations for a user (mixed flights, hotels, and destinations).
     * Hybrid: content-based (28%) + activity-based (22%) + collaborative (20%) + preference (15%) + popularity (15%) + feedback tuning.
     */
    List<RecommendationItem> getRecommendations(String userId, int limit);

    /**
     * Get contextual recommendations with destination and context awareness.
     */
    List<RecommendationItem> getRecommendations(String userId, String context, String destination, int limit);

    /**
     * Get recommended flights for a user.
     */
    List<RecommendationItem> getFlightRecommendations(String userId, int limit);

    /**
     * Get recommended hotels for a user.
     */
    List<RecommendationItem> getHotelRecommendations(String userId, int limit);

    /**
     * Get popular & personalized destinations for a user.
     */
    List<RecommendationItem> getPopularDestinations(int limit);

    /**
     * Get destination recommendations tailored to user style & categories.
     */
    List<RecommendationItem> getDestinationRecommendations(String userId, int limit);

    /**
     * Record user feedback (Helpful, Not Relevant, Dismiss) on a recommendation.
     */
    RecommendationFeedback recordFeedback(String userId, String targetId, String targetType,
                                          RecommendationFeedbackType feedbackType, String reasonCode, String category);

    /**
     * Synthesize and retrieve the user's inferred travel preference profile.
     */
    UserPreferenceProfileDto getUserPreferenceProfile(String userId);

    /**
     * Get recent activity history for a user.
     */
    List<UserActivity> getUserActivityHistory(String userId, int limit);
}
