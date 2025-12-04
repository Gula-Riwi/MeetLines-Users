package com.Gula.MeetLines.booking.infrastructure.persistence;

import com.Gula.MeetLines.booking.domain.AppointmentStatus;
import com.Gula.MeetLines.booking.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository interface for AppointmentEntity.
 * 
 * <p>
 * This is a Spring Data interface that provides:
 * </p>
 * <ul>
 * <li>Basic CRUD operations (save, findById, delete, etc.)</li>
 * <li>Custom query methods</li>
 * <li>Automatic implementation by Spring</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
public interface SpringDataAppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    /**
     * Finds appointments by user ID.
     * 
     * <p>
     * Spring generates: SELECT * FROM appointments WHERE app_users_id = ?
     * </p>
     * 
     * @param appUserId User identifier
     * @return List of appointment entities
     */
    List<AppointmentEntity> findByAppUserId(UUID appUserId);

    /**
     * Finds appointments by project ID.
     * 
     * <p>
     * Spring generates: SELECT * FROM appointments WHERE project_id = ?
     * </p>
     * 
     * @param projectId Project identifier
     * @return List of appointment entities
     */
    List<AppointmentEntity> findByProjectId(UUID projectId);

    /**
     * Finds appointments by status.
     * 
     * <p>
     * Spring generates: SELECT * FROM appointments WHERE status = ?
     * </p>
     * 
     * @param status Appointment status
     * @return List of appointment entities
     */
    List<AppointmentEntity> findByStatus(AppointmentStatus status);

    /**
     * Finds PENDING appointments that should start (start_time <= currentTime).
     * 
     * <p>
     * Custom query to find appointments ready to transition to IN_PROGRESS.
     * </p>
     * 
     * @param currentTime Current time
     * @return List of pending appointments to start
     */
    @Query("""
            SELECT a FROM AppointmentEntity a
            WHERE a.status = 'PENDING'
            AND a.startTime <= :currentTime
            ORDER BY a.startTime ASC
            """)
    List<AppointmentEntity> findPendingAppointmentsToStart(
            @Param("currentTime") ZonedDateTime currentTime);

    /**
     * Finds IN_PROGRESS appointments that should complete (end_time <=
     * currentTime).
     * 
     * <p>
     * Custom query to find appointments ready to transition to COMPLETED.
     * </p>
     * 
     * @param currentTime Current time
     * @return List of in-progress appointments to complete
     */
    @Query("""
            SELECT a FROM AppointmentEntity a
            WHERE a.status = 'IN_PROGRESS'
            AND a.endTime <= :currentTime
            ORDER BY a.endTime ASC
            """)
    List<AppointmentEntity> findInProgressAppointmentsToComplete(
            @Param("currentTime") ZonedDateTime currentTime);

    /**
     * Finds appointments within a date range for a project.
     * 
     * <p>
     * Used for calendar views and availability checks.
     * </p>
     * 
     * @param projectId Project identifier
     * @param startDate Start of date range
     * @param endDate   End of date range
     * @return List of appointments in the range
     */
    @Query("""
            SELECT a FROM AppointmentEntity a
            WHERE a.projectId = :projectId
            AND a.startTime >= :startDate
            AND a.endTime <= :endDate
            ORDER BY a.startTime ASC
            """)
    List<AppointmentEntity> findByProjectIdAndDateRange(
            @Param("projectId") UUID projectId,
            @Param("startDate") ZonedDateTime startDate,
            @Param("endDate") ZonedDateTime endDate);

    /**
     * Finds appointments that conflict with a given time slot.
     * 
     * <p>
     * Two appointments conflict if they overlap in time:
     * </p>
     * 
     * <pre>
     * (requestedStart &lt; existingEnd) AND (requestedEnd &gt; existingStart)
     * </pre>
     * 
     * <p>
     * Only considers active appointments (PENDING or IN_PROGRESS).
     * </p>
     * 
     * @param projectId Project identifier
     * @param startTime Requested start time
     * @param endTime   Requested end time
     * @param excludeId Appointment ID to exclude (for rescheduling), can be null
     * @return List of conflicting appointments (empty if no conflicts)
     */
    @Query("""
            SELECT a FROM AppointmentEntity a
            WHERE a.projectId = :projectId
            AND a.status IN ('PENDING', 'IN_PROGRESS')
            AND a.startTime < :endTime
            AND a.endTime > :startTime
            AND (:excludeId IS NULL OR a.id != :excludeId)
            """)
    List<AppointmentEntity> findConflictingAppointments(
            @Param("projectId") UUID projectId,
            @Param("startTime") ZonedDateTime startTime,
            @Param("endTime") ZonedDateTime endTime,
            @Param("excludeId") Long excludeId);

    /**
     * Counts appointments by project and status.
     * 
     * <p>
     * Spring generates: SELECT COUNT(*) FROM appointments
     * WHERE project_id = ? AND status = ?
     * </p>
     * 
     * @param projectId Project identifier
     * @param status    Appointment status
     * @return Count of appointments
     */
    long countByProjectIdAndStatus(UUID projectId, AppointmentStatus status);
}
