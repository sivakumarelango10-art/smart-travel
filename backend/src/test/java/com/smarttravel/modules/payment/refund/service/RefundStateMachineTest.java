package com.smarttravel.modules.payment.refund.service;

import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.modules.payment.refund.model.RefundStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefundStateMachineTest {

    private RefundStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new RefundStateMachine();
    }

    @Test
    @DisplayName("Should permit valid refund lifecycle transitions")
    void shouldPermitValidTransitions() {
        assertThat(stateMachine.isValidTransition(RefundStatus.REQUESTED, RefundStatus.PROCESSING)).isTrue();
        assertThat(stateMachine.isValidTransition(RefundStatus.REQUESTED, RefundStatus.CANCELLED)).isTrue();
        assertThat(stateMachine.isValidTransition(RefundStatus.PROCESSING, RefundStatus.COMPLETED)).isTrue();
        assertThat(stateMachine.isValidTransition(RefundStatus.PROCESSING, RefundStatus.FAILED)).isTrue();
    }

    @Test
    @DisplayName("Should reject illegal transitions from terminal states")
    void shouldRejectTransitionsFromTerminalStates() {
        assertThat(stateMachine.isValidTransition(RefundStatus.COMPLETED, RefundStatus.PROCESSING)).isFalse();
        assertThat(stateMachine.isValidTransition(RefundStatus.FAILED, RefundStatus.COMPLETED)).isFalse();
        assertThat(stateMachine.isValidTransition(RefundStatus.CANCELLED, RefundStatus.PROCESSING)).isFalse();

        assertThatThrownBy(() -> stateMachine.validateTransition(RefundStatus.COMPLETED, RefundStatus.PROCESSING))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
