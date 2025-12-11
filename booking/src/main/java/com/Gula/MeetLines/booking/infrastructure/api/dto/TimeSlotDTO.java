package com.Gula.MeetLines.booking.infrastructure.api.dto;

import com.Gula.MeetLines.booking.domain.TimeSlot;

import java.time.ZonedDateTime;

/**
 * DTO for a single time slot.
 */
public record TimeSlotDTO(
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        long durationMinutes) {

    /**
     * Creates a DTO from a domain TimeSlot.
     */
    public static TimeSlotDTO from(TimeSlot timeSlot) {
        return new TimeSlotDTO(
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getDurationInMinutes());
    }
}
