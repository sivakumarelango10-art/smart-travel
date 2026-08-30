package com.smarttravel.modules.review.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.review.dto.ReviewStatsDto;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

/**
 * Review service implementation with advanced sorting, dynamic filtering,
 * photo attachments, and full admin moderation lifecycle.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    /** Auto-flag if a review gets this many flags */
    private static final int AUTO_FLAG_THRESHOLD = 3;

    private final ReviewRepository reviewRepository;
    private final ReviewMediaStorageService mediaStorageService;
    private final MongoTemplate mongoTemplate;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             ReviewMediaStorageService mediaStorageService,
                             MongoTemplate mongoTemplate) {
        this.reviewRepository = reviewRepository;
        this.mediaStorageService = mediaStorageService;
        this.mongoTemplate = mongoTemplate;
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
        return getReviewsForTarget(targetType, targetId, "NEWEST", null, false, false, pageable);
    }

    @Override
    public Page<Review> getReviewsForTarget(ReviewTargetType targetType, String targetId,
                                            String sortBy, Integer ratingFilter,
                                            Boolean verifiedOnly, Boolean withPhotosOnly,
                                            Pageable pageable) {
        Criteria criteria = Criteria.where("targetType").is(targetType)
                .and("targetId").is(targetId)
                .and("status").is(ReviewStatus.PUBLISHED);

        if (ratingFilter != null && ratingFilter >= 1 && ratingFilter <= 5) {
            criteria = criteria.and("rating").gte(ratingFilter.doubleValue())
                    .lt(ratingFilter + 1.0);
        }

        if (Boolean.TRUE.equals(verifiedOnly)) {
            criteria = criteria.and("verifiedPurchase").is(true);
        }

        if (Boolean.TRUE.equals(withPhotosOnly)) {
            criteria = criteria.and("photos.0").exists(true);
        }

        Query query = new Query(criteria);
        long total = mongoTemplate.count(query, Review.class);

        Sort sort = switch (sortBy != null ? sortBy.toUpperCase().trim() : "NEWEST") {
            case "MOST_HELPFUL" -> Sort.by(Sort.Direction.DESC, "helpfulCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "HIGHEST_RATED" -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "LOWEST_RATED" -> Sort.by(Sort.Direction.ASC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "OLDEST" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        query.with(pageRequest);

        List<Review> list = mongoTemplate.find(query, Review.class);
        return new PageImpl<>(list, pageRequest, total);
    }

    @Override
    public ReviewStatsDto getReviewStats(ReviewTargetType targetType, String targetId) {
        List<Review> published = reviewRepository.findPublishedByTarget(targetType.name(), targetId);

        if (published.isEmpty()) {
            return new ReviewStatsDto(0.0, 0, 0, 0, 0, 0, 0, 0.0, 0.0, 0.0,
                    Map.of("5", 0L, "4", 0L, "3", 0L, "2", 0L, "1", 0L));
        }

        long count5 = 0, count4 = 0, count3 = 0, count2 = 0, count1 = 0;
        double sumRating = 0, sumCleanliness = 0, sumService = 0, sumValue = 0;
        int countCleanliness = 0, countService = 0, countValue = 0;

        for (Review r : published) {
            double rating = r.getRating();
            sumRating += rating;

            if (rating >= 4.5) count5++;
            else if (rating >= 3.5) count4++;
            else if (rating >= 2.5) count3++;
            else if (rating >= 1.5) count2++;
            else count1++;

            if (r.getCleanlinessRating() > 0) {
                sumCleanliness += r.getCleanlinessRating();
                countCleanliness++;
            }
            if (r.getServiceRating() > 0) {
                sumService += r.getServiceRating();
                countService++;
            }
            if (r.getValueRating() > 0) {
                sumValue += r.getValueRating();
                countValue++;
            }
        }

        long total = published.size();
        double avgRating = Math.round((sumRating / total) * 10.0) / 10.0;
        double avgCleanliness = countCleanliness > 0 ? Math.round((sumCleanliness / countCleanliness) * 10.0) / 10.0 : avgRating;
        double avgService = countService > 0 ? Math.round((sumService / countService) * 10.0) / 10.0 : avgRating;
        double avgValue = countValue > 0 ? Math.round((sumValue / countValue) * 10.0) / 10.0 : avgRating;

        Map<String, Long> distribution = new LinkedHashMap<>();
        distribution.put("5", count5);
        distribution.put("4", count4);
        distribution.put("3", count3);
        distribution.put("2", count2);
        distribution.put("1", count1);

        return new ReviewStatsDto(
                avgRating,
                total,
                count5,
                count4,
                count3,
                count2,
                count1,
                avgCleanliness,
                avgService,
                avgValue,
                distribution
        );
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
    public Page<Review> getReviewsForAdmin(ReviewStatus status, ReviewTargetType targetType, Pageable pageable) {
        if (status != null && targetType != null) {
            return reviewRepository.findByStatusAndTargetType(status, targetType, pageable);
        } else if (status != null) {
            return reviewRepository.findByStatus(status, pageable);
        } else {
            return reviewRepository.findAll(pageable);
        }
    }

    @Override
    public Review approveReview(String reviewId, String adminUserId) {
        Review review = getReviewById(reviewId);
        review.setStatus(ReviewStatus.PUBLISHED);
        review.getFlaggedBy().clear();
        review.setFlagCount(0);
        review.setModeratedBy(adminUserId);
        review.setModeratedAt(Instant.now());
        review.setModerationNote("Approved by admin");
        return reviewRepository.save(review);
    }

    @Override
    public Review hideReview(String reviewId, String adminUserId) {
        Review review = getReviewById(reviewId);
        review.setStatus(ReviewStatus.HIDDEN);
        review.setModeratedBy(adminUserId);
        review.setModeratedAt(Instant.now());
        review.setModerationNote("Hidden by administrator");
        log.info("Review {} hidden by admin {}", reviewId, adminUserId);
        return reviewRepository.save(review);
    }

    @Override
    public Review removeReview(String reviewId, String adminUserId, String reason) {
        Review review = getReviewById(reviewId);
        review.setStatus(ReviewStatus.REMOVED);
        review.setModeratedBy(adminUserId);
        review.setModeratedAt(Instant.now());
        review.setModerationNote(reason != null && !reason.isBlank() ? reason : "Removed by administrator for violating community guidelines");
        log.info("Review {} removed by admin {} for reason: {}", reviewId, adminUserId, reason);
        return reviewRepository.save(review);
    }

    @Override
    public Review restoreReview(String reviewId, String adminUserId) {
        Review review = getReviewById(reviewId);
        review.setStatus(ReviewStatus.PUBLISHED);
        review.getFlaggedBy().clear();
        review.setFlagCount(0);
        review.setModeratedBy(adminUserId);
        review.setModeratedAt(Instant.now());
        review.setModerationNote("Restored by administrator");
        log.info("Review {} restored by admin {}", reviewId, adminUserId);
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

        // Security authorization check: only the author or an admin can attach photos
        if (!review.getUserId().equals(userId) && !isAdmin) {
            throw new BadRequestException("You can only upload photos to your own reviews.");
        }

        // Limit photos per review to 5
        if (review.getPhotos().size() >= 5) {
            throw new BadRequestException("A maximum of 5 photos can be uploaded per review.");
        }

        String storedFilename = mediaStorageService.storePhoto(reviewId, file);
        String photoUrl = (storedFilename != null && storedFilename.startsWith("/api/v1/reviews/photos/"))
                ? storedFilename
                : "/api/v1/reviews/photos/" + storedFilename;
        review.getPhotos().add(photoUrl);
        Review saved = reviewRepository.save(review);
        log.info("Photo {} attached to review {} by user {}", photoUrl, reviewId, userId);
        return saved;
    }

    @Override
    public byte[] getPhotoBytes(String filename) {
        String clean = (filename != null && filename.contains("/")) ? filename.substring(filename.lastIndexOf('/') + 1) : filename;
        return mediaStorageService.loadPhoto(clean);
    }

    @Override
    public String getPhotoContentType(String filename) {
        String clean = (filename != null && filename.contains("/")) ? filename.substring(filename.lastIndexOf('/') + 1) : filename;
        return mediaStorageService.getContentType(clean);
    }
}
