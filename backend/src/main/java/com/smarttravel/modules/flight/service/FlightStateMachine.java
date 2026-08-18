package com.smarttravel.modules.flight.service;

import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.modules.flight.model.FlightStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State machine enforcing legal operational flight status transitions.
 */
@Component
public class FlightStateMachine {

    private static final Map<FlightStatus, Set<FlightStatus>> VALID_TRANSITIONS = new EnumMap<>(FlightStatus.class);

    static {
        // SCHEDULED transitions
        VALID_TRANSITIONS.put(FlightStatus.SCHEDULED, EnumSet.of(
                FlightStatus.BOARDING,
                FlightStatus.DELAYED,
                FlightStatus.CANCELLED
        ));

        // BOARDING transitions
        VALID_TRANSITIONS.put(FlightStatus.BOARDING, EnumSet.of(
                FlightStatus.ON_TIME,
                FlightStatus.DELAYED
        ));

        // ON_TIME transitions
        VALID_TRANSITIONS.put(FlightStatus.ON_TIME, EnumSet.of(
                FlightStatus.DEPARTED,
                FlightStatus.DELAYED
        ));

        // DELAYED transitions (allows reassessment to BOARDING, direct DEPARTED, CANCELLED, or updated DELAYED)
        VALID_TRANSITIONS.put(FlightStatus.DELAYED, EnumSet.of(
                FlightStatus.BOARDING,
                FlightStatus.DEPARTED,
                FlightStatus.CANCELLED,
                FlightStatus.DELAYED
        ));

        // DEPARTED transitions
        VALID_TRANSITIONS.put(FlightStatus.DEPARTED, EnumSet.of(
                FlightStatus.ARRIVED,
                FlightStatus.DIVERTED
        ));

        // Terminal states (no further transitions allowed)
        VALID_TRANSITIONS.put(FlightStatus.ARRIVED, Collections.emptySet());
        VALID_TRANSITIONS.put(FlightStatus.CANCELLED, Collections.emptySet());
        VALID_TRANSITIONS.put(FlightStatus.DIVERTED, Collections.emptySet());
    }

    /**
     * Checks whether transition from current status to next status is permitted.
     */
    public boolean isValidTransition(FlightStatus current, FlightStatus next) {
        if (current == null || next == null) {
            return false;
        }
        Set<FlightStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Collections.emptySet());
        return allowed.contains(next);
    }

    /**
     * Validates status transition and throws InvalidStateTransitionException if illegal.
     */
    public void validateTransition(FlightStatus current, FlightStatus next) {
        if (!isValidTransition(current, next)) {
            throw new InvalidStateTransitionException(
                    "Invalid flight status transition from " + current + " to " + next
            );
        }
    }
}
