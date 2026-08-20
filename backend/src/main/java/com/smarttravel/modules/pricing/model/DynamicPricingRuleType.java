package com.smarttravel.modules.pricing.model;

/**
 * Enumeration of dynamic pricing rule types.
 */
public enum DynamicPricingRuleType {
    /** Demand-based pricing based on cabin occupancy percentage */
    DEMAND,
    /** Holiday surcharge for specific holiday date ranges */
    HOLIDAY,
    /** Seasonal pricing (summer/winter/festival seasons) */
    SEASONAL,
    /** Peak time surcharge for peak booking hours/days */
    PEAK_TIME,
    /** Low-demand discount to stimulate bookings */
    LOW_DEMAND
}
