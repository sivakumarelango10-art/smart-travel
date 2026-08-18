package com.smarttravel.modules.flight.service;

import com.smarttravel.modules.flight.dto.CabinSelectionResponse;
import com.smarttravel.modules.flight.dto.FareBreakdownDto;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Service for deterministic itemized fare calculations, multi-passenger scaling, and fallback inventory generation.
 */
public interface FareCalculationService {

    /**
     * Calculates the itemized fare breakdown for a single passenger in the specified cabin tier.
     */
    FareBreakdownDto calculateSinglePassengerFare(CabinInventory inventory);

    /**
     * Calculates the total itemized fare breakdown scaled for the given number of passengers.
     */
    FareBreakdownDto calculateTotalFare(CabinInventory inventory, int passengerCount);

    /**
     * Constructs the CabinSelectionResponse containing single and total fare calculations.
     */
    CabinSelectionResponse buildCabinSelectionResponse(CabinInventory inventory, int passengerCount);

    /**
     * Calculates the total itemized fare breakdown for a specific cabin class and base price.
     */
    FareBreakdownDto calculateFare(BigDecimal basePrice, CabinClass cabinClass, int passengerCount);

    /**
     * Generates default realistic CabinInventory instances for legacy flights lacking explicit per-cabin inventories.
     */
    List<CabinInventory> generateDefaultCabinInventories(BigDecimal basePrice, int totalSeats, int availableSeats, Set<CabinClass> cabinClasses);
}
