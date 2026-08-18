package com.smarttravel.modules.payment.service;

import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.modules.payment.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PaymentStateMachineTest {

    private PaymentStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new PaymentStateMachine();
    }

    @ParameterizedTest(name = "Valid transition from {0} to {1}")
    @CsvSource({
            "CREATED, ORDER_CREATED",
            "CREATED, FAILED",
            "ORDER_CREATED, PENDING",
            "ORDER_CREATED, VERIFIED",
            "ORDER_CREATED, FAILED",
            "ORDER_CREATED, CANCELLED",
            "ORDER_CREATED, EXPIRED",
            "PENDING, VERIFIED",
            "PENDING, FAILED",
            "PENDING, CANCELLED",
            "PENDING, EXPIRED"
    })
    @DisplayName("Permitted state transitions should return true and validate cleanly")
    void testValidTransitions(PaymentStatus from, PaymentStatus to) {
        assertThat(stateMachine.isValidTransition(from, to)).isTrue();
        assertDoesNotThrow(() -> stateMachine.validateTransition(from, to));
    }

    @ParameterizedTest(name = "Terminal status {0} cannot transition to {1}")
    @CsvSource({
            "VERIFIED, CREATED",
            "VERIFIED, ORDER_CREATED",
            "VERIFIED, PENDING",
            "VERIFIED, FAILED",
            "VERIFIED, CANCELLED",
            "VERIFIED, EXPIRED",
            "FAILED, CREATED",
            "FAILED, ORDER_CREATED",
            "FAILED, PENDING",
            "FAILED, VERIFIED",
            "CANCELLED, CREATED",
            "CANCELLED, ORDER_CREATED",
            "CANCELLED, PENDING",
            "CANCELLED, VERIFIED",
            "EXPIRED, CREATED",
            "EXPIRED, ORDER_CREATED",
            "EXPIRED, PENDING",
            "EXPIRED, VERIFIED"
    })
    @DisplayName("Terminal states should reject all further transitions with InvalidStateTransitionException")
    void testTerminalStatesCannotTransition(PaymentStatus terminal, PaymentStatus target) {
        assertThat(stateMachine.isValidTransition(terminal, target)).isFalse();
        assertThatThrownBy(() -> stateMachine.validateTransition(terminal, target))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Invalid payment status transition from " + terminal + " to " + target);
    }

    @ParameterizedTest
    @EnumSource(PaymentStatus.class)
    @DisplayName("Self-transitions should be invalid for all states")
    void testSelfTransitionsAreInvalid(PaymentStatus status) {
        assertThat(stateMachine.isValidTransition(status, status)).isFalse();
    }

    @Test
    @DisplayName("Null checks should return false without throwing NullPointerException")
    void testNullChecks() {
        assertThat(stateMachine.isValidTransition(null, PaymentStatus.ORDER_CREATED)).isFalse();
        assertThat(stateMachine.isValidTransition(PaymentStatus.CREATED, null)).isFalse();
        assertThat(stateMachine.isValidTransition(null, null)).isFalse();
    }
}
