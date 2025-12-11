package com.Gula.MeetLines.booking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * Value object representing business hours for a single day.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DaySchedule {

    private String start;
    private String end;
    private Boolean closed;

    /**
     * Checks if this day is closed for business.
     */
    public boolean isClosed() {
        return closed != null && closed;
    }

    /**
     * Gets the start time as LocalTime.
     */
    public LocalTime getStartTime() {
        return isClosed() ? null : LocalTime.parse(start);
    }

    /**
     * Gets the end time as LocalTime.
     */
    public LocalTime getEndTime() {
        return isClosed() ? null : LocalTime.parse(end);
    }
}
