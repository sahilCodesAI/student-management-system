package com.sms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a client tries to create or update a resource
 * with a value that already exists and must be unique (e.g., email address).
 *
 * HTTP 409 Conflict → the request conflicts with the current state of the resource.
 * This is the semantically correct status code for uniqueness violations.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
