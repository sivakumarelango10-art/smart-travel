package com.smarttravel.modules.review.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document representing a user or host reply to a review.
 */
@Document(collection = "review_replies")
@CompoundIndexes({
        @CompoundIndex(name = "reply_review_status_idx", def = "{'reviewId': 1, 'status': 1, 'createdAt': 1}"),
        @CompoundIndex(name = "reply_user_idx", def = "{'userId': 1, 'createdAt': -1}")
})
public class ReviewReply {

    @Id
    private String id;

    @Indexed
    private String reviewId;

    @Indexed
    private String userId;

    private String userName;

    private String content;

    private ReviewStatus status = ReviewStatus.PUBLISHED;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public ReviewReply() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ReviewReply r = new ReviewReply();
        public Builder id(String v) { r.id = v; return this; }
        public Builder reviewId(String v) { r.reviewId = v; return this; }
        public Builder userId(String v) { r.userId = v; return this; }
        public Builder userName(String v) { r.userName = v; return this; }
        public Builder content(String v) { r.content = v; return this; }
        public Builder status(ReviewStatus v) { r.status = v; return this; }
        public ReviewReply build() { return r; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReviewId() { return reviewId; }
    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
