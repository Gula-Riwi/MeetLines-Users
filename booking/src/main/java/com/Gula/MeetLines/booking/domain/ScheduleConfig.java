package com.Gula.MeetLines.booking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Value object representing the schedule configuration from
 * project_bot_configs.
 * 
 * <p>
 * Maps to the JSON structure in transactional_config_json column.
 * </p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleConfig {

    private Integer slotDuration;

    @JsonProperty("businessHours")
    private Map<String, DaySchedule> businessHours;

    private Boolean appointmentEnabled;

    private Integer bufferBetweenAppointments;

    private String timezone;

    /**
     * Checks if appointments are enabled.
     */
    public boolean isAppointmentEnabled() {
        return appointmentEnabled != null && appointmentEnabled;
    }

    /**
     * Gets the timezone, defaulting to America/Bogota if not specified.
     */
    public ZoneId getZoneId() {
        return timezone != null ? ZoneId.of(timezone) : ZoneId.of("America/Bogota");
    }

    /**
     * Gets the business hours for a specific day of week.
     */
    public DaySchedule getDaySchedule(DayOfWeek dayOfWeek) {
        String dayKey = dayOfWeek.name().toLowerCase();
        return businessHours != null ? businessHours.get(dayKey) : null;
    }

    /**
     * Generates all possible time slots for a given date.
     * 
     * @param date The date to generate slots for
     * @return List of TimeSlot objects
     */
    public List<TimeSlot> generateSlotsForDate(LocalDate date) {
        DaySchedule daySchedule = getDaySchedule(date.getDayOfWeek());

        if (daySchedule == null || daySchedule.isClosed()) {
            return Collections.emptyList();
        }

        List<TimeSlot> slots = new ArrayList<>();
        LocalTime current = daySchedule.getStartTime();
        LocalTime end = daySchedule.getEndTime();
        ZoneId zoneId = getZoneId();

        int totalMinutesPerSlot = slotDuration + (bufferBetweenAppointments != null ? bufferBetweenAppointments : 0);

        while (current.plusMinutes(slotDuration).isBefore(end) ||
                current.plusMinutes(slotDuration).equals(end)) {

            ZonedDateTime slotStart = ZonedDateTime.of(date, current, zoneId);
            ZonedDateTime slotEnd = ZonedDateTime.of(date, current.plusMinutes(slotDuration), zoneId);

            slots.add(new TimeSlot(slotStart, slotEnd));

            current = current.plusMinutes(totalMinutesPerSlot);
        }

        return slots;
    }
}
