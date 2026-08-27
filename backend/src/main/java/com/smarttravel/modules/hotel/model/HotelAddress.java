package com.smarttravel.modules.hotel.model;

/**
 * Hotel address details with geographical coordinates.
 */
public class HotelAddress {
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String postalCode;
    private String country = "India";
    private Double latitude;
    private Double longitude;

    public HotelAddress() {}

    public HotelAddress(String line1, String line2, String city, String state, String postalCode, String country) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country != null ? country : "India";
    }

    public HotelAddress(String line1, String line2, String city, String state, String postalCode, String country, Double latitude, Double longitude) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country != null ? country : "India";
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final HotelAddress r = new HotelAddress();
        public Builder line1(String v) { r.line1 = v; return this; }
        public Builder line2(String v) { r.line2 = v; return this; }
        public Builder city(String v) { r.city = v; return this; }
        public Builder state(String v) { r.state = v; return this; }
        public Builder postalCode(String v) { r.postalCode = v; return this; }
        public Builder country(String v) { r.country = v; return this; }
        public Builder latitude(Double v) { r.latitude = v; return this; }
        public Builder longitude(Double v) { r.longitude = v; return this; }
        public HotelAddress build() { return r; }
    }

    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }
    public String getLine2() { return line2; }
    public void setLine2(String line2) { this.line2 = line2; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
