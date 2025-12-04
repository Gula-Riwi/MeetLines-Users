package com.Gula.MeetLines.booking.domain.events;

import com.Gula.MeetLines.booking.domain.Appointment;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Domain Event representing that an appointment has been cancelled.
 * 
 * <p>
 * This event is published when an appointment is cancelled and triggers:
 * </p>
 * <ul>
 * <li>Notification to user via n8n</li>
 * <li>Notification to business owner</li>
 * <li>Audit logging</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Getter
public class AppointmentCancelledEvent {

    private final Long appointmentId;
    private final UUID projectId;
    private final UUID userId;
    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;
    private final String cancellationReason;
    private final ZonedDateTime occurredOn;

    public AppointmentCancelledEvent(
            Long appointmentId,
            UUID projectId,
            UUID userId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            String cancellationReason,
            ZonedDateTime occurredOn) {
        this.appointmentId = appointmentId;
        this.projectId = projectId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cancellationReason = cancellationReason;
        this.occurredOn = occurredOn;
    }

    /**
     * Factory method to create event from Appointment.
     * 
     * @param appointment        The cancelled appointment
     * @param cancellationReason Reason for cancellation
     * @return AppointmentCancelledEvent
     */
    public static AppointmentCancelledEvent from(Appointment appointment, String cancellationReason) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }

        return new AppointmentCancelledEvent(
                appointment.getId(),
                appointment.getProjectId(),
                appointment.getAppUserId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                cancellationReason,
                ZonedDateTime.now());
    }

    @Override
    public String toString() {
        return String.format(
                "AppointmentCancelledEvent[appointmentId=%d, userId=%s, reason=%s, occurredOn=%s]",
                appointmentId,
                userId,
                cancellationReason,
                occurredOn);
    }
}
