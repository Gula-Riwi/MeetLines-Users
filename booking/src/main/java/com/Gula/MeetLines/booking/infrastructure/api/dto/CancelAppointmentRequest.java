package com.Gula.MeetLines.booking.infrastructure.api.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for cancelling an appointment.
 * 
 * @param reason Reason for cancellation (optional)
 */
public record CancelAppointmentRequest(
        @Size(max = 500, message = "Reason cannot exceed 500 characters") String reason) {
}
