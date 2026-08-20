package com.smarttravel.modules.user.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable user travel preferences and saved profile details.
 */
public class UserPreferences {

    private String preferredSeatType; // e.g. WINDOW, AISLE, EXTRA_LEGROOM
    private String preferredRoomType; // e.g. DELUXE, SUITE, STANDARD
    private String preferredClass;    // e.g. ECONOMY, PREMIUM_ECONOMY, BUSINESS, FIRST
    private String homeAirport;       // e.g. DEL, BOM, BLR
    private String dietaryPreference; // e.g. VEGETARIAN, NON_VEG, VEGAN
    private List<String> favoriteDestinations = new ArrayList<>();

    // Saved Address Details
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    // Travel Document Details
    private String passportNumber;
    private String nationality;

    public UserPreferences() {
    }

    public UserPreferences(String preferredSeatType, String preferredRoomType, String preferredClass,
                           String homeAirport, String dietaryPreference, List<String> favoriteDestinations,
                           String addressLine1, String addressLine2, String city, String state,
                           String postalCode, String country, String passportNumber, String nationality) {
        this.preferredSeatType = preferredSeatType;
        this.preferredRoomType = preferredRoomType;
        this.preferredClass = preferredClass;
        this.homeAirport = homeAirport;
        this.dietaryPreference = dietaryPreference;
        this.favoriteDestinations = favoriteDestinations != null ? favoriteDestinations : new ArrayList<>();
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
        this.passportNumber = passportNumber;
        this.nationality = nationality;
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

    public String getPreferredClass() {
        return preferredClass;
    }

    public void setPreferredClass(String preferredClass) {
        this.preferredClass = preferredClass;
    }

    public String getHomeAirport() {
        return homeAirport;
    }

    public void setHomeAirport(String homeAirport) {
        this.homeAirport = homeAirport;
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

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
}
