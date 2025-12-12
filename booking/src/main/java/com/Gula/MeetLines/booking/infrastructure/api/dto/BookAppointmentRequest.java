package com.Gula.MeetLines.booking.infrastructure.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Request DTO for booking an appointment.
 * 
 * <p>
 * This is a <strong>Data Transfer Object (DTO)</strong> for the API layer.
 * </p>
 * 
 * <p>
 * <strong>Why use a separate DTO instead of domain Appointment?</strong>
 * </p>
 * <ul>
 * <li>Separation of concerns: API contracts vs domain model</li>
 * <li>Flexibility: Can change API without changing domain</li>
 * <li>Validation: Can use Jakarta Validation annotations here</li>
 * <li>Security: Don't expose internal domain structure</li>
 * </ul>
 * 
 * @param projectId Project/business identifier
 * @param userId    User who is booking the appointment
 * @param serviceId Service being booked
 * @param startTime Appointment start time
 * @param endTime   Appointment end time
 * @param price     Price of the service
 * @param currency  Currency code (e.g., "COP", "USD")
 * @param userNotes Optional notes from the user
 */
public record BookAppointmentRequest(
                @NotNull(message = "Project ID is required") UUID projectId,

                @NotNull(message = "User ID is required") UUID userId,

                @NotNull(message = "Service ID is required") @Positive(message = "Service ID must be positive") Integer serviceId,

                UUID employeeId,

                @NotNull(message = "Start time is required") @Future(message = "Start time must be in the future") ZonedDateTime startTime,

                @NotNull(message = "End time is required") @Future(message = "End time must be in the future") ZonedDateTime endTime,

                @NotNull(message = "Price is required") @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,

                @NotNull(message = "Currency is required") @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g., COP, USD)") String currency,

                @Size(max = 1000, message = "User notes cannot exceed 1000 characters") String userNotes) {
}
