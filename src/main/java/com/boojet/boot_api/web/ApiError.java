package com.boojet.boot_api.web;

import java.time.Instant;


/**
 * Standard error response body returned by the Boojet API when a request fails.
 * <p>
 * Instances of this record are typically produced by {@link GlobalExceptionHandler}
 * and returned as JSON to provide consistent error details.
 *
 * @param timestamp time the error response was created (UTC instant)
 * @param status HTTP status code (e.g., 400, 404, 500)
 * @param error short HTTP reason phrase (e.g., "Bad Request", "Not Found")
 * @param message human-readable error message
 * @param path request path that triggered the error
 */
public record ApiError( 
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
){}

    

