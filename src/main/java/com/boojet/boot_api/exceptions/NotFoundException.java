package com.boojet.boot_api.exceptions;

/**
 * Base type for "resource not found" errors in Boojet.
 * <p>
 * Thrown when a requested entity does not exist (e.g., account, transaction, income plan).
 * Mapped to HTTP 404 by the global exception handler.
 */
public abstract class NotFoundException extends RuntimeException{

    /**
     * Creates a not-found exception with a human-readable message.
     *
     * @param message description of the missing resource
     */
    public NotFoundException(String message){
        super(message);
    }
}
