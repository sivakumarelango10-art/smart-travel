package com.smarttravel.modules.flight.provider.aviationstack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Aircraft physical and model information from Aviationstack.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackAircraft {

    @JsonProperty("registration")
    private String registration;

    @JsonProperty("iata")
    private String iata;

    @JsonProperty("icao")
    private String icao;

    @JsonProperty("icao24")
    private String icao24;

    public AviationstackAircraft() {
    }

    public String getRegistration() {
        return registration;
    }

    public void setRegistration(String registration) {
        this.registration = registration;
    }

    public String getIata() {
        return iata;
    }

    public void setIata(String iata) {
        this.iata = iata;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getIcao24() {
        return icao24;
    }

    public void setIcao24(String icao24) {
        this.icao24 = icao24;
    }
}
