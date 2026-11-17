package com.sms.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler — catches exceptions thrown anywhere in the application
 * and converts them into structured HTTP error responses.
 *
 * KEY CONCEPT — @ControllerAdvice (or @RestControllerAdvice):
 *
 * Without this class, if our service throws ResourceNotFoundException:
 *   - Spring catches it and returns a generic Whitelabel Error Page (HTML)
 *   - OR a poorly structured JSON with stack trace details
 *
 * With @ControllerAdvice, we intercept exceptions BEFORE Spring's default handling.
 *
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody
 *   → Just like @RestController, this ensures our handler methods return JSON bodies.
 *
 * HOW IT WORKS:
 *   1. Any @RestController throws an exception
 *   2. Spring's DispatcherServlet catches it
 *   3. It looks for a matching @ExceptionHandler in any @ControllerAdvice bean
 *   4. Calls our handler method, which builds and returns the ErrorResponse
 *   5. Spring serialises ErrorResponse to JSON and sends the HTTP response
 *
 * The HttpServletRequest parameter gives us the request path — injected by Spring automatically.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ─────────────────────────────────────────────────────────────────────────
    // 404 Not Found
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles ResourceNotFoundException — thrown when a student ID doesn't exist.
     *
     * @ExceptionHandler(ResourceNotFoundException.class) → this method is called
     *   whenever a ResourceNotFoundException bubbles up from any controller.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())       // 404
                .error(HttpStatus.NOT_FOUND.getReasonPhrase()) // "Not Found"
                .message(ex.getMessage())                   // "Student not found with id: 42"
                .path(request.getRequestURI())              // "/api/v1/students/42"
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 409 Conflict
    // ─────────────────────────────────────────────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error(HttpStatus.CONFLICT.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 400 Bad Request — Bean Validation failures
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Handles MethodArgumentNotValidException — thrown by Spring when @Valid fails.
     *
     * This exception contains a BindingResult with ALL the validation failures.
     * We extract them into a Map<fieldName, errorMessage> for a clear response.
     *
     * Example response body:
     * {
     *   "timestamp": "2024-01-15T10:30:00",
     *   "status": 400,
     *   "error": "Bad Request",
     *   "message": "Validation failed",
     *   "path": "/api/v1/students",
     *   "fieldErrors": {
     *     "email": "Please provide a valid email address",
     *     "firstName": "First name is required"
     *   }
     * }
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Collect all field errors into a Map
        Map<String, String> fieldErrors = new HashMap<>();

        // ex.getBindingResult().getFieldErrors() → list of all failed field constraints
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // fieldError.getField()          → "email", "firstName", etc.
            // fieldError.getDefaultMessage() → the message from @NotBlank(message="...")
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed. Please check the field errors.")
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 500 Internal Server Error — catch-all
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Catch-all for any unexpected exception we didn't anticipate.
     *
     * IMPORTANT: In production, do NOT leak the exception message to the client
     * as it may contain sensitive information (SQL queries, internal paths, etc.)
     * Use a generic message and log the full exception server-side.
     *
     * Exception.class → matches any exception (most general, so Spring picks it last)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        // Log the full stack trace server-side (visible in your IDE console)
        // In production you'd use: log.error("Unexpected error", ex);
        ex.printStackTrace();

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please contact support.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
