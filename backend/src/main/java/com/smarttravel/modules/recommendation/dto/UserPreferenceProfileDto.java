package com.smarttravel.modules.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

/**
 * Inferred user travel preference profile synthesized from interactions, bookings, and feedback.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserPreferenceProfileDto {

    private String userId;
    private List<String> topCategories;
    private Map<String, Double> categoryAffinities;
    private List<String> preferredDestinations;
    private List<String> preferredAirlines;
    private String homeAirport;
    private String preferredCabinClass;
    private String inferredTravelStyle; // e.g., "BEACH & LUXURY", "BUSINESS", "ADVENTURE & NATURE"
    private int totalActivities;
    private long helpfulFeedbackCount;
    private double confidenceScore;

    public UserPreferenceProfileDto() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final UserPreferenceProfileDto r = new UserPreferenceProfileDto();
        public Builder userId(String v) { r.userId = v; return this; }
        public Builder topCategories(List<String> v) { r.topCategories = v; return this; }
        public Builder categoryAffinities(Map<String, Double> v) { r.categoryAffinities = v; return this; }
        public Builder preferredDestinations(List<String> v) { r.preferredDestinations = v; return this; }
        public Builder preferredAirlines(List<String> v) { r.preferredAirlines = v; return this; }
        public Builder homeAirport(String v) { r.homeAirport = v; return this; }
        public Builder preferredCabinClass(String v) { r.preferredCabinClass = v; return this; }
        public Builder inferredTravelStyle(String v) { r.inferredTravelStyle = v; return this; }
        public Builder totalActivities(int v) { r.totalActivities = v; return this; }
        public Builder helpfulFeedbackCount(long v) { r.helpfulFeedbackCount = v; return this; }
        public Builder confidenceScore(double v) { r.confidenceScore = v; return this; }
        public UserPreferenceProfileDto build() { return r; }
    }

    public String getUserId() { return userId; }
    public List<String> getTopCategories() { return topCategories; }
    public Map<String, Double> getCategoryAffinities() { return categoryAffinities; }
    public List<String> getPreferredDestinations() { return preferredDestinations; }
    public List<String> getPreferredAirlines() { return preferredAirlines; }
    public String getHomeAirport() { return homeAirport; }
    public String getPreferredCabinClass() { return preferredCabinClass; }
    public String getInferredTravelStyle() { return inferredTravelStyle; }
    public int getTotalActivities() { return totalActivities; }
    public long getHelpfulFeedbackCount() { return helpfulFeedbackCount; }
    public double getConfidenceScore() { return confidenceScore; }
}
