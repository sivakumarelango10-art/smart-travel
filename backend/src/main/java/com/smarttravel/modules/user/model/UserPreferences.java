package com.smarttravel.modules.user.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable user travel preferences.
 */
public class UserPreferences {

    private String preferredSeatType; // e.g. WINDOW, AISLE, EXTRA_LEGROOM
    private String preferredRoomType; // e.g. DELUXE, SUITE, STANDARD
    private List<String> favoriteDestinations = new ArrayList<>();
    private String dietaryPreference; // e.g. VEGETARIAN, NON_VEG, VEGAN

    public UserPreferences() {
    }

    public UserPreferences(String preferredSeatType, String preferredRoomType, List<String> favoriteDestinations, String dietaryPreference) {
        this.preferredSeatType = preferredSeatType;
        this.preferredRoomType = preferredRoomType;
        this.favoriteDestinations = favoriteDestinations != null ? favoriteDestinations : new ArrayList<>();
        this.dietaryPreference = dietaryPreference;
    }

    public String getPreferredSeatType() {
        return preferredSeatType;
    }

    public void setPreferredSeatType(String preferredSeatType) {
        this.preferredSeatType = preferredSeatType;
    }

    public String getPreferredRoomType() {
        return preferredRoomType;
    }

    public void setPreferredRoomType(String preferredRoomType) {
        this.preferredRoomType = preferredRoomType;
    }

    public List<String> getFavoriteDestinations() {
        return favoriteDestinations;
    }

    public void setFavoriteDestinations(List<String> favoriteDestinations) {
        this.favoriteDestinations = favoriteDestinations;
    }

    public String getDietaryPreference() {
        return dietaryPreference;
    }

    public void setDietaryPreference(String dietaryPreference) {
        this.dietaryPreference = dietaryPreference;
    }
}
