package com.smarttravel.modules.recommendation.service;

import java.util.Map;
import java.util.Set;

/**
 * Item-based Collaborative Filtering Service.
 * Computes item co-occurrence similarity from user interaction patterns (searches, views, tracks, reviews, bookings)
 * and generates collaborative recommendation affinity scores for active travelers.
 */
public interface CollaborativeFilteringService {

    /**
     * Computes collaborative filtering affinity scores for a given user across candidate item target IDs.
     *
     * @param userId The target user ID.
     * @param candidateTargetIds Set of candidate entity IDs (flight/hotel/destination codes).
     * @return Map of candidate ID to collaborative score [0.0 - 100.0].
     */
    Map<String, Double> computeCollaborativeScores(String userId, Set<String> candidateTargetIds);

    /**
     * Calculates the item-item co-occurrence cosine similarity between two items based on user interaction weights.
     *
     * @param itemIdA First item ID.
     * @param itemIdB Second item ID.
     * @return Cosine similarity coefficient in range [0.0, 1.0].
     */
    double computeItemSimilarity(String itemIdA, String itemIdB);
}
