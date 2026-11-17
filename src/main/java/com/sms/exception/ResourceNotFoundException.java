package com.sms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a requested resource does not exist.
 *
 * KEY CONCEPT — Custom Exceptions:
 * Instead of returning null or using generic exceptions, we throw
 * semantically meaningful exceptions that describe exactly what went wrong.
 *
 * WHY extends RuntimeException?
 *   - Checked exceptions (extends Exception) must be declared in method signatures
 *     with 'throws' — this creates noise across the codebase.
 *   - Spring and most modern Java frameworks use unchecked (Runtime) exceptions
 *     for business errors, letting them bubble up to a central handler.
 *   - @Transactional only rolls back on RuntimeException by default.
 *
 * @ResponseStatus → tells Spring what HTTP status to use if this exception
 * reaches a controller and is NOT caught by a @ExceptionHandler.
 * This is a safety net — our GlobalExceptionHandler will catch it first.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * @param message → a human-readable description, e.g.:
     *                  "Student not found with id: 42"
     *                  This message is passed to RuntimeException and stored
     *                  so we can retrieve it later with getMessage().
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
