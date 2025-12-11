package com.Gula.MeetLines.booking.infrastructure.persistence.converter;

import com.Gula.MeetLines.booking.domain.ScheduleConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA Converter for ScheduleConfig to/from JSONB.
 */
@Converter
public class ScheduleConfigConverter implements AttributeConverter<ScheduleConfig, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(ScheduleConfig config) {
        if (config == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting ScheduleConfig to JSON", e);
        }
    }

    @Override
    public ScheduleConfig convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ScheduleConfig.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON to ScheduleConfig", e);
        }
    }
}
