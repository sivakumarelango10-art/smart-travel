package com.smarttravel.modules.review.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin REST controller for managing, moderating, approving, and removing reviews.
 */
@RestController
@RequestMapping({"/api/v1/admin/reviews", "/v1/admin/reviews", "/admin/reviews", "/api/v1/moderation/reviews"})
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Review Moderation", description = "Administrator APIs for reviewing and moderating user feedback")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Get flagged reviews pending moderation")
    @GetMapping("/flagged")
    public ResponseEntity<ApiResponse<Page<Review>>> getFlaggedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Review> reviews = reviewService.getReviewsForAdmin(ReviewStatus.FLAGGED, null, pageable);
        return ResponseEntity.ok(ApiResponse.success("Flagged reviews retrieved", reviews));
    }

    @Operation(summary = "Get reviews by status and target type")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Review>>> getAllReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) ReviewTargetType targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Review> reviews = reviewService.getReviewsForAdmin(status, targetType, pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", reviews));
    }

    @Operation(summary = "Approve a flagged review")
    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<ApiResponse<Review>> approveReview(
            @PathVariable String reviewId,
            Authentication authentication) {
        String adminId = (authentication != null && authentication.getName() != null) ? authentication.getName() : "admin-user";
        Review approved = reviewService.approveReview(reviewId, adminId);
        return ResponseEntity.ok(ApiResponse.success("Review approved and published", approved));
    }

    @Operation(summary = "Hide a review from public listings")
    @PostMapping("/{reviewId}/hide")
    public ResponseEntity<ApiResponse<Review>> hideReview(
            @PathVariable String reviewId,
            Authentication authentication) {
        String adminId = (authentication != null && authentication.getName() != null) ? authentication.getName() : "admin-user";
        Review hidden = reviewService.hideReview(reviewId, adminId);
        return ResponseEntity.ok(ApiResponse.success("Review hidden", hidden));
    }

    @Operation(summary = "Remove a review for guideline violations")
    @PostMapping("/{reviewId}/remove")
    public ResponseEntity<ApiResponse<Review>> removeReview(
            @PathVariable String reviewId,
            @RequestBody(required = false) RemoveReviewRequest request,
            Authentication authentication) {
        String adminId = (authentication != null && authentication.getName() != null) ? authentication.getName() : "admin-user";
        String reason = request != null ? request.reason() : "Violates community standards";
        Review removed = reviewService.removeReview(reviewId, adminId, reason);
        return ResponseEntity.ok(ApiResponse.success("Review removed", removed));
    }

    @Operation(summary = "Restore a hidden or removed review")
    @PostMapping("/{reviewId}/restore")
    public ResponseEntity<ApiResponse<Review>> restoreReview(
            @PathVariable String reviewId,
            Authentication authentication) {
        String adminId = (authentication != null && authentication.getName() != null) ? authentication.getName() : "admin-user";
        Review restored = reviewService.restoreReview(reviewId, adminId);
        return ResponseEntity.ok(ApiResponse.success("Review restored to published state", restored));
    }

    public record RemoveReviewRequest(String reason) {}
}
