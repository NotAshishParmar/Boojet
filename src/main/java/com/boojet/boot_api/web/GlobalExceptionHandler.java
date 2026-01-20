package com.boojet.boot_api.web;

import java.time.Instant;

import com.boojet.boot_api.exceptions.BadRequestException;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;


/**
 * Centralized REST exception handling for the Boojet API.
 * <p>
 * Converts common application/runtime exceptions into consistent {@link ApiError}
 * JSON responses with an HTTP status code and request path.
 *
 * <p>Handled cases:
 * <ul>
 *   <li>{@link NotFoundException} -> 404 Not Found</li>
 *   <li>{@link BadRequestException} and {@link IllegalArgumentException} -> 400 Bad Request</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handles "resource not found" errors.
     *
     * @param ex  the thrown exception
     * @param req the HTTP request (used to include the request URI in the response)
     * @return a 404 response containing an {@link ApiError} body
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex, HttpServletRequest req){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiError(Instant.now(), 404, "Not Found", ex.getMessage(), req.getRequestURI())
        );
    }

    /**
     * Handles invalid client input (validation / bad parameters).
     *
     * @param ex  the thrown exception
     * @param req the HTTP request (used to include the request URI in the response)
     * @return a 400 response containing an {@link ApiError} body
     */
    @ExceptionHandler({BadRequestException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex, HttpServletRequest req){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError(Instant.now(), 400, "Bad Request", ex.getMessage(), req.getRequestURI())
        );
    }
}
