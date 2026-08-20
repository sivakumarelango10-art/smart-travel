package com.smarttravel.modules.recommendation.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.recommendation.dto.RecommendationItem;
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
 * REST controller for personalized recommendations and activity tracking.
 */
@RestController
@RequestMapping("/v1/recommendations")
@Tag(name = "Recommendations", description = "Personalized flight and hotel recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Operation(summary = "Get personalized recommendations (mixed flights + hotels)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        String userId = authentication != null && authentication.isAuthenticated()
                ? authentication.getName() : null;
        List<RecommendationItem> recs = recommendationService.getRecommendations(userId, Math.min(limit, 20));
        return ResponseEntity.ok(ApiResponse.success("Recommendations retrieved", recs));
    }

    @Operation(summary = "Get recommended flights")
    @GetMapping("/flights")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getFlightRecommendations(
            @RequestParam(defaultValue = "8") int limit,
            Authentication authentication) {
        String userId = authentication != null && authentication.isAuthenticated()
                ? authentication.getName() : null;
        List<RecommendationItem> recs = recommendationService.getFlightRecommendations(userId, Math.min(limit, 20));
        return ResponseEntity.ok(ApiResponse.success("Flight recommendations retrieved", recs));
    }

    @Operation(summary = "Get recommended hotels")
    @GetMapping("/hotels")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getHotelRecommendations(
            @RequestParam(defaultValue = "8") int limit,
            Authentication authentication) {
        String userId = authentication != null && authentication.isAuthenticated()
                ? authentication.getName() : null;
        List<RecommendationItem> recs = recommendationService.getHotelRecommendations(userId, Math.min(limit, 20));
        return ResponseEntity.ok(ApiResponse.success("Hotel recommendations retrieved", recs));
    }

    @Operation(summary = "Get popular destinations (public)")
    @GetMapping("/destinations")
    public ResponseEntity<ApiResponse<List<RecommendationItem>>> getPopularDestinations(
            @RequestParam(defaultValue = "6") int limit) {
        List<RecommendationItem> recs = recommendationService.getPopularDestinations(Math.min(limit, 20));
        return ResponseEntity.ok(ApiResponse.success("Popular destinations retrieved", recs));
    }

    @Operation(summary = "Track a user activity event (for better recommendations)")
    @PostMapping("/track")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> trackActivity(
            @RequestBody TrackActivityRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        recommendationService.trackActivity(
                userId,
                request.activityType(),
                request.targetId(),
                request.targetType(),
                request.metadata()
        );
        return ResponseEntity.ok(ApiResponse.success("Activity tracked"));
    }

    // ── Request ───────────────────────────────────────────────────────────────

    public record TrackActivityRequest(
            UserActivityType activityType,
            String targetId,
            String targetType,
            Map<String, Object> metadata
    ) {}
}
