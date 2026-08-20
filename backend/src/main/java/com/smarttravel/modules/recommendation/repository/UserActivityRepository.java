package com.smarttravel.modules.recommendation.repository;

import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for user activity events.
 */
public interface UserActivityRepository extends MongoRepository<UserActivity, String> {

    List<UserActivity> findByUserIdOrderByCreatedAtDesc(String userId);

    List<UserActivity> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            String userId, Instant since);

    List<UserActivity> findByUserIdAndActivityTypeOrderByCreatedAtDesc(
            String userId, UserActivityType activityType);

    List<UserActivity> findByUserIdAndTargetTypeAndCreatedAtAfterOrderByCreatedAtDesc(
            String userId, String targetType, Instant since);

    /** Top destinations by activity count (for popularity scoring) */
    long countByTargetIdAndActivityTypeAndCreatedAtAfter(
            String targetId, UserActivityType activityType, Instant since);

    /** Check if user already has an activity of this type for this target */
    boolean existsByUserIdAndTargetIdAndActivityType(
            String userId, String targetId, UserActivityType activityType);

    List<UserActivity> findByCreatedAtAfterOrderByCreatedAtDesc(
            Instant since, org.springframework.data.domain.Pageable pageable);
}
