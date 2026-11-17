package com.example.studentmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource (e.g. student by ID) does not exist.
 * Maps to HTTP 404 Not Found via GlobalExceptionHandler.
 *
 * Extends RuntimeException (unchecked) so it doesn't need to be declared
 * in every method signature with 'throws' — it bubbles up automatically.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
