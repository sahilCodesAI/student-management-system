package com.sms.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardised error response body returned by the API for ALL errors.
 *
 * KEY CONCEPT — Why a custom error POJO?
 *
 * Without this, Spring's default error response looks like:
 * {
 *   "timestamp": "...",
 *   "status": 404,
 *   "error": "Not Found",
 *   "path": "/api/v1/students/999"
 * }
 *
 * This is generic and not very helpful. Our ErrorResponse gives the client:
 *   - timestamp   → when the error occurred
 *   - status      → HTTP status code (e.g. 404)
 *   - error       → short HTTP reason phrase (e.g. "Not Found")
 *   - message     → human-readable description from our exception
 *   - path        → the exact URL that caused the error
 *   - fieldErrors → only for validation errors: maps field name → error message
 *                   e.g. {"email": "Please provide a valid email address"}
 *
 * Jackson serialises this to JSON automatically.
 * 'fieldErrors' will be omitted from the JSON if null (configured in handler).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Only populated for validation errors (400 Bad Request).
     * Map<fieldName, errorMessage> — e.g.:
     *   "firstName" → "First name is required"
     *   "email"     → "Please provide a valid email address"
     *
     * null for non-validation errors, so Jackson won't include it in the JSON
     * (requires @JsonInclude(NON_NULL) on the class or globally).
     */
    private Map<String, String> fieldErrors;
}
