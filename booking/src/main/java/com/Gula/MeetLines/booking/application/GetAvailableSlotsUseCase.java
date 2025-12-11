package com.Gula.MeetLines.booking.application;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import com.Gula.MeetLines.booking.domain.ScheduleConfig;
import com.Gula.MeetLines.booking.domain.TimeSlot;
import com.Gula.MeetLines.booking.infrastructure.persistence.ProjectBotConfigRepository;
import com.Gula.MeetLines.booking.infrastructure.persistence.entity.ProjectBotConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Use case for getting available appointment slots for a project on a specific
 * date.
 */
@Service
@RequiredArgsConstructor
public class GetAvailableSlotsUseCase {

        private final ProjectBotConfigRepository projectBotConfigRepository;
        private final AppointmentRepository appointmentRepository;
        private final ObjectMapper objectMapper = new ObjectMapper();

        /**
         * Executes the use case to get available slots.
         * 
         * @param projectId The project identifier
         * @param date      The date to check availability
         * @return List of available TimeSlots
         * @throws IllegalArgumentException if project not found or appointments
         *                                  disabled
         */
        @Transactional(readOnly = true)
        public List<TimeSlot> execute(UUID projectId, LocalDate date) {
                // Step 1: Get project configuration
                ProjectBotConfig config = projectBotConfigRepository.findByProjectId(projectId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Project configuration not found for project: " + projectId));

                // Step 2: Parse JSON to ScheduleConfig
                ScheduleConfig scheduleConfig;
                try {
                        String jsonConfig = config.getTransactionalConfigJson();
                        if (jsonConfig == null || jsonConfig.isBlank()) {
                                throw new IllegalArgumentException(
                                                "Schedule configuration not found for project: " + projectId);
                        }
                        scheduleConfig = objectMapper.readValue(jsonConfig, ScheduleConfig.class);
                } catch (Exception e) {
                        throw new IllegalArgumentException(
                                        "Invalid schedule configuration for project: " + projectId, e);
                }

                // Step 2: Check if appointments are enabled
                if (!scheduleConfig.isAppointmentEnabled()) {
                        throw new IllegalArgumentException(
                                        "Appointments are disabled for project: " + projectId);
                }

                // Step 3: Generate all possible slots for the date
                List<TimeSlot> allSlots = scheduleConfig.generateSlotsForDate(date);

                if (allSlots.isEmpty()) {
                        // Day is closed or no configuration
                        return allSlots;
                }

                // Step 4: Get booked appointments for that date
                List<Appointment> bookedAppointments = appointmentRepository
                                .findByProjectIdAndDate(projectId, date);

                // Step 5: Filter out booked slots
                return allSlots.stream()
                                .filter(slot -> !isSlotBooked(slot, bookedAppointments))
                                .collect(Collectors.toList());
        }

        /**
         * Checks if a time slot overlaps with any booked appointment.
         */
        private boolean isSlotBooked(TimeSlot slot, List<Appointment> bookedAppointments) {
                return bookedAppointments.stream()
                                .anyMatch(appointment -> slot.overlapsWith(
                                                appointment.getStartTime(),
                                                appointment.getEndTime()));
        }
}
