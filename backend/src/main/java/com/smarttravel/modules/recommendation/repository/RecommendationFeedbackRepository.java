package com.smarttravel.modules.recommendation.repository;

import com.smarttravel.modules.recommendation.model.RecommendationFeedback;
import com.smarttravel.modules.recommendation.model.RecommendationFeedbackType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationFeedbackRepository extends MongoRepository<RecommendationFeedback, String> {

    List<RecommendationFeedback> findByUserId(String userId);

    List<RecommendationFeedback> findByUserIdAndFeedbackType(String userId, RecommendationFeedbackType feedbackType);

    Optional<RecommendationFeedback> findByUserIdAndTargetId(String userId, String targetId);

    void deleteByUserIdAndTargetId(String userId, String targetId);

    long countByUserIdAndFeedbackType(String userId, RecommendationFeedbackType feedbackType);
}
