package com.smarttravel.modules.review.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Review service implementation with moderation lifecycle.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    /** Auto-flag if a review gets this many flags */
    private static final int AUTO_FLAG_THRESHOLD = 3;

    private final ReviewRepository reviewRepository;
    private final com.smarttravel.modules.review.service.storage.ReviewMediaStorageService mediaStorageService;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             com.smarttravel.modules.review.service.storage.ReviewMediaStorageService mediaStorageService) {
        this.reviewRepository = reviewRepository;
        this.mediaStorageService = mediaStorageService;
    }

    @Override
    public Review createReview(String userId, String userFullName, ReviewTargetType targetType,
                                String targetId, String targetName, double rating,
                                double cleanlinessRating, double serviceRating, double valueRating,
                                String title, String body, String bookingId) {
        // Prevent duplicate reviews
        if (reviewRepository.findByUserIdAndTargetId(userId, targetId).isPresent()) {
            throw new ConflictException("You have already submitted a review for this " +
                    targetType.name().toLowerCase() + ".");
        }

        // Validate rating
        if (rating < 1.0 || rating > 5.0) {
            throw new BadRequestException("Rating must be between 1.0 and 5.0");
        }

        Review review = Review.builder()
                .userId(userId)
                .userFullName(userFullName)
                .targetType(targetType)
                .targetId(targetId)
                .targetName(targetName)
                .rating(Math.round(rating * 2) / 2.0) // round to 0.5
                .cleanlinessRating(cleanlinessRating)
                .serviceRating(serviceRating)
                .valueRating(valueRating)
                .title(title)
                .body(body)
                .status(ReviewStatus.PUBLISHED)
                .helpfulVoters(new ArrayList<>())
                .flaggedBy(new ArrayList<>())
                .bookingId(bookingId)
                .verifiedPurchase(bookingId != null && !bookingId.isBlank())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review {} created by user {} for {} {}", saved.getId(), userId, targetType, targetId);
        return saved;
    }

    @Override
    public Page<Review> getReviewsForTarget(ReviewTargetType targetType, String targetId, Pageable pageable) {
        return reviewRepository.findByTargetTypeAndTargetIdAndStatusOrderByCreatedAtDesc(
                targetType, targetId, ReviewStatus.PUBLISHED, pageable);
    }

    @Override
    public Page<Review> getUserReviews(String userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Review getReviewById(String reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
    }

    @Override
    public Review voteHelpful(String reviewId, String votingUserId) {
        Review review = getReviewById(reviewId);
        if (review.getUserId().equals(votingUserId)) {
            throw new BadRequestException("You cannot vote your own review as helpful.");
        }

        List<String> voters = review.getHelpfulVoters();
        if (voters.contains(votingUserId)) {
            // Toggle — remove if already voted
            voters.remove(votingUserId);
        } else {
            voters.add(votingUserId);
        }
        review.setHelpfulVoters(voters);
        return reviewRepository.save(review);
    }

    @Override
    public Review flagReview(String reviewId, String flaggingUserId) {
        Review review = getReviewById(reviewId);
        if (review.getUserId().equals(flaggingUserId)) {
            throw new BadRequestException("You cannot flag your own review.");
        }

        List<String> flagged = review.getFlaggedBy();
        if (flagged.contains(flaggingUserId)) {
            throw new ConflictException("You have already flagged this review.");
        }
        flagged.add(flaggingUserId);
        review.setFlaggedBy(flagged);

        // Auto-promote to FLAGGED status if threshold reached
        if (flagged.size() >= AUTO_FLAG_THRESHOLD && review.getStatus() == ReviewStatus.PUBLISHED) {
            review.setStatus(ReviewStatus.FLAGGED);
            log.info("Review {} auto-flagged for admin review ({} flags)", reviewId, flagged.size());
        }

        return reviewRepository.save(review);
    }

    @Override
    public Review approveReview(String reviewId, String adminUserId) {
        Review review = getReviewById(reviewId);
        review.setStatus(ReviewStatus.PUBLISHED);
        review.getFlaggedBy().clear();
        review.setModeratedBy(adminUserId);
        review.setModeratedAt(Instant.now());
        review.setModerationNote("Approved by admin");
        return reviewRepository.save(review);
    }

    @Override
    public Review removeReview(String reviewId, String adminUserId, String reason) {
        Review review = getReviewById(reviewId);
        review.setStatus(ReviewStatus.REMOVED);
        review.setModeratedBy(adminUserId);
        review.setModeratedAt(Instant.now());
        review.setModerationNote(reason);
        log.info("Review {} removed by admin {} for reason: {}", reviewId, adminUserId, reason);
        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(String reviewId, String userId) {
        Review review = getReviewById(reviewId);
        if (!review.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete your own reviews.");
        }
        reviewRepository.deleteById(reviewId);
        log.info("Review {} deleted by owner {}", reviewId, userId);
    }

    @Override
    public double getAverageRating(ReviewTargetType targetType, String targetId) {
        List<Review> reviews = reviewRepository.findRatingsForTarget(targetType, targetId);
        OptionalDouble avg = reviews.stream()
                .mapToDouble(Review::getRating)
                .average();
        return avg.isPresent() ? Math.round(avg.getAsDouble() * 10.0) / 10.0 : 0.0;
    }

    @Override
    public Review attachPhoto(String reviewId, String userId, org.springframework.web.multipart.MultipartFile file, boolean isAdmin) {
        Review review = getReviewById(reviewId);
        if (!isAdmin && !review.getUserId().equals(userId)) {
            throw new BadRequestException("You can only upload photos to your own reviews.");
        }

        if (review.getPhotos() != null && review.getPhotos().size() >= 5) {
            throw new BadRequestException("Maximum of 5 photos allowed per review.");
        }

        String photoUrl = mediaStorageService.storePhoto(reviewId, file);
        if (review.getPhotos() == null) {
            review.setPhotos(new ArrayList<>());
        }
        review.getPhotos().add(photoUrl);
        review.setUpdatedAt(Instant.now());
        log.info("Attached photo '{}' to review ID '{}'", photoUrl, reviewId);
        return reviewRepository.save(review);
    }

    @Override
    public byte[] getPhotoBytes(String filename) {
        return mediaStorageService.loadPhoto(filename);
    }

    @Override
    public String getPhotoContentType(String filename) {
        return mediaStorageService.getContentType(filename);
    }
}
