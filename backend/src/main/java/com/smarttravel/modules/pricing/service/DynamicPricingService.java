package com.smarttravel.modules.pricing.service;

import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown;
import com.smarttravel.modules.pricing.model.FlightPriceHistory;

/**
 * Service for calculating transparent dynamic pricing with demand, seasonal,
 * and holiday adjustments on top of the base cabin fare.
 */
public interface DynamicPricingService {

    /**
     * Calculate the full dynamic price breakdown for a cabin inventory on a specific flight.
     *
     * @param flight      The flight entity
     * @param inventory   The cabin inventory (base price already includes cabin multiplier)
     * @param passengers  Number of passengers
     * @return Full transparent pricing breakdown
     */
    DynamicPriceBreakdown calculateDynamicPrice(Flight flight, CabinInventory inventory, int passengers);

    /**
     * Record a price history snapshot if enough time has elapsed since last snapshot.
     */
    void recordPriceSnapshot(Flight flight, CabinInventory inventory);

    /**
     * Build a FlightPriceHistory record from the current breakdown.
     */
    FlightPriceHistory toHistoryRecord(DynamicPriceBreakdown breakdown, String flightNumber);

    /**
     * Broadcasts a real-time price change event to /topic/pricing/{flightId}.
     */
    void publishPriceUpdate(Flight flight, CabinInventory inventory, java.math.BigDecimal oldPrice);
}

