package com.Gula.MeetLines.booking.infrastructure.api.dto;

import com.Gula.MeetLines.booking.domain.TimeSlot;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for available time slots.
 */
public record AvailableSlotsResponse(
        LocalDate date,
        int totalSlots,
        List<TimeSlotDTO> availableSlots) {

    /**
     * Creates a response from a list of TimeSlots.
     */
    public static AvailableSlotsResponse from(LocalDate date, List<TimeSlot> timeSlots) {
        List<TimeSlotDTO> slotDTOs = timeSlots.stream()
                .map(TimeSlotDTO::from)
                .collect(Collectors.toList());

        return new AvailableSlotsResponse(
                date,
                slotDTOs.size(),
                slotDTOs);
    }
}
