package com.smarttravel.modules.review.model;

/**
 * Moderation status of a review.
 */
public enum ReviewStatus {
    /** Review published and visible to all users */
    PUBLISHED,
    /** Review flagged by users, pending admin review */
    FLAGGED,
    /** Review hidden from public listings by moderator */
    HIDDEN,
    /** Review removed by admin due to policy violation */
    REMOVED,
    /** Review pending moderation (not yet published) */
    PENDING
}
