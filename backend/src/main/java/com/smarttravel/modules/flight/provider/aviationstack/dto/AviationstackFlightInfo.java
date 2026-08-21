package com.smarttravel.modules.flight.provider.aviationstack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Flight number and code identifiers from Aviationstack.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackFlightInfo {

    @JsonProperty("number")
    private String number;

    @JsonProperty("iata")
    private String iata;

    @JsonProperty("icao")
    private String icao;

    @JsonProperty("codeshared")
    private Object codeshared;

    public AviationstackFlightInfo() {
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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

    public Object getCodeshared() {
        return codeshared;
    }

    public void setCodeshared(Object codeshared) {
        this.codeshared = codeshared;
    }
}
