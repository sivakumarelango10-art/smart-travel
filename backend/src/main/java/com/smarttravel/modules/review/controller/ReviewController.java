package com.smarttravel.modules.review.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user reviews and ratings.
 */
@RestController
@RequestMapping({"/api/v1/reviews", "/v1/reviews"})
@Tag(name = "Reviews", description = "User reviews and ratings for flights and hotels")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "Submit a new review")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Review>> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            Authentication authentication) {

        String userId = authentication.getName();
        String userFullName = authentication.getName(); // Will be overridden by JWT details if available

        // Try to extract displayName from JWT principal
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails ud) {
            userFullName = ud.getUsername();
        }

        Review review = reviewService.createReview(
                userId,
                request.userDisplayName() != null ? request.userDisplayName() : userFullName,
                request.targetType(),
                request.targetId(),
                request.targetName(),
                request.rating(),
                request.cleanlinessRating(),
                request.serviceRating(),
                request.valueRating(),
                request.title(),
                request.body(),
                request.bookingId()
        );

        return ResponseEntity.ok(ApiResponse.success("Review submitted", review));
    }

    @Operation(summary = "Get reviews for a specific flight or hotel")
    @GetMapping({"", "/target/{targetType}/{targetId}"})
    public ResponseEntity<ApiResponse<Page<Review>>> getReviews(
            @PathVariable(required = false) ReviewTargetType targetType,
            @PathVariable(required = false) String targetId,
            @RequestParam(required = false) ReviewTargetType targetTypeParam,
            @RequestParam(required = false) String targetIdParam,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ReviewTargetType effectiveType = targetType != null ? targetType : targetTypeParam;
        String effectiveId = targetId != null ? targetId : targetIdParam;

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Review> reviews = (effectiveType != null && effectiveId != null)
                ? reviewService.getReviewsForTarget(effectiveType, effectiveId, pageable)
                : Page.empty(pageable);
        return ResponseEntity.ok(ApiResponse.success("Reviews retrieved", reviews));
    }

    @Operation(summary = "Get average rating for a target")
    @GetMapping("/rating")
    public ResponseEntity<ApiResponse<Double>> getAverageRating(
            @RequestParam ReviewTargetType targetType,
            @RequestParam String targetId) {
        double avg = reviewService.getAverageRating(targetType, targetId);
        return ResponseEntity.ok(ApiResponse.success("Average rating retrieved", avg));
    }

    @Operation(summary = "Get reviews by the current user")
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<Review>>> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Review> reviews = reviewService.getUserReviews(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Your reviews retrieved", reviews));
    }

    @Operation(summary = "Vote a review as helpful")
    @PostMapping("/{reviewId}/helpful")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Review>> voteHelpful(
            @PathVariable String reviewId,
            Authentication authentication) {
        String userId = authentication.getName();
        Review review = reviewService.voteHelpful(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success("Helpfulness vote recorded", review));
    }

    @Operation(summary = "Flag a review for moderation")
    @PostMapping("/{reviewId}/flag")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Review>> flagReview(
            @PathVariable String reviewId,
            Authentication authentication) {
        String userId = authentication.getName();
        Review review = reviewService.flagReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success("Review flagged for moderation", review));
    }

    @Operation(summary = "Upload and attach a photo to an existing review")
    @PostMapping(value = "/{reviewId}/photos", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Review>> uploadPhoto(
            @PathVariable String reviewId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication authentication) {
        String userId = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Review updated = reviewService.attachPhoto(reviewId, userId, file, isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Photo attached successfully", updated));
    }

    @Operation(summary = "Get photo content by filename")
    @GetMapping("/photos/{filename}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String filename) {
        byte[] bytes = reviewService.getPhotoBytes(filename);
        String contentType = reviewService.getPhotoContentType(filename);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                .header(org.springframework.http.HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(bytes);
    }

    @Operation(summary = "Delete own review")
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable String reviewId,
            Authentication authentication) {
        String userId = authentication.getName();
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }

    // ── Request ───────────────────────────────────────────────────────────────

    public record CreateReviewRequest(
            @NotNull(message = "targetType is required") ReviewTargetType targetType,
            @NotBlank(message = "targetId is required") String targetId,
            String targetName,
            String userDisplayName,
            @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
            @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
            double rating,
            double cleanlinessRating,
            double serviceRating,
            double valueRating,
            @NotBlank(message = "Review title is required")
            @Size(min = 5, max = 150, message = "Title must be 5-150 characters")
            String title,
            @NotBlank(message = "Review body is required")
            @Size(min = 20, max = 2000, message = "Body must be 20-2000 characters")
            String body,
            String bookingId
    ) {}
}
