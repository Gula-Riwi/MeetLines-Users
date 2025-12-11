package com.Gula.MeetLines.booking.application;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import com.Gula.MeetLines.booking.domain.events.AppointmentCancelledEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for cancelling an appointment.
 * 
 * <p>
 * This is an <strong>Application Service</strong> that orchestrates the
 * cancellation process.
 * </p>
 * 
 * <p>
 * <strong>Responsibilities:</strong>
 * </p>
 * <ul>
 * <li>Find the appointment</li>
 * <li>Delegate cancellation to domain (business rules)</li>
 * <li>Save the updated appointment</li>
 * <li>Publish cancellation event (triggers notification)</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Service
@RequiredArgsConstructor
public class CancelAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Cancels an appointment.
     * 
     * <p>
     * <strong>Process flow:</strong>
     * </p>
     * <ol>
     * <li>Find appointment by ID</li>
     * <li>Call domain method to cancel (validates business rules)</li>
     * <li>Save updated appointment</li>
     * <li>Publish AppointmentCancelledEvent (triggers n8n notification)</li>
     * </ol>
     * 
     * @param command The cancellation command
     * @return The cancelled appointment
     * @throws AppointmentNotFoundException if appointment doesn't exist
     * @throws IllegalStateException        if appointment cannot be cancelled (from
     *                                      domain)
     */
    @Transactional
    public Appointment execute(CancelAppointmentCommand command) {
        // Find appointment
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment with ID " + command.appointmentId() + " not found"));

        // Cancel (domain validates business rules)
        appointment.cancel(command.reason());

        // Save
        Appointment cancelled = appointmentRepository.save(appointment);

        // Publish event
        AppointmentCancelledEvent event = AppointmentCancelledEvent.from(cancelled, command.reason());
        eventPublisher.publishEvent(event);

        return cancelled;
    }

    /**
     * Command for cancelling an appointment.
     * 
     * @param appointmentId ID of the appointment to cancel
     * @param reason        Reason for cancellation (optional)
     */
    public record CancelAppointmentCommand(
            Long appointmentId,
            String reason) {
        public CancelAppointmentCommand {
            if (appointmentId == null) {
                throw new IllegalArgumentException("Appointment ID is required");
            }
        }
    }

    /**
     * Exception thrown when an appointment is not found.
     */
    public static class AppointmentNotFoundException extends RuntimeException {
        public AppointmentNotFoundException(String message) {
            super(message);
        }
    }
}
