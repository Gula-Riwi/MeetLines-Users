package com.Gula.MeetLines.booking.application;

import com.Gula.MeetLines.booking.domain.DaySchedule;
import com.Gula.MeetLines.booking.domain.ScheduleConfig;
import com.Gula.MeetLines.booking.infrastructure.persistence.ProjectBotConfigRepository;
import com.Gula.MeetLines.booking.infrastructure.persistence.entity.ProjectBotConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Use case for getting project working hours on a specific date.
 * 
 * <p>
 * This returns the opening and closing times for the business on a given date.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class GetProjectWorkingHoursUseCase {

    private final ProjectBotConfigRepository projectBotConfigRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gets working hours for a project on a specific date.
     * 
     * @param projectId The project identifier
     * @param date      The date to check
     * @return WorkingHoursInfo with opening/closing times and open status
     * @throws IllegalArgumentException if project not found
     */
    @Transactional(readOnly = true)
    public WorkingHoursInfo execute(UUID projectId, LocalDate date) {
        // Get project configuration
        ProjectBotConfig config = projectBotConfigRepository.findByProjectId(projectId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project configuration not found for project: " + projectId));

        // Parse schedule configuration
        ScheduleConfig scheduleConfig;
        try {
            String jsonConfig = config.getTransactionalConfigJson();
            if (jsonConfig == null || jsonConfig.isBlank()) {
                return WorkingHoursInfo.closed();
            }
            scheduleConfig = objectMapper.readValue(jsonConfig, ScheduleConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Invalid schedule configuration for project: " + projectId, e);
        }

        // Check if appointments are enabled
        if (!scheduleConfig.isAppointmentEnabled()) {
            return WorkingHoursInfo.closed();
        }

        // Get day of week
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        
        // Get schedule for this day
        DaySchedule daySchedule = scheduleConfig.getDaySchedule(dayOfWeek);
        
        if (daySchedule == null || daySchedule.isClosed()) {
            return WorkingHoursInfo.closed();
        }

        // Get current time in Colombia timezone
        ZonedDateTime nowInColombia = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        LocalTime currentTime = nowInColombia.toLocalTime();
        LocalDate today = nowInColombia.toLocalDate();

        // Check if the requested date is today and if we are within working hours
        boolean isCurrentlyOpen = date.equals(today) 
                && currentTime.isAfter(daySchedule.getStartTime()) 
                && currentTime.isBefore(daySchedule.getEndTime());

        return WorkingHoursInfo.open(
                daySchedule.getStartTime(),
                daySchedule.getEndTime(),
                isCurrentlyOpen);
    }

    /**
     * Information about working hours for a specific date.
     */
    public static class WorkingHoursInfo {
        private final LocalTime openingTime;
        private final LocalTime closingTime;
        private final boolean isOpen;

        private WorkingHoursInfo(LocalTime openingTime, LocalTime closingTime, boolean isOpen) {
            this.openingTime = openingTime;
            this.closingTime = closingTime;
            this.isOpen = isOpen;
        }

        public static WorkingHoursInfo closed() {
            return new WorkingHoursInfo(null, null, false);
        }

        public static WorkingHoursInfo open(LocalTime openingTime, LocalTime closingTime, boolean isCurrentlyOpen) {
            return new WorkingHoursInfo(openingTime, closingTime, isCurrentlyOpen);
        }

        public LocalTime getOpeningTime() {
            return openingTime;
        }

        public LocalTime getClosingTime() {
            return closingTime;
        }

        public boolean isOpen() {
            return isOpen;
        }
    }
}
