package com.smarttravel.modules.pricing.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown;
import com.smarttravel.modules.pricing.model.DynamicPricingRule;
import com.smarttravel.modules.pricing.model.FlightPriceHistory;
import com.smarttravel.modules.pricing.repository.DynamicPricingRuleRepository;
import com.smarttravel.modules.pricing.repository.FlightPriceHistoryRepository;
import com.smarttravel.modules.pricing.service.DynamicPricingService;
import com.smarttravel.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for flight pricing endpoints.
 * Exposes dynamic price breakdown and price history for frontend charting.
 */
@RestController
@RequestMapping({"/api/v1/pricing", "/v1/pricing", "/api/pricing"})
@Tag(name = "Pricing", description = "Dynamic pricing engine and price history")
public class PricingController {

    private final FlightRepository flightRepository;
    private final DynamicPricingService dynamicPricingService;
    private final FlightPriceHistoryRepository priceHistoryRepository;
    private final DynamicPricingRuleRepository pricingRuleRepository;

    public PricingController(FlightRepository flightRepository,
                             DynamicPricingService dynamicPricingService,
                             FlightPriceHistoryRepository priceHistoryRepository,
                             DynamicPricingRuleRepository pricingRuleRepository) {
        this.flightRepository = flightRepository;
        this.dynamicPricingService = dynamicPricingService;
        this.priceHistoryRepository = priceHistoryRepository;
        this.pricingRuleRepository = pricingRuleRepository;
    }

    @Operation(summary = "Get dynamic price breakdown for a flight cabin")
    @GetMapping("/flights/{flightId}/breakdown")
    public ResponseEntity<ApiResponse<DynamicPriceBreakdown>> getPriceBreakdown(
            @PathVariable String flightId,
            @RequestParam(defaultValue = "ECONOMY") CabinClass cabinClass,
            @RequestParam(defaultValue = "1") int passengers) {

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        CabinInventory inventory = flight.getCabinInventories().stream()
                .filter(ci -> ci.getCabinClass() == cabinClass)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CabinInventory", "cabinClass", cabinClass.name()));

        DynamicPriceBreakdown breakdown = dynamicPricingService.calculateDynamicPrice(flight, inventory, passengers);
        return ResponseEntity.ok(ApiResponse.success("Price breakdown retrieved", breakdown));
    }

    @Operation(summary = "Get price history for a flight (for trend chart)")
    @GetMapping("/flights/{flightId}/history")
    public ResponseEntity<ApiResponse<Page<FlightPriceHistory>>> getPriceHistory(
            @PathVariable String flightId,
            @RequestParam(required = false) CabinClass cabinClass,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "48") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        Page<FlightPriceHistory> history;

        if (cabinClass != null) {
            history = priceHistoryRepository.findByFlightIdAndCabinClassOrderByCapturedAtDesc(
                    flightId, cabinClass, pageable);
        } else {
            history = priceHistoryRepository.findByFlightIdOrderByCapturedAtDesc(flightId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Price history retrieved", history));
    }

    @Operation(summary = "Record a price snapshot for a flight (triggers price capture)")
    @PostMapping("/flights/{flightId}/snapshot")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> captureSnapshot(
            @PathVariable String flightId,
            @RequestParam(defaultValue = "ECONOMY") CabinClass cabinClass) {

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight", "id", flightId));

        CabinInventory inventory = flight.getCabinInventories().stream()
                .filter(ci -> ci.getCabinClass() == cabinClass)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CabinInventory", "cabinClass", cabinClass.name()));

        dynamicPricingService.recordPriceSnapshot(flight, inventory);
        return ResponseEntity.ok(ApiResponse.success("Price snapshot captured"));
    }

    @Operation(summary = "Get all active pricing rules (admin)")
    @GetMapping("/rules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DynamicPricingRule>>> getPricingRules() {
        List<DynamicPricingRule> rules = pricingRuleRepository.findByEnabledTrueOrderByPriorityAsc();
        return ResponseEntity.ok(ApiResponse.success("Pricing rules retrieved", rules));
    }
}
