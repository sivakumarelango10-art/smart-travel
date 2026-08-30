package com.smarttravel.modules.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Human-readable explainability metadata behind why a specific item was recommended.
 * Powers the "Why this recommendation?" interactive tooltip and modal.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationExplanation {

    private String reasonCode;
    private String headline;
    private String details;
    private String category;
    private double confidence;
    private List<String> tags;
    private boolean isAiGenerated;

    public RecommendationExplanation() {}

    public RecommendationExplanation(String reasonCode, String headline, String details,
                                     String category, double confidence, List<String> tags, boolean isAiGenerated) {
        this.reasonCode = reasonCode;
        this.headline = headline;
        this.details = details;
        this.category = category;
        this.confidence = confidence;
        this.tags = tags;
        this.isAiGenerated = isAiGenerated;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RecommendationExplanation r = new RecommendationExplanation();
        public Builder reasonCode(String v) { r.reasonCode = v; return this; }
        public Builder headline(String v) { r.headline = v; return this; }
        public Builder details(String v) { r.details = v; return this; }
        public Builder category(String v) { r.category = v; return this; }
        public Builder confidence(double v) { r.confidence = v; return this; }
        public Builder tags(List<String> v) { r.tags = v; return this; }
        public Builder isAiGenerated(boolean v) { r.isAiGenerated = v; return this; }
        public RecommendationExplanation build() { return r; }
    }

    public String getReasonCode() { return reasonCode; }
    public String getHeadline() { return headline; }
    public String getDetails() { return details; }
    public String getCategory() { return category; }
    public double getConfidence() { return confidence; }
    public List<String> getTags() { return tags; }
    public boolean isAiGenerated() { return isAiGenerated; }
}
