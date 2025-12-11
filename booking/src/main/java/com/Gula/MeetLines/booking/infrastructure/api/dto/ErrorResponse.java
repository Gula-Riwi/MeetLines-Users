package com.Gula.MeetLines.booking.infrastructure.api.dto;

import java.time.ZonedDateTime;

/**
 * Standard error response DTO.
 * 
 * <p>
 * This provides a consistent error format across all API endpoints.
 * </p>
 * 
 * <p>
 * <strong>Example error response:</strong>
 * </p>
 * 
 * <pre>
 * {
 *   "code": "TIME_SLOT_NOT_AVAILABLE",
 *   "message": "Time slot from 2025-12-05T10:00:00 to 2025-12-05T11:00:00 is already booked",
 *   "timestamp": "2025-12-03T19:45:00-05:00"
 * }
 * </pre>
 * 
 * @param code      Error code (e.g., "TIME_SLOT_NOT_AVAILABLE",
 *                  "INVALID_ARGUMENT")
 * @param message   Human-readable error message
 * @param timestamp When the error occurred
 */
public record ErrorResponse(
        String code,
        String message,
        ZonedDateTime timestamp) {
    /**
     * Creates an error response with current timestamp.
     * 
     * @param code    Error code
     * @param message Error message
     */
    public ErrorResponse(String code, String message) {
        this(code, message, ZonedDateTime.now());
    }
}
