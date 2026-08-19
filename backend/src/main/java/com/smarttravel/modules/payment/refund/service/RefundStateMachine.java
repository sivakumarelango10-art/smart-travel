package com.smarttravel.modules.payment.refund.service;

import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State machine enforcing legal status transitions across the refund lifecycle.
 */
@Component
public class RefundStateMachine {

    private static final Map<RefundStatus, Set<RefundStatus>> VALID_TRANSITIONS = new EnumMap<>(RefundStatus.class);

    static {
        // REQUESTED transitions
        VALID_TRANSITIONS.put(RefundStatus.REQUESTED, EnumSet.of(
                RefundStatus.PROCESSING,
                RefundStatus.CANCELLED,
                RefundStatus.FAILED
        ));

        // PROCESSING transitions
        VALID_TRANSITIONS.put(RefundStatus.PROCESSING, EnumSet.of(
                RefundStatus.COMPLETED,
                RefundStatus.FAILED
        ));

        // Terminal states
        VALID_TRANSITIONS.put(RefundStatus.COMPLETED, Collections.emptySet());
        VALID_TRANSITIONS.put(RefundStatus.FAILED, Collections.emptySet());
        VALID_TRANSITIONS.put(RefundStatus.CANCELLED, Collections.emptySet());
    }

    /**
     * Checks whether transition from current status to next status is permitted.
     */
    public boolean isValidTransition(RefundStatus current, RefundStatus next) {
        if (current == null || next == null) {
            return false;
        }
        Set<RefundStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Collections.emptySet());
        return allowed.contains(next);
    }

    /**
     * Validates status transition and throws InvalidStateTransitionException (HTTP 409) if illegal.
     */
    public void validateTransition(RefundStatus current, RefundStatus next) {
        if (!isValidTransition(current, next)) {
            throw new InvalidStateTransitionException(
                    "Invalid refund status transition from " + current + " to " + next
            );
        }
    }
}
