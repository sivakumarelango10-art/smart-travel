package com.smarttravel.modules.pricing.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.pricing.model.PriceFreeze;
import com.smarttravel.modules.pricing.service.PriceFreezeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for price freeze management.
 */
@RestController
@RequestMapping("/v1/price-freezes")
@Tag(name = "Price Freeze", description = "Lock fares before committing to booking")
public class PriceFreezeController {

    private final PriceFreezeService priceFreezeService;

    public PriceFreezeController(PriceFreezeService priceFreezeService) {
        this.priceFreezeService = priceFreezeService;
    }

    @Operation(summary = "Create a price freeze for a flight cabin")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PriceFreeze>> createFreeze(
            @Valid @RequestBody CreateFreezeRequest request,
            Authentication authentication) {
        String userId = authentication.getName();
        PriceFreeze freeze = priceFreezeService.createFreeze(
                userId, request.flightId(), request.cabinClass(), request.passengerCount());
        return ResponseEntity.ok(ApiResponse.success("Price freeze created", freeze));
    }

    @Operation(summary = "Get all price freezes for current user")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PriceFreeze>>> getUserFreezes(Authentication authentication) {
        String userId = authentication.getName();
        List<PriceFreeze> freezes = priceFreezeService.getUserFreezes(userId);
        return ResponseEntity.ok(ApiResponse.success("Price freezes retrieved", freezes));
    }

    @Operation(summary = "Get a specific price freeze by ID")
    @GetMapping("/{freezeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PriceFreeze>> getFreeze(
            @PathVariable String freezeId,
            Authentication authentication) {
        String userId = authentication.getName();
        PriceFreeze freeze = priceFreezeService.getFreezeById(freezeId, userId);
        return ResponseEntity.ok(ApiResponse.success("Price freeze retrieved", freeze));
    }

    @Operation(summary = "Cancel an active price freeze")
    @PostMapping("/{freezeId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PriceFreeze>> cancelFreeze(
            @PathVariable String freezeId,
            Authentication authentication) {
        String userId = authentication.getName();
        PriceFreeze freeze = priceFreezeService.cancelFreeze(freezeId, userId);
        return ResponseEntity.ok(ApiResponse.success("Price freeze cancelled", freeze));
    }

    // ── Request Records ───────────────────────────────────────────────────────

    public record CreateFreezeRequest(
            @NotBlank(message = "flightId is required") String flightId,
            @NotNull(message = "cabinClass is required") CabinClass cabinClass,
            @Min(value = 1, message = "Minimum 1 passenger required") int passengerCount
    ) {}
}
