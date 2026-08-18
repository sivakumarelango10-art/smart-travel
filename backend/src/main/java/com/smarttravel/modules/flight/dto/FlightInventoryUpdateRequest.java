package com.smarttravel.modules.flight.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Admin Payload to Update Flight Cabin Inventories and Pricing")
public class FlightInventoryUpdateRequest {

    @Schema(description = "List of cabin class inventory and fare configurations")
    @NotEmpty(message = "At least one cabin inventory configuration is required")
    @Valid
    private List<CabinInventoryDto> cabinInventories;

    public FlightInventoryUpdateRequest() {
    }

    public FlightInventoryUpdateRequest(List<CabinInventoryDto> cabinInventories) {
        this.cabinInventories = cabinInventories;
    }

    public List<CabinInventoryDto> getCabinInventories() {
        return cabinInventories;
    }

    public void setCabinInventories(List<CabinInventoryDto> cabinInventories) {
        this.cabinInventories = cabinInventories;
    }
}
