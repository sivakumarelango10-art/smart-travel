package com.smarttravel.modules.recommendation.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document storing user feedback (helpful, not relevant, dismiss) on recommendations.
 * Used by the scoring algorithm to refine future suggestions and filter out dismissed items.
 */
@Document(collection = "recommendation_feedback")
@CompoundIndexes({
        @CompoundIndex(name = "feedback_user_target_idx", def = "{'userId': 1, 'targetId': 1}"),
        @CompoundIndex(name = "feedback_user_type_idx", def = "{'userId': 1, 'feedbackType': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "feedback_target_idx", def = "{'targetId': 1, 'feedbackType': 1}")
})
public class RecommendationFeedback {

    @Id
    private String id;

    @Indexed
    private String userId;

    /** Target ID of the recommended item (flight ID, hotel ID, destination name) */
    @Indexed
    private String targetId;

    /** FLIGHT, HOTEL, or DESTINATION */
    private String targetType;

    /** HELPFUL, NOT_RELEVANT, or DISMISS */
    private RecommendationFeedbackType feedbackType;

    /** Reason code associated with the recommendation (e.g. BASED_ON_HISTORY, COLLABORATIVE) */
    private String reasonCode;

    /** Inferred category (e.g. BEACH, LUXURY, MOUNTAIN) */
    private String category;

    @CreatedDate
    private Instant createdAt;

    public RecommendationFeedback() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RecommendationFeedback r = new RecommendationFeedback();
        public Builder id(String v) { r.id = v; return this; }
        public Builder userId(String v) { r.userId = v; return this; }
        public Builder targetId(String v) { r.targetId = v; return this; }
        public Builder targetType(String v) { r.targetType = v; return this; }
        public Builder feedbackType(RecommendationFeedbackType v) { r.feedbackType = v; return this; }
        public Builder reasonCode(String v) { r.reasonCode = v; return this; }
        public Builder category(String v) { r.category = v; return this; }
        public Builder createdAt(Instant v) { r.createdAt = v; return this; }
        public RecommendationFeedback build() { return r; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public RecommendationFeedbackType getFeedbackType() { return feedbackType; }
    public void setFeedbackType(RecommendationFeedbackType feedbackType) { this.feedbackType = feedbackType; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
