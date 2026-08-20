package com.smarttravel.modules.review.service;

import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service for creating, retrieving, and moderating reviews.
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
     * Get published reviews for a specific target.
     */
    Page<Review> getReviewsForTarget(ReviewTargetType targetType, String targetId, Pageable pageable);

    /**
     * Get all reviews by a user.
     */
    Page<Review> getUserReviews(String userId, Pageable pageable);

    /**
     * Get a review by ID (ownership-checked for edit/delete).
     */
    Review getReviewById(String reviewId);

    /**
     * Vote a review as helpful.
     */
    Review voteHelpful(String reviewId, String votingUserId);

    /**
     * Flag a review for moderation.
     */
    Review flagReview(String reviewId, String flaggingUserId);

    /**
     * Admin: approve a flagged review back to published.
     */
    Review approveReview(String reviewId, String adminUserId);

    /**
     * Admin: remove a review (moderation action).
     */
    Review removeReview(String reviewId, String adminUserId, String reason);

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
