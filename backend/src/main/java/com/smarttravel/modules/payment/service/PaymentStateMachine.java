package com.smarttravel.modules.payment.service;

import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.modules.payment.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * State machine enforcing legal status transitions across the payment lifecycle.
 */
@Component
public class PaymentStateMachine {

    private static final Map<PaymentStatus, Set<PaymentStatus>> VALID_TRANSITIONS = new EnumMap<>(PaymentStatus.class);

    static {
        // CREATED transitions
        VALID_TRANSITIONS.put(PaymentStatus.CREATED, EnumSet.of(
                PaymentStatus.ORDER_CREATED,
                PaymentStatus.FAILED
        ));

        // ORDER_CREATED transitions
        VALID_TRANSITIONS.put(PaymentStatus.ORDER_CREATED, EnumSet.of(
                PaymentStatus.PENDING,
                PaymentStatus.VERIFIED,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED
        ));

        // PENDING transitions
        VALID_TRANSITIONS.put(PaymentStatus.PENDING, EnumSet.of(
                PaymentStatus.VERIFIED,
                PaymentStatus.FAILED,
                PaymentStatus.CANCELLED,
                PaymentStatus.EXPIRED
        ));

        // Terminal states (no further transitions permitted)
        VALID_TRANSITIONS.put(PaymentStatus.VERIFIED, Collections.emptySet());
        VALID_TRANSITIONS.put(PaymentStatus.FAILED, Collections.emptySet());
        VALID_TRANSITIONS.put(PaymentStatus.CANCELLED, Collections.emptySet());
        VALID_TRANSITIONS.put(PaymentStatus.EXPIRED, Collections.emptySet());
    }

    /**
     * Checks whether transition from current status to next status is permitted.
     */
    public boolean isValidTransition(PaymentStatus current, PaymentStatus next) {
        if (current == null || next == null) {
            return false;
        }
        Set<PaymentStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Collections.emptySet());
        return allowed.contains(next);
    }

    /**
     * Validates status transition and throws InvalidStateTransitionException (HTTP 409) if illegal.
     */
    public void validateTransition(PaymentStatus current, PaymentStatus next) {
        if (!isValidTransition(current, next)) {
            throw new InvalidStateTransitionException(
                    "Invalid payment status transition from " + current + " to " + next
            );
        }
    }
}
