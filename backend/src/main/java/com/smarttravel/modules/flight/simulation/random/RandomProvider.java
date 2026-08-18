package com.smarttravel.modules.flight.simulation.random;

/**
 * Abstraction for randomness in flight simulations to enable deterministic testing.
 */
public interface RandomProvider {

    /**
     * Returns a pseudorandom, uniformly distributed double value between 0.0 (inclusive) and 1.0 (exclusive).
     */
    double nextDouble();

    /**
     * Returns a pseudorandom integer between min (inclusive) and max (inclusive).
     */
    int nextInt(int min, int max);

    /**
     * Returns a realistic reason for a flight delay.
     */
    String getRandomDelayReason();
}
