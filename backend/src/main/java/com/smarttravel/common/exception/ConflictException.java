package com.smarttravel.common.exception;

/**
 * Exception thrown when a request conflicts with the current state of a resource
 * (e.g., insufficient inventory or concurrency race condition).
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
