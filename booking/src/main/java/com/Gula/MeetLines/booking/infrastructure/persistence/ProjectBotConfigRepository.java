package com.Gula.MeetLines.booking.infrastructure.persistence;

import com.Gula.MeetLines.booking.infrastructure.persistence.entity.ProjectBotConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for ProjectBotConfig.
 */
@Repository
public interface ProjectBotConfigRepository extends JpaRepository<ProjectBotConfig, UUID> {

    /**
     * Finds configuration by project ID.
     */
    Optional<ProjectBotConfig> findByProjectId(UUID projectId);
}
