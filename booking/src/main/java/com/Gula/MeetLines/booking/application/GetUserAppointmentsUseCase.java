package com.Gula.MeetLines.booking.application;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case for getting all appointments for a user (history).
 */
@Service
@RequiredArgsConstructor
public class GetUserAppointmentsUseCase {

    private final AppointmentRepository appointmentRepository;

    /**
     * Gets all appointments for a user.
     * 
     * @param userId The user identifier
     * @return List of all user's appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> execute(UUID userId) {
        return appointmentRepository.findByUserId(userId);
    }
}
