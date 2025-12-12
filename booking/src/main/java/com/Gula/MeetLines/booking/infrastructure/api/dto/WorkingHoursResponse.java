package com.Gula.MeetLines.booking.infrastructure.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

/**
 * Response DTO for project working hours on a specific date.
 * 
 * @param date        The date (yyyy-MM-dd)
 * @param openingTime Opening time (HH:mm:ss)
 * @param closingTime Closing time (HH:mm:ss)
 * @param isOpen      Whether the business is open on this date
 */
public record WorkingHoursResponse(
        String date,
        @JsonProperty("openingTime") String openingTime,
        @JsonProperty("closingTime") String closingTime,
        @JsonProperty("isOpen") boolean isOpen) {

    public static WorkingHoursResponse closed(String date) {
        return new WorkingHoursResponse(date, null, null, false);
    }

    public static WorkingHoursResponse open(String date, LocalTime openingTime, LocalTime closingTime) {
        return new WorkingHoursResponse(
                date,
                openingTime.toString(),
                closingTime.toString(),
                true);
    }
}
