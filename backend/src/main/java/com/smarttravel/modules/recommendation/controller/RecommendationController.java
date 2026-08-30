package com.smarttravel.modules.recommendation.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.recommendation.dto.RecommendationItem;
import com.smarttravel.modules.recommendation.dto.UserPreferenceProfileDto;
import com.smarttravel.modules.recommendation.model.RecommendationFeedback;
import com.smarttravel.modules.recommendation.model.RecommendationFeedbackType;
import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;
import com.smarttravel.modules.recommendation.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for personalized, explainable recommendations, feedback loop, and preference profiles.
 */
@RestController
@RequestMapping({"/api/v1/recommendations", "/v1/recommendations"})
@Tag(name = "Recommendations", description = "Explainable personalized recommendations and feedback loop")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Get personalized, explainable recommendations (mixed flights, hotels, destinations)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getRecommendations(
            @RequestParam(required = false) String context,
            @RequestParam(required = false) String destination,
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        String userId = getUserIdOrNull(authentication);
        List<RecommendationItem> recs = recommendationService.getRecommendations(userId, context, destination, Math.min(limit, 25));
        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved", recs));
    }

    @Operation(summary = "Get recommended flights with explainable reasoning")
    @GetMapping("/flights")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getFlightRecommendations(
            @RequestParam(defaultValue = "8") int limit,
            Authentication authentication) {
        String userId = getUserIdOrNull(authentication);
        List<RecommendationItem> recs = recommendationService.getFlightRecommendations(userId, Math.min(limit, 20));
        return ResponseEntity.ok(ApiResponse.success("Flight recommendations retrieved", recs));
    }

    @Operation(summary = "Get recommended hotels with explainable reasoning")
    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getHotelRecommendations(
            @RequestParam(defaultValue = "8") int limit,
            Authentication authentication) {
        String userId = getUserIdOrNull(authentication);
        List<RecommendationItem> recs = recommendationService.getHotelRecommendations(userId, Math.min(limit, 20));
        return ResponseEntity.ok(ApiResponse.success("Hotel recommendations retrieved", recs));
    }

    @Operation(summary = "Get personalized & trending destinations")
    @GetMapping("/destinations")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getDestinations(
            @RequestParam(defaultValue = "8") int limit,
            Authentication authentication) {
        String userId = getUserIdOrNull(authentication);
        List<RecommendationItem> recs = recommendationService.getDestinationRecommendations(userId, Math.min(limit, 20));
        return ResponseEntity.ok(ApiResponse.success("Destination recommendations retrieved", recs));
    }

    @Operation(summary = "Submit feedback on a recommendation (Helpful, Not Relevant, Dismiss)")
    @PostMapping({"/{targetId}/feedback", "/feedback"})
    public ResponseEntity<ApiResponse<RecommendationFeedback>> submitFeedback(
            @PathVariable(required = false) String targetId,
            @RequestBody SubmitFeedbackRequest request,
            Authentication authentication) {
        String userId = getUserIdOrNull(authentication);
        if (userId == null) {
            userId = "anon-guest";
        }

        String effectiveTargetId = targetId != null ? targetId : request.targetId();
        RecommendationFeedback feedback = recommendationService.recordFeedback(
                userId,
                effectiveTargetId,
                request.targetType(),
                request.feedbackType(),
                request.reasonCode(),
                request.category()
        );

        return ResponseEntity.ok(ApiResponse.success("Feedback recorded successfully", feedback));
    }

    @Operation(summary = "Get current user's inferred travel preference profile")
    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<UserPreferenceProfileDto>> getUserPreferences(
            Authentication authentication) {
        String userId = getUserIdOrNull(authentication);
        UserPreferenceProfileDto profile = recommendationService.getUserPreferenceProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("User preferences profile retrieved", profile));
    }

    @Operation(summary = "Get user activity history")
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserActivity>>> getUserActivityHistory(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        String userId = authentication.getName();
        List<UserActivity> history = recommendationService.getUserActivityHistory(userId, Math.min(limit, 50));
        return ResponseEntity.ok(ApiResponse.success("Activity history retrieved", history));
    }

    @Operation(summary = "Track user interaction event")
    @PostMapping("/track")
    public ResponseEntity<ApiResponse<Void>> trackActivity(
            @RequestBody TrackActivityRequest request,
            Authentication authentication) {
        String userId = getUserIdOrNull(authentication);
        if (userId == null) {
            userId = "anonymous";
        }

        recommendationService.trackActivity(
                userId,
                request.activityType(),
                request.targetId(),
                request.targetType(),
                request.metadata()
        );
        return ResponseEntity.ok(ApiResponse.success("Activity tracked"));
    }

    // ── Helper ─────────────────────────────────────────────────────────────────

    private String getUserIdOrNull(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }
        return null;
    }

    // ── Request DTOs ───────────────────────────────────────────────────────────

    public record TrackActivityRequest(
            UserActivityType activityType,
            String targetId,
            String targetType,
            Map<String, Object> metadata
    ) {}

    public record SubmitFeedbackRequest(
            String targetId,
            String targetType,
            RecommendationFeedbackType feedbackType,
            String reasonCode,
            String category
    ) {}
}
