package com.smarttravel.modules.flight.simulation.random;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Default production implementation of RandomProvider using ThreadLocalRandom.
 */
@Component
public class DefaultRandomProvider implements RandomProvider {

    private static final List<String> DELAY_REASONS = List.of(
            "Weather conditions at departure airport",
            "Air traffic control congestion",
            "Technical inspection and maintenance",
            "Late inbound aircraft turnaround",
            "Ground handling and baggage loading delay",
            "Operational airspace restrictions"
    );

    @Override
    public double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    @Override
    public int nextInt(int min, int max) {
        if (min >= max) {
            return min;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    @Override
    public String getRandomDelayReason() {
        int idx = ThreadLocalRandom.current().nextInt(DELAY_REASONS.size());
        return DELAY_REASONS.get(idx);
    }
}
