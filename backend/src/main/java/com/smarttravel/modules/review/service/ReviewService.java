package com.smarttravel.modules.review.service;

import com.smarttravel.modules.review.dto.ReviewStatsDto;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for creating, retrieving, filtering, sorting, and moderating reviews.
 */
public interface ReviewService {

    /**
     * Submit a new review for a flight or hotel.
     */
    Review createReview(String userId, String userFullName, ReviewTargetType targetType,
                        String targetId, String targetName, double rating,
                        double cleanlinessRating, double serviceRating, double valueRating,
                        String title, String body, String bookingId);

    /**
     * Get published reviews for a specific target with pagination (backward-compatible).
     */
    Page<Review> getReviewsForTarget(ReviewTargetType targetType, String targetId, Pageable pageable);

    /**
     * Get published reviews for a specific target with advanced sorting and filtering.
     *
     * @param sortBy "NEWEST", "MOST_HELPFUL", "HIGHEST_RATED", "LOWEST_RATED", "OLDEST"
     * @param ratingFilter Optional filter by exact star rating (e.g. 5, 4, 3, 2, 1)
     * @param verifiedOnly Optional filter for verified bookings only
     * @param withPhotosOnly Optional filter for reviews with photo attachments only
     */
    Page<Review> getReviewsForTarget(ReviewTargetType targetType, String targetId,
                                    String sortBy, Integer ratingFilter,
                                    Boolean verifiedOnly, Boolean withPhotosOnly,
                                    Pageable pageable);

    /**
     * Get detailed star breakdown and category averages for a target.
     */
    ReviewStatsDto getReviewStats(ReviewTargetType targetType, String targetId);

    /**
     * Get all reviews submitted by a user.
     */
    Page<Review> getUserReviews(String userId, Pageable pageable);

    /**
     * Get a review by ID.
     */
    Review getReviewById(String reviewId);

    /**
     * Vote a review as helpful (toggle).
     */
    Review voteHelpful(String reviewId, String votingUserId);

    /**
     * Flag a review for moderation.
     */
    Review flagReview(String reviewId, String flaggingUserId);

    /**
     * Admin: get reviews for moderation (filter by status/targetType).
     */
    Page<Review> getReviewsForAdmin(ReviewStatus status, ReviewTargetType targetType, Pageable pageable);

    /**
     * Admin: approve a flagged review back to published.
     */
    Review approveReview(String reviewId, String adminUserId);

    /**
     * Admin: hide a review without deleting.
     */
    Review hideReview(String reviewId, String adminUserId);

    /**
     * Admin: remove a review (moderation action with reason).
     */
    Review removeReview(String reviewId, String adminUserId, String reason);

    /**
     * Admin: restore a previously hidden or removed review.
     */
    Review restoreReview(String reviewId, String adminUserId);

    /**
     * Delete own review.
     */
    void deleteReview(String reviewId, String userId);

    /**
     * Attach an uploaded photo to an existing review.
     */
    Review attachPhoto(String reviewId, String userId, org.springframework.web.multipart.MultipartFile file, boolean isAdmin);

    /**
     * Load raw photo bytes by filename.
     */
    byte[] getPhotoBytes(String filename);

    /**
     * Resolve MIME content type for a photo.
     */
    String getPhotoContentType(String filename);

    /**
     * Get average rating for a target.
     */
    double getAverageRating(ReviewTargetType targetType, String targetId);
}
