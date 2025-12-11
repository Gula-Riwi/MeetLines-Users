package com.Gula.MeetLines.booking.domain.events;

import com.Gula.MeetLines.booking.domain.Appointment;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Domain Event representing that an appointment has been successfully booked.
 * 
 * <p>
 * This is a <strong>Domain Event</strong> in DDD terms, which means:
 * </p>
 * <ul>
 * <li>It represents something that ALREADY HAPPENED (past tense: "booked")</li>
 * <li>It's immutable (all fields are final, no setters)</li>
 * <li>It carries the minimum data needed for event handlers</li>
 * <li>It enables decoupling between domain and infrastructure</li>
 * </ul>
 * 
 * <p>
 * <strong>Why use Domain Events?</strong>
 * </p>
 * <ul>
 * <li><strong>Decoupling:</strong> The domain doesn't know about n8n, email,
 * SMS, etc.</li>
 * <li><strong>Single Responsibility:</strong> Booking logic is separate from
 * notification logic</li>
 * <li><strong>Extensibility:</strong> Easy to add new listeners without
 * changing domain code</li>
 * <li><strong>Audit trail:</strong> Events can be stored for
 * history/debugging</li>
 * </ul>
 * 
 * <p>
 * <strong>Event Flow:</strong>
 * </p>
 * 
 * <pre>
 *     1. User books appointment
 *     2. BookAppointmentUseCase creates Appointment
 *     3. UseCase publishes AppointmentBookedEvent
 *     4. Event listeners react:
 *        - NotificationListener → calls n8n webhook
 *        - AuditListener → logs the event
 *        - AnalyticsListener → tracks metrics
 * </pre>
 * 
 * <p>
 * <strong>Spring Event Publishing:</strong>
 * </p>
 * 
 * <pre>
 * // In your UseCase:
 * Appointment appointment = Appointment.create(...);
 * repository.save(appointment);
 * 
 * // Publish event
 * AppointmentBookedEvent event = new AppointmentBookedEvent(
 *     appointment.getId(),
 *     appointment.getProjectId(),
 *     appointment.getAppUserId(),
 *     appointment.getStartTime(),
 *     appointment.getEndTime(),
 *     ZonedDateTime.now()
 * );
 * eventPublisher.publishEvent(event);
 * </pre>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Getter
public class AppointmentBookedEvent {

    /**
     * Unique identifier of the booked appointment.
     * Used to fetch full appointment details if needed.
     */
    private final Long appointmentId;

    /**
     * Identifier of the project/business where the appointment was booked.
     * Useful for routing notifications to the correct business owner.
     */
    private final UUID projectId;

    /**
     * Identifier of the user who booked the appointment.
     * Used to send notifications to the correct user.
     */
    private final UUID userId;

    /**
     * Start time of the appointment.
     * Used in notification messages: "Your appointment is scheduled for..."
     */
    private final ZonedDateTime startTime;

    /**
     * End time of the appointment.
     * Used to calculate appointment duration in notifications.
     */
    private final ZonedDateTime endTime;

    /**
     * Timestamp when this event was created (when the booking happened).
     * Useful for event ordering and audit trails.
     */
    private final ZonedDateTime occurredOn;

    /**
     * Creates a new AppointmentBookedEvent.
     * 
     * <p>
     * <strong>Why all parameters are required?</strong>
     * </p>
     * <p>
     * Events should be self-contained with all necessary data.
     * Listeners shouldn't need to query the database to get basic info.
     * </p>
     * 
     * @param appointmentId Unique identifier of the appointment
     * @param projectId     Project/business identifier
     * @param userId        User who booked the appointment
     * @param startTime     Appointment start time
     * @param endTime       Appointment end time
     * @param occurredOn    When this event occurred
     */
    public AppointmentBookedEvent(
            Long appointmentId,
            UUID projectId,
            UUID userId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            ZonedDateTime occurredOn) {
        this.appointmentId = appointmentId;
        this.projectId = projectId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.occurredOn = occurredOn;
    }

    /**
     * Factory method to create an event from an Appointment entity.
     * 
     * <p>
     * This is a convenience method to avoid repeating field mappings
     * in every UseCase.
     * </p>
     * 
     * <p>
     * <strong>Example usage:</strong>
     * </p>
     * 
     * <pre>
     * Appointment appointment = repository.save(newAppointment);
     * AppointmentBookedEvent event = AppointmentBookedEvent.from(appointment);
     * eventPublisher.publishEvent(event);
     * </pre>
     * 
     * @param appointment The appointment that was booked
     * @return A new AppointmentBookedEvent
     * @throws IllegalArgumentException if appointment is null or has no ID
     */
    public static AppointmentBookedEvent from(Appointment appointment) {
        if (appointment == null) {
            throw new IllegalArgumentException("Appointment cannot be null");
        }

        if (appointment.getId() == null) {
            throw new IllegalArgumentException(
                    "Appointment must be saved (have an ID) before creating an event");
        }

        return new AppointmentBookedEvent(
                appointment.getId(),
                appointment.getProjectId(),
                appointment.getAppUserId(),
                appointment.getStartTime(),
                appointment.getEndTime(),
                ZonedDateTime.now());
    }

    /**
     * Gets a human-readable description of this event.
     * Useful for logging and debugging.
     * 
     * @return Event description
     */
    @Override
    public String toString() {
        return String.format(
                "AppointmentBookedEvent[appointmentId=%d, userId=%s, startTime=%s, occurredOn=%s]",
                appointmentId,
                userId,
                startTime,
                occurredOn);
    }
}
