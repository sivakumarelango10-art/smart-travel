package com.smarttravel.modules.recommendation.model;

/**
 * Types of user activities tracked for recommendation scoring.
 * Higher-weight activities signal stronger preference.
 */
public enum UserActivityType {
    /** User searched for flights/hotels to/from this destination */
    SEARCH(1),
    /** User viewed a flight or hotel detail page */
    VIEW(2),
    /** User viewed a flight for >30 seconds (strong interest signal) */
    EXTENDED_VIEW(3),
    /** User booked a flight or hotel */
    BOOK(10),
    /** User added a flight to their tracking list */
    TRACK(4),
    /** User left a review */
    REVIEW(5),
    /** User searched for hotels in a city */
    SEARCH_HOTEL(1),
    /** User viewed hotel details */
    VIEW_HOTEL(2);

    private final int weight;

    UserActivityType(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
