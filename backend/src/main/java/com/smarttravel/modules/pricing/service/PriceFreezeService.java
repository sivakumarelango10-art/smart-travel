package com.smarttravel.modules.pricing.service;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown;
import com.smarttravel.modules.pricing.model.PriceFreeze;

import java.util.List;

/**
 * Service for creating and managing price freezes.
 */
public interface PriceFreezeService {

    /**
     * Create a new price freeze for a user on a specific flight cabin.
     */
    PriceFreeze createFreeze(String userId, String flightId, CabinClass cabinClass, int passengerCount);

    /**
     * Get all price freezes for a user.
     */
    List<PriceFreeze> getUserFreezes(String userId);

    /**
     * Get a specific price freeze by ID, validating ownership.
     */
    PriceFreeze getFreezeById(String freezeId, String userId);

    /**
     * Cancel an active price freeze.
     */
    PriceFreeze cancelFreeze(String freezeId, String userId);

    /**
     * Mark a freeze as USED after successful booking.
     */
    PriceFreeze markAsUsed(String freezeId, String bookingId, String userId);

    /**
     * Expire stale ACTIVE freezes (scheduled cleanup).
     */
    void expireStaleFreeze();
}
