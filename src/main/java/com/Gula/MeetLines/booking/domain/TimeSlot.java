package com.Gula.MeetLines.booking.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;

/**
 * Value object representing an available time slot for appointments.
 * 
 * <p>
 * Immutable value object in the domain layer.
 * </p>
 */
@Getter
@AllArgsConstructor
public class TimeSlot {

    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;

    /**
     * Calculates the duration of this slot in minutes.
     */
    public long getDurationInMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Checks if this slot overlaps with an appointment.
     * 
     * Two time intervals overlap if:
     * slotStart < appointmentEnd AND slotEnd > appointmentStart
     * 
     * Examples:
     * - Slot 10:30-11:00 does NOT overlap with appointment 10:00-10:30 (adjacent,
     * not overlapping)
     * - Slot 10:00-10:30 DOES overlap with appointment 10:15-10:45
     */
    public boolean overlapsWith(ZonedDateTime appointmentStart, ZonedDateTime appointmentEnd) {
        return startTime.isBefore(appointmentEnd) && endTime.isAfter(appointmentStart);
    }
}
