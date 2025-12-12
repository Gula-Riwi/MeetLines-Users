package com.Gula.MeetLines.booking.application;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case for retrieving a single appointment by ID.
 * 
 * <p>
 * This is a simple <strong>Query Use Case</strong> with no business logic.
 * </p>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Service
@RequiredArgsConstructor
public class GetAppointmentUseCase {

    private final AppointmentRepository appointmentRepository;

    /**
     * Retrieves an appointment by ID.
     * 
     * @param appointmentId The appointment ID
     * @return The appointment
     * @throws AppointmentNotFoundException if not found
     */
    @Transactional(readOnly = true)
    public Appointment execute(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException(
                        "Appointment with ID " + appointmentId + " not found"));
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
