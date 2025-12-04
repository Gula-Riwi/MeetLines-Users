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
 * Use Case for listing appointments with various filters.
 * 
 * <p>
 * This is a <strong>Query Use Case</strong> that provides different ways to
 * list appointments.
 * </p>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Service
@RequiredArgsConstructor
public class ListAppointmentsUseCase {

    private final AppointmentRepository appointmentRepository;

    /**
     * Lists all appointments for a specific user.
     * 
     * <p>
     * <strong>Use case:</strong> User wants to see their appointment history.
     * </p>
     * 
     * @param userId The user ID
     * @return List of appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> executeByUser(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        return appointmentRepository.findByUserId(userId);
    }

    /**
     * Lists all appointments for a specific project/business.
     * 
     * <p>
     * <strong>Use case:</strong> Business owner wants to see all appointments for
     * their business.
     * </p>
     * 
     * @param projectId The project ID
     * @return List of appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> executeByProject(UUID projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID is required");
        }
        return appointmentRepository.findByProjectId(projectId);
    }

    /**
     * Lists appointments by status for a project.
     * 
     * <p>
     * <strong>Use case:</strong> Business owner wants to see all
     * pending/in-progress appointments.
     * </p>
     * 
     * @param projectId The project ID
     * @param status    The appointment status
     * @return List of appointments
     */
    @Transactional(readOnly = true)
    public List<Appointment> executeByProjectAndStatus(UUID projectId, AppointmentStatus status) {
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        // Get all appointments for project and filter by status
        return appointmentRepository.findByProjectId(projectId)
                .stream()
                .filter(apt -> apt.getStatus() == status)
                .toList();
    }
}
