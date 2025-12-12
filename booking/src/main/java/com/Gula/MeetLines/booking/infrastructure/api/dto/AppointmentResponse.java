package com.Gula.MeetLines.booking.infrastructure.api.dto;

import com.Gula.MeetLines.booking.domain.Appointment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Response DTO for appointment data.
 * 
 * <p>
 * This is a <strong>Data Transfer Object (DTO)</strong> for the API layer.
 * </p>
 * 
 * <p>
 * <strong>Why use a separate DTO instead of returning domain
 * Appointment?</strong>
 * </p>
 * <ul>
 * <li>Control what data is exposed to clients</li>
 * <li>Can add computed fields (e.g., duration)</li>
 * <li>API contract is independent of domain changes</li>
 * <li>Security: Don't expose sensitive internal fields</li>
 * </ul>
 * 
 * @param id              Appointment ID
 * @param projectId       Project/business identifier
 * @param userId          User identifier
 * @param serviceId       Service identifier
 * @param startTime       Appointment start time
 * @param endTime         Appointment end time
 * @param status          Appointment status
 * @param price           Price snapshot
 * @param currency        Currency code
 * @param meetingLink     Virtual meeting link (if available)
 * @param userNotes       User notes
 * @param durationMinutes Calculated duration in minutes
 * @param createdAt       Creation timestamp
 * @param updatedAt       Last update timestamp
 */
public record AppointmentResponse(
        Long id,
        UUID projectId,
        UUID userId,
        Integer serviceId,
        UUID employeeId,
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        String status,
        BigDecimal price,
        String currency,
        String meetingLink,
        String userNotes,
        Long durationMinutes,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt) {
    /**
     * Creates a response DTO from a domain Appointment.
     * 
     * @param appointment Domain appointment
     * @return Response DTO
     */
    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getProjectId(),
                appointment.getAppUserId(),
                appointment.getServiceId(),
                appointment.getEmployeeId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                appointment.getStatus().name(),
                appointment.getPriceSnapshot(),
                appointment.getCurrencySnapshot(),
                appointment.getMeetingLink(),
                appointment.getUserNotes(),
                appointment.getDurationInMinutes(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt());
    }
}
