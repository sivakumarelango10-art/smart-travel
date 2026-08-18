package com.smarttravel.modules.flight.service;

import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.modules.flight.model.FlightStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlightStateMachineTest {

    private FlightStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new FlightStateMachine();
    }

    @Nested
    @DisplayName("Valid Status Transitions")
    class ValidTransitions {

        @ParameterizedTest(name = "{0} -> {1} should be valid")
        @CsvSource({
                "SCHEDULED, BOARDING",
                "SCHEDULED, DELAYED",
                "SCHEDULED, CANCELLED",
                "BOARDING, ON_TIME",
                "BOARDING, DELAYED",
                "ON_TIME, DEPARTED",
                "ON_TIME, DELAYED",
                "DELAYED, BOARDING",
                "DELAYED, DEPARTED",
                "DELAYED, CANCELLED",
                "DEPARTED, ARRIVED",
                "DEPARTED, DIVERTED"
        })
        void testPermittedTransitions(FlightStatus current, FlightStatus next) {
            assertTrue(stateMachine.isValidTransition(current, next));
            assertDoesNotThrow(() -> stateMachine.validateTransition(current, next));
        }
    }

    @Nested
    @DisplayName("Invalid Status Transitions")
    class InvalidTransitions {

        @ParameterizedTest(name = "{0} -> {1} should be invalid")
        @CsvSource({
                "ARRIVED, SCHEDULED",
                "ARRIVED, DELAYED",
                "CANCELLED, DEPARTED",
                "DIVERTED, ARRIVED",
                "DEPARTED, BOARDING",
                "SCHEDULED, ARRIVED",
                "BOARDING, ARRIVED",
                "ON_TIME, ARRIVED"
        })
        void testForbiddenTransitions(FlightStatus current, FlightStatus next) {
            assertFalse(stateMachine.isValidTransition(current, next));
            assertThrows(InvalidStateTransitionException.class, () -> stateMachine.validateTransition(current, next));
        }

        @Test
        @DisplayName("Terminal states cannot transition to any other status")
        void testTerminalStates() {
            for (FlightStatus next : FlightStatus.values()) {
                assertFalse(stateMachine.isValidTransition(FlightStatus.ARRIVED, next));
                assertFalse(stateMachine.isValidTransition(FlightStatus.CANCELLED, next));
                assertFalse(stateMachine.isValidTransition(FlightStatus.DIVERTED, next));
            }
        }
    }
}
