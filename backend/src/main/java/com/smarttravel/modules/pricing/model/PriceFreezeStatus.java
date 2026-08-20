package com.smarttravel.modules.pricing.model;

/**
 * Lifecycle status of a price freeze.
 */
public enum PriceFreezeStatus {
    /** Freeze is currently valid and can be used for booking */
    ACTIVE,
    /** Freeze was consumed to create a booking */
    USED,
    /** Freeze has passed its expiration time without being used */
    EXPIRED,
    /** User explicitly cancelled the freeze */
    CANCELLED
}
