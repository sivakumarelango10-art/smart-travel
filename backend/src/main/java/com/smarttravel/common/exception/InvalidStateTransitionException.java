package com.smarttravel.common.exception;

/**
 * Exception thrown when an illegal flight lifecycle state transition is requested.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
