package com.smarttravel.modules.review.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB document representing a user review for a flight or hotel.
 */
@Document(collection = "reviews")
@CompoundIndexes({
        @CompoundIndex(name = "review_target_idx", def = "{'targetType': 1, 'targetId': 1, 'status': 1, 'createdAt': -1}"),
        @CompoundIndex(name = "review_user_target_idx", def = "{'userId': 1, 'targetId': 1}", unique = true),
        @CompoundIndex(name = "review_user_idx", def = "{'userId': 1, 'createdAt': -1}")
})
public class Review {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String userFullName;

    /** The target entity being reviewed */
    private ReviewTargetType targetType; // FLIGHT or HOTEL

    @Indexed
    private String targetId; // flightId or hotelId

    private String targetName; // Flight number or hotel name

    /** Overall rating 1.0 – 5.0 */
    private double rating;

    /** Specific sub-ratings */
    private double cleanlinessRating;
    private double serviceRating;
    private double valueRating;

    private String title;

    private String body;

    private ReviewStatus status = ReviewStatus.PUBLISHED;

    /** IDs of users who found this review helpful */
    private List<String> helpfulVoters = new ArrayList<>();

    /** IDs of users who flagged this review */
    private List<String> flaggedBy = new ArrayList<>();

    /** Admin moderation notes */
    private String moderationNote;

    private String moderatedBy;
    private Instant moderatedAt;

    /** Booking/experience ID linking review to verified purchase */
    private String bookingId;
    private boolean verifiedPurchase = false;

    /** Uploaded photo public URLs/filenames */
    private List<String> photos = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Review() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Review r = new Review();
        public Builder id(String v) { r.id = v; return this; }
        public Builder userId(String v) { r.userId = v; return this; }
        public Builder userFullName(String v) { r.userFullName = v; return this; }
        public Builder targetType(ReviewTargetType v) { r.targetType = v; return this; }
        public Builder targetId(String v) { r.targetId = v; return this; }
        public Builder targetName(String v) { r.targetName = v; return this; }
        public Builder rating(double v) { r.rating = v; return this; }
        public Builder cleanlinessRating(double v) { r.cleanlinessRating = v; return this; }
        public Builder serviceRating(double v) { r.serviceRating = v; return this; }
        public Builder valueRating(double v) { r.valueRating = v; return this; }
        public Builder title(String v) { r.title = v; return this; }
        public Builder body(String v) { r.body = v; return this; }
        public Builder status(ReviewStatus v) { r.status = v; return this; }
        public Builder helpfulVoters(List<String> v) { r.helpfulVoters = v; return this; }
        public Builder flaggedBy(List<String> v) { r.flaggedBy = v; return this; }
        public Builder moderationNote(String v) { r.moderationNote = v; return this; }
        public Builder bookingId(String v) { r.bookingId = v; return this; }
        public Builder verifiedPurchase(boolean v) { r.verifiedPurchase = v; return this; }
        public Builder photos(List<String> v) { r.photos = v != null ? v : new ArrayList<>(); return this; }
        public Review build() { return r; }
    }

    public int getHelpfulCount() { return helpfulVoters == null ? 0 : helpfulVoters.size(); }
    public int getFlagCount() { return flaggedBy == null ? 0 : flaggedBy.size(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public ReviewTargetType getTargetType() { return targetType; }
    public void setTargetType(ReviewTargetType targetType) { this.targetType = targetType; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    public double getCleanlinessRating() { return cleanlinessRating; }
    public void setCleanlinessRating(double cleanlinessRating) { this.cleanlinessRating = cleanlinessRating; }
    public double getServiceRating() { return serviceRating; }
    public void setServiceRating(double serviceRating) { this.serviceRating = serviceRating; }
    public double getValueRating() { return valueRating; }
    public void setValueRating(double valueRating) { this.valueRating = valueRating; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }
    public List<String> getHelpfulVoters() { return helpfulVoters; }
    public void setHelpfulVoters(List<String> helpfulVoters) { this.helpfulVoters = helpfulVoters; }
    public List<String> getFlaggedBy() { return flaggedBy; }
    public void setFlaggedBy(List<String> flaggedBy) { this.flaggedBy = flaggedBy; }
    public String getModerationNote() { return moderationNote; }
    public void setModerationNote(String moderationNote) { this.moderationNote = moderationNote; }
    public String getModeratedBy() { return moderatedBy; }
    public void setModeratedBy(String moderatedBy) { this.moderatedBy = moderatedBy; }
    public Instant getModeratedAt() { return moderatedAt; }
    public void setModeratedAt(Instant moderatedAt) { this.moderatedAt = moderatedAt; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public boolean isVerifiedPurchase() { return verifiedPurchase; }
    public void setVerifiedPurchase(boolean verifiedPurchase) { this.verifiedPurchase = verifiedPurchase; }
    public List<String> getPhotos() { return photos; }
    public void setPhotos(List<String> photos) { this.photos = photos != null ? photos : new ArrayList<>(); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
