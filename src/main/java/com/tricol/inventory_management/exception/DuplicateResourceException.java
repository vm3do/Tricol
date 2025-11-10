package com.tricol.inventory_management.exception;

/**
 * Thrown when attempting to create a resource that already exists (unique constraint violation).
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
