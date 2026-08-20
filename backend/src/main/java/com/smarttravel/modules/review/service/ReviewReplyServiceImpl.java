package com.smarttravel.modules.review.service;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewReply;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.repository.ReviewReplyRepository;
import com.smarttravel.modules.review.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private static final Logger log = LoggerFactory.getLogger(ReviewReplyServiceImpl.class);

    private final ReviewReplyRepository replyRepository;
    private final ReviewRepository reviewRepository;

    public ReviewReplyServiceImpl(ReviewReplyRepository replyRepository,
                                  ReviewRepository reviewRepository) {
        this.replyRepository = replyRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public ReviewReply createReply(String reviewId, String userId, String userName, String content) {
        if (content == null || content.trim().length() < 2) {
            throw new BadRequestException("Reply content must be at least 2 characters");
        }
        if (content.length() > 1000) {
            throw new BadRequestException("Reply content cannot exceed 1000 characters");
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (review.getStatus() == ReviewStatus.REMOVED) {
            throw new BadRequestException("Cannot reply to a removed review");
        }

        ReviewReply reply = ReviewReply.builder()
                .reviewId(reviewId)
                .userId(userId)
                .userName(userName != null && !userName.isBlank() ? userName : "Traveler")
                .content(content.trim())
                .status(ReviewStatus.PUBLISHED)
                .build();

        ReviewReply saved = replyRepository.save(reply);
        log.info("User '{}' added reply '{}' to review '{}'", userId, saved.getId(), reviewId);
        return saved;
    }

    @Override
    public List<ReviewReply> getRepliesForReview(String reviewId) {
        return replyRepository.findByReviewIdAndStatusOrderByCreatedAtAsc(reviewId, ReviewStatus.PUBLISHED);
    }

    @Override
    public ReviewReply updateReply(String replyId, String userId, String content, boolean isAdmin) {
        ReviewReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewReply", "id", replyId));

        if (!isAdmin && !reply.getUserId().equals(userId)) {
            throw new BadRequestException("You can only edit your own replies");
        }

        if (content == null || content.trim().length() < 2) {
            throw new BadRequestException("Reply content must be at least 2 characters");
        }
        if (content.length() > 1000) {
            throw new BadRequestException("Reply content cannot exceed 1000 characters");
        }

        reply.setContent(content.trim());
        reply.setUpdatedAt(Instant.now());
        log.info("Reply '{}' updated by user '{}'", replyId, userId);
        return replyRepository.save(reply);
    }

    @Override
    public void deleteReply(String replyId, String userId, boolean isAdmin) {
        ReviewReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ResourceNotFoundException("ReviewReply", "id", replyId));

        if (!isAdmin && !reply.getUserId().equals(userId)) {
            throw new BadRequestException("You can only delete your own replies");
        }

        replyRepository.deleteById(replyId);
        log.info("Reply '{}' deleted by user '{}'", replyId, userId);
    }
}
