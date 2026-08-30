package com.smarttravel.modules.recommendation.model;

/**
 * Types of feedback a user can submit on a personalized recommendation.
 */
public enum RecommendationFeedbackType {
    /** User marked the recommendation as helpful (positive reinforcement) */
    HELPFUL,
    /** User marked the recommendation as not relevant (negative penalty) */
    NOT_RELEVANT,
    /** User dismissed the recommendation card from view */
    DISMISS
}
