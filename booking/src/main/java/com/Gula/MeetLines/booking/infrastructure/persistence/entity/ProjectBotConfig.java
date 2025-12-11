package com.Gula.MeetLines.booking.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * JPA Entity for project_bot_configs table.
 * 
 * <p>
 * Maps to the existing table that contains chatbot and appointment
 * configuration.
 * </p>
 */
@Entity
@Table(name = "project_bot_configs")
@Data
public class ProjectBotConfig {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "bot_name")
    private String botName;

    @Column(name = "industry")
    private String industry;

    @Column(name = "tone")
    private String tone;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "transactional_config_json", columnDefinition = "jsonb")
    private String transactionalConfigJson;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean isActive;
}
