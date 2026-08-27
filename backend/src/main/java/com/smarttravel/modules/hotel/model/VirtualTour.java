package com.smarttravel.modules.hotel.model;

/**
 * 360-degree Virtual Tour and Equirectangular Panorama Metadata.
 */
public class VirtualTour {

    private boolean enabled = true;
    private String panoramaUrl;
    private String thumbnailUrl;
    private String title;
    private String description;
    private String roomCategory;

    public VirtualTour() {}

    public VirtualTour(boolean enabled, String panoramaUrl, String thumbnailUrl, String title, String description, String roomCategory) {
        this.enabled = enabled;
        this.panoramaUrl = panoramaUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.description = description;
        this.roomCategory = roomCategory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final VirtualTour v = new VirtualTour();

        public Builder enabled(boolean enabled) { v.enabled = enabled; return this; }
        public Builder panoramaUrl(String panoramaUrl) { v.panoramaUrl = panoramaUrl; return this; }
        public Builder thumbnailUrl(String thumbnailUrl) { v.thumbnailUrl = thumbnailUrl; return this; }
        public Builder title(String title) { v.title = title; return this; }
        public Builder description(String description) { v.description = description; return this; }
        public Builder roomCategory(String roomCategory) { v.roomCategory = roomCategory; return this; }

        public VirtualTour build() { return v; }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getPanoramaUrl() { return panoramaUrl; }
    public void setPanoramaUrl(String panoramaUrl) { this.panoramaUrl = panoramaUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoomCategory() { return roomCategory; }
    public void setRoomCategory(String roomCategory) { this.roomCategory = roomCategory; }
}
