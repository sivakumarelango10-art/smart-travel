package com.smarttravel.modules.review.service;

import com.smarttravel.modules.review.model.ReviewReply;

import java.util.List;

public interface ReviewReplyService {

    ReviewReply createReply(String reviewId, String userId, String userName, String content);

    List<ReviewReply> getRepliesForReview(String reviewId);

    ReviewReply updateReply(String replyId, String userId, String content, boolean isAdmin);

    void deleteReply(String replyId, String userId, boolean isAdmin);
}
