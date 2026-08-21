package com.smarttravel.modules.flight.provider.aviationstack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Real-time GPS and telemetry data from Aviationstack.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackLive {

    @JsonProperty("updated")
    private String updated;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("altitude")
    private Double altitude;

    @JsonProperty("direction")
    private Double direction;

    @JsonProperty("speed_horizontal")
    private Double speedHorizontal;

    @JsonProperty("speed_vertical")
    private Double speedVertical;

    @JsonProperty("is_ground")
    private Boolean isGround;

    public AviationstackLive() {
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    public Double getDirection() {
        return direction;
    }

    public void setDirection(Double direction) {
        this.direction = direction;
    }

    public Double getSpeedHorizontal() {
        return speedHorizontal;
    }

    public void setSpeedHorizontal(Double speedHorizontal) {
        this.speedHorizontal = speedHorizontal;
    }

    public Double getSpeedVertical() {
        return speedVertical;
    }

    public void setSpeedVertical(Double speedVertical) {
        this.speedVertical = speedVertical;
    }

    public Boolean getIsGround() {
        return isGround;
    }

    public void setIsGround(Boolean isGround) {
        this.isGround = isGround;
    }
}
