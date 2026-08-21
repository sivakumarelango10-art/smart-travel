package com.smarttravel.modules.flight.provider.aviationstack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Operating airline details from Aviationstack.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackAirline {

    @JsonProperty("name")
    private String name;

    @JsonProperty("iata")
    private String iata;

    @JsonProperty("icao")
    private String icao;

    public AviationstackAirline() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
