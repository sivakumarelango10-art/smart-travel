package com.smarttravel.modules.flight.provider.aviationstack.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

/**
 * Top-level response container for Aviationstack API responses.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AviationstackFlightResponse {

    @JsonProperty("pagination")
    private AviationstackPagination pagination;

    @JsonProperty("data")
    private List<AviationstackFlightItem> data;

    @JsonProperty("error")
    private AviationstackError error;

    public AviationstackFlightResponse() {
    }

    public AviationstackPagination getPagination() {
        return pagination;
    }

    public void setPagination(AviationstackPagination pagination) {
        this.pagination = pagination;
    }

    public List<AviationstackFlightItem> getData() {
        return data != null ? data : Collections.emptyList();
    }

    public void setData(List<AviationstackFlightItem> data) {
        this.data = data;
    }

    public AviationstackError getError() {
        return error;
    }

    public void setError(AviationstackError error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null && error.getCode() != null;
    }
}
