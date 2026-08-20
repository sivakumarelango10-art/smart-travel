package com.smarttravel.modules.review.repository;

import com.smarttravel.modules.review.model.ReviewReply;
import com.smarttravel.modules.review.model.ReviewStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReplyRepository extends MongoRepository<ReviewReply, String> {

    List<ReviewReply> findByReviewIdAndStatusOrderByCreatedAtAsc(String reviewId, ReviewStatus status);

    List<ReviewReply> findByReviewIdOrderByCreatedAtAsc(String reviewId);

    void deleteByReviewId(String reviewId);
}
