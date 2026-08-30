package com.smarttravel.modules.review.repository;

import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

/**
 * Repository for review queries.
 */
public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findByTargetTypeAndTargetIdAndStatusOrderByCreatedAtDesc(
            ReviewTargetType targetType, String targetId, ReviewStatus status, Pageable pageable);

    Page<Review> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    Optional<Review> findByUserIdAndTargetId(String userId, String targetId);

    Page<Review> findByStatusOrderByFlagCountDescCreatedAtDesc(ReviewStatus status, Pageable pageable);

    Page<Review> findByStatus(ReviewStatus status, Pageable pageable);

    Page<Review> findByStatusAndTargetType(ReviewStatus status, ReviewTargetType targetType, Pageable pageable);

    long countByStatus(ReviewStatus status);

    @Query("{'targetType': ?0, 'targetId': ?1, 'status': 'PUBLISHED'}")
    java.util.List<Review> findPublishedByTarget(String targetType, String targetId);

    long countByTargetTypeAndTargetIdAndStatus(
            ReviewTargetType targetType, String targetId, ReviewStatus status);

    @Query(value = "{'targetType': ?0, 'targetId': ?1, 'status': 'PUBLISHED'}", 
           fields = "{'rating': 1}")
    java.util.List<Review> findRatingsForTarget(ReviewTargetType targetType, String targetId);
}
