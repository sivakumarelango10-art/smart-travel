package com.smarttravel.modules.booking.service;

import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.modules.booking.model.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingStateMachineTest {

    private BookingStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new BookingStateMachine();
    }

    @ParameterizedTest(name = "{0} -> {1} should be VALID")
    @CsvSource({
            "PENDING, CONFIRMED",
            "PENDING, CANCELLED",
            "PENDING, EXPIRED",
            "CONFIRMED, CANCELLED"
    })
    @DisplayName("Valid transitions are permitted")
    void testValidTransitions(BookingStatus current, BookingStatus next) {
        assertTrue(stateMachine.isValidTransition(current, next));
        assertDoesNotThrow(() -> stateMachine.validateTransition(current, next));
    }

    @ParameterizedTest(name = "{0} -> {1} should be INVALID")
    @CsvSource({
            "CANCELLED, PENDING",
            "CANCELLED, CONFIRMED",
            "CANCELLED, EXPIRED",
            "EXPIRED, PENDING",
            "EXPIRED, CONFIRMED",
            "EXPIRED, CANCELLED",
            "CONFIRMED, PENDING",
            "CONFIRMED, EXPIRED"
    })
    @DisplayName("Invalid transitions throw InvalidStateTransitionException")
    void testInvalidTransitions(BookingStatus current, BookingStatus next) {
        assertFalse(stateMachine.isValidTransition(current, next));
        assertThrows(InvalidStateTransitionException.class, () -> stateMachine.validateTransition(current, next));
    }

    @Test
    @DisplayName("Null transitions return false")
    void testNullTransitions() {
        assertFalse(stateMachine.isValidTransition(null, BookingStatus.CONFIRMED));
        assertFalse(stateMachine.isValidTransition(BookingStatus.PENDING, null));
        assertFalse(stateMachine.isValidTransition(null, null));
    }
}
