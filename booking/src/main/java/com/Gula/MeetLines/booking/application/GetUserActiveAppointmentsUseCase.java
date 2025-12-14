package com.Gula.MeetLines.booking.application;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import com.Gula.MeetLines.booking.domain.AppointmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for getting active (pending) appointments for a user.
 */
@Service
@RequiredArgsConstructor
public class GetUserActiveAppointmentsUseCase {

    private final AppointmentRepository appointmentRepository;

    /**
     * Gets active (pending) appointments for a user.
     * 
     * @param userId The user identifier
     * @return List of user's pending appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> execute(UUID userId) {
        return appointmentRepository.findByUserIdAndStatus(userId, AppointmentStatus.pending);
    }
}
