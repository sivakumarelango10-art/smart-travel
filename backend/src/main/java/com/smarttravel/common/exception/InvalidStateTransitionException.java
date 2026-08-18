package com.smarttravel.common.exception;

/**
 * Exception thrown when an illegal flight or booking lifecycle state transition is requested.
 * Maps to HTTP 409 Conflict.
 */
public class InvalidStateTransitionException extends ConflictException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
