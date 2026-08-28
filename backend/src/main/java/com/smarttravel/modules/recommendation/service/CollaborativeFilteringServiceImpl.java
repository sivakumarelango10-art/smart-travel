package com.smarttravel.modules.recommendation.service;

import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;
import com.smarttravel.modules.recommendation.repository.UserActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Production-ready lightweight item-based collaborative filtering implementation.
 * Builds user-item affinity vectors from weighted platform interactions:
 * - BOOK = 5.0
 * - REVIEW = 4.0
 * - TRACK = 3.0
 * - VIEW / EXTENDED_VIEW = 2.0
 * - SEARCH = 1.0
 * Computes vector cosine similarity between items and scores candidate items for target users.
 */
@Service
public class CollaborativeFilteringServiceImpl implements CollaborativeFilteringService {

    private static final Logger log = LoggerFactory.getLogger(CollaborativeFilteringServiceImpl.class);

    private final UserActivityRepository activityRepository;

    // Configurable interaction weights
    public static final double WEIGHT_BOOK = 5.0;
    public static final double WEIGHT_REVIEW = 4.0;
    public static final double WEIGHT_TRACK = 3.0;
    public static final double WEIGHT_VIEW = 2.0;
    public static final double WEIGHT_SEARCH = 1.0;

    public CollaborativeFilteringServiceImpl(UserActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public Map<String, Double> computeCollaborativeScores(String userId, Set<String> candidateTargetIds) {
        if (userId == null || userId.isBlank() || candidateTargetIds == null || candidateTargetIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // Fetch user interactions across the platform
        List<UserActivity> allActivities = activityRepository.findAll();
        if (allActivities == null || allActivities.isEmpty()) {
            return Collections.emptyMap();
        }

        // Fast check: exit immediately if target user has zero activities
        boolean hasTargetUserActivity = allActivities.stream().anyMatch(a -> userId.equals(a.getUserId()));
        if (!hasTargetUserActivity) {
            return Collections.emptyMap();
        }

        // Build User -> (Item -> Weight) profile matrix
        Map<String, Map<String, Double>> userItemMatrix = new HashMap<>();

        for (UserActivity act : allActivities) {
            String u = act.getUserId();
            String item = resolveItemKey(act);
            if (u == null || item == null) continue;

            double weight = getInteractionWeight(act.getActivityType());
            userItemMatrix.computeIfAbsent(u, k -> new HashMap<>())
                    .merge(item, weight, Double::sum);
        }

        // Build Item -> (User -> Weight) inverse index for fast similarity lookup
        Map<String, Map<String, Double>> itemUserMatrix = new HashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : userItemMatrix.entrySet()) {
            String u = entry.getKey();
            for (Map.Entry<String, Double> itemEntry : entry.getValue().entrySet()) {
                itemUserMatrix.computeIfAbsent(itemEntry.getKey(), k -> new HashMap<>())
                        .put(u, itemEntry.getValue());
            }
        }

        Map<String, Double> targetUserItems = userItemMatrix.getOrDefault(userId, Collections.emptyMap());
        if (targetUserItems.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Double> scores = new HashMap<>();

        for (String candidateId : candidateTargetIds) {
            // If user already heavily interacted with candidate, collaborative score is based on associated items
            double candidateScore = 0.0;
            double totalSimWeight = 0.0;

            for (Map.Entry<String, Double> userItemEntry : targetUserItems.entrySet()) {
                String userItem = userItemEntry.getKey();
                if (userItem.equalsIgnoreCase(candidateId)) continue; // Don't self-compare

                double similarity = computeCosineSimilarity(userItem, candidateId, itemUserMatrix);
                if (similarity > 0.0) {
                    candidateScore += userItemEntry.getValue() * similarity;
                    totalSimWeight += similarity;
                }
            }

            if (totalSimWeight > 0.0) {
                // Normalize to a [0.0 - 100.0] scale
                double normalized = Math.min(100.0, (candidateScore / totalSimWeight) * 20.0);
                scores.put(candidateId, Math.round(normalized * 10.0) / 10.0);
            } else {
                scores.put(candidateId, 0.0);
            }
        }

        log.debug("Computed collaborative filtering scores for user '{}': {}", userId, scores);
        return scores;
    }

    @Override
    public double computeItemSimilarity(String itemIdA, String itemIdB) {
        if (itemIdA == null || itemIdB == null || itemIdA.equalsIgnoreCase(itemIdB)) {
            return 1.0;
        }

        List<UserActivity> allActivities = activityRepository.findAll();
        Map<String, Map<String, Double>> itemUserMatrix = new HashMap<>();

        for (UserActivity act : allActivities) {
            String u = act.getUserId();
            String item = resolveItemKey(act);
            if (u == null || item == null) continue;

            double weight = getInteractionWeight(act.getActivityType());
            itemUserMatrix.computeIfAbsent(item, k -> new HashMap<>())
                    .merge(u, weight, Double::sum);
        }

        return computeCosineSimilarity(itemIdA, itemIdB, itemUserMatrix);
    }

    private double computeCosineSimilarity(String itemA, String itemB, Map<String, Map<String, Double>> itemUserMatrix) {
        Map<String, Double> usersA = itemUserMatrix.get(itemA);
        Map<String, Double> usersB = itemUserMatrix.get(itemB);

        if (usersA == null || usersB == null) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (double w : usersA.values()) {
            normA += w * w;
        }
        for (double w : usersB.values()) {
            normB += w * w;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        for (Map.Entry<String, Double> entryA : usersA.entrySet()) {
            String user = entryA.getKey();
            if (usersB.containsKey(user)) {
                dotProduct += entryA.getValue() * usersB.get(user);
            }
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-9);
    }

    private String resolveItemKey(UserActivity act) {
        if (act.getTargetId() != null && !act.getTargetId().isBlank()) {
            return act.getTargetId();
        }
        if (act.getMetadata() != null) {
            if (act.getMetadata().containsKey("arrivalAirport")) {
                return (String) act.getMetadata().get("arrivalAirport");
            }
            if (act.getMetadata().containsKey("city")) {
                return (String) act.getMetadata().get("city");
            }
        }
        return null;
    }

    private double getInteractionWeight(UserActivityType type) {
        if (type == null) return WEIGHT_SEARCH;
        return switch (type) {
            case BOOK -> WEIGHT_BOOK;
            case REVIEW -> WEIGHT_REVIEW;
            case TRACK -> WEIGHT_TRACK;
            case VIEW, EXTENDED_VIEW, VIEW_HOTEL -> WEIGHT_VIEW;
            case SEARCH, SEARCH_HOTEL -> WEIGHT_SEARCH;
        };
    }
}
