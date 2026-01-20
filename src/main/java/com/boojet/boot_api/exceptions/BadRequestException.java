package com.boojet.boot_api.exceptions;

/**
 * Thrown when a client request is invalid.
 * <p>
 * Examples include malformed input, missing required fields, invalid dates,
 * or violations of business validation rules. Mapped to HTTP 400
 * by the global exception handler.
 */
public class BadRequestException extends RuntimeException{
    public BadRequestException(String message){
        super(message);
    }
    
}
