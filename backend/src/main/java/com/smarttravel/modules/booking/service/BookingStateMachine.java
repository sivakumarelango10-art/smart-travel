package com.smarttravel.modules.booking.service;

import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.modules.booking.model.BookingStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State machine enforcing legal lifecycle transitions for flight bookings.
 */
@Component
public class BookingStateMachine {

    private static final Map<BookingStatus, Set<BookingStatus>> VALID_TRANSITIONS = new EnumMap<>(BookingStatus.class);

    static {
        // PENDING transitions
        VALID_TRANSITIONS.put(BookingStatus.PENDING, EnumSet.of(
                BookingStatus.CONFIRMED,
                BookingStatus.CANCELLED,
                BookingStatus.EXPIRED
        ));

        // CONFIRMED transitions
        VALID_TRANSITIONS.put(BookingStatus.CONFIRMED, EnumSet.of(
                BookingStatus.CANCELLED
        ));

        // Terminal states (no further transitions permitted)
        VALID_TRANSITIONS.put(BookingStatus.CANCELLED, Collections.emptySet());
        VALID_TRANSITIONS.put(BookingStatus.EXPIRED, Collections.emptySet());
    }

    /**
     * Checks whether transition from current status to next status is permitted.
     */
    public boolean isValidTransition(BookingStatus current, BookingStatus next) {
        if (current == null || next == null) {
            return false;
        }
        Set<BookingStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Collections.emptySet());
        return allowed.contains(next);
    }

    /**
     * Validates status transition and throws InvalidStateTransitionException (HTTP 409) if illegal.
     */
    public void validateTransition(BookingStatus current, BookingStatus next) {
        if (!isValidTransition(current, next)) {
            throw new InvalidStateTransitionException(
                    "Invalid booking status transition from " + current + " to " + next
            );
        }
    }
}
