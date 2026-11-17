package com.example.studentmanagement.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardised error body returned by the API for every error scenario.
 *
 * All API errors produce the same shape of JSON so the client can handle
 * errors predictably:
 *
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Student not found with id: 99",
 *   "path": "/api/v1/students/99",
 *   "fieldErrors": null          ← only populated for validation failures
 * }
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
     * Only populated for 400 validation errors.
     * Example: { "email": "Please provide a valid email address" }
     * Null for all other error types — Jackson omits it from the JSON output.
     */
    private Map<String, String> fieldErrors;
}
