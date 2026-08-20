package com.smarttravel.modules.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * A single recommendation item returned to the frontend.
 * Contains everything needed to render a recommendation card.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationItem {

    private String id;
    private RecommendationItemType type;
    private String targetId;
    private String title;
    private String subtitle;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private String priceLabel;
    private String currency;

    /** Score 0.0 – 100.0 */
    private double score;

    /** Human-readable reason why this was recommended */
    private String reasonCode;
    private String reasonLabel;

    // For flights
    private String fromCity;
    private String toCity;
    private String fromCode;
    private String toCode;
    private String airline;

    // For hotels
    private String city;
    private Integer starRating;
    private Double avgRating;

    public RecommendationItem() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final RecommendationItem r = new RecommendationItem();
        public Builder id(String v) { r.id = v; return this; }
        public Builder type(RecommendationItemType v) { r.type = v; return this; }
        public Builder targetId(String v) { r.targetId = v; return this; }
        public Builder title(String v) { r.title = v; return this; }
        public Builder subtitle(String v) { r.subtitle = v; return this; }
        public Builder description(String v) { r.description = v; return this; }
        public Builder imageUrl(String v) { r.imageUrl = v; return this; }
        public Builder price(BigDecimal v) { r.price = v; return this; }
        public Builder priceLabel(String v) { r.priceLabel = v; return this; }
        public Builder currency(String v) { r.currency = v; return this; }
        public Builder score(double v) { r.score = v; return this; }
        public Builder reasonCode(String v) { r.reasonCode = v; return this; }
        public Builder reasonLabel(String v) { r.reasonLabel = v; return this; }
        public Builder fromCity(String v) { r.fromCity = v; return this; }
        public Builder toCity(String v) { r.toCity = v; return this; }
        public Builder fromCode(String v) { r.fromCode = v; return this; }
        public Builder toCode(String v) { r.toCode = v; return this; }
        public Builder airline(String v) { r.airline = v; return this; }
        public Builder city(String v) { r.city = v; return this; }
        public Builder starRating(Integer v) { r.starRating = v; return this; }
        public Builder avgRating(Double v) { r.avgRating = v; return this; }
        public RecommendationItem build() { return r; }
    }

    public String getId() { return id; }
    public RecommendationItemType getType() { return type; }
    public String getTargetId() { return targetId; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public BigDecimal getPrice() { return price; }
    public String getPriceLabel() { return priceLabel; }
    public String getCurrency() { return currency; }
    public double getScore() { return score; }
    public String getReasonCode() { return reasonCode; }
    public String getReasonLabel() { return reasonLabel; }
    public String getFromCity() { return fromCity; }
    public String getToCity() { return toCity; }
    public String getFromCode() { return fromCode; }
    public String getToCode() { return toCode; }
    public String getAirline() { return airline; }
    public String getCity() { return city; }
    public Integer getStarRating() { return starRating; }
    public Double getAvgRating() { return avgRating; }
}
