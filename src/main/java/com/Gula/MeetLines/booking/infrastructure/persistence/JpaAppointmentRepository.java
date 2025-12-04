package com.Gula.MeetLines.booking.infrastructure.persistence;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import com.Gula.MeetLines.booking.domain.AppointmentStatus;
import com.Gula.MeetLines.booking.infrastructure.persistence.entity.AppointmentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA implementation of the AppointmentRepository domain interface.
 * 
 * <p>
 * This is an <strong>Adapter</strong> in Hexagonal Architecture, which means:
 * </p>
 * <ul>
 * <li>It's in the INFRASTRUCTURE layer</li>
 * <li>It implements a domain interface (AppointmentRepository)</li>
 * <li>It adapts Spring Data JPA to the domain's needs</li>
 * <li>It handles the mapping between domain and JPA entities</li>
 * </ul>
 * 
 * <p>
 * <strong>Architecture pattern:</strong>
 * </p>
 * 
 * <pre>
 *     Domain Interface (AppointmentRepository)
 *            ↑ implements
 *     JpaAppointmentRepository (this class)
 *            ↓ delegates to
 *     SpringDataJpaRepository (Spring Data interface)
 *            ↓ uses
 *     AppointmentEntity (JPA entity)
 * </pre>
 * 
 * <p>
 * <strong>Responsibilities:</strong>
 * </p>
 * <ul>
 * <li>Implement all methods from AppointmentRepository interface</li>
 * <li>Delegate to Spring Data JPA repository</li>
 * <li>Map between domain Appointment and JPA AppointmentEntity</li>
 * <li>Provide custom queries when needed</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Repository
@RequiredArgsConstructor
public class JpaAppointmentRepository implements AppointmentRepository {

    /**
     * Spring Data JPA repository for AppointmentEntity.
     * 
     * <p>
     * This is a Spring Data interface that provides:
     * </p>
     * <ul>
     * <li>Basic CRUD operations (save, findById, delete, etc.)</li>
     * <li>Custom query methods (defined below)</li>
     * <li>Automatic implementation by Spring</li>
     * </ul>
     */
    private final SpringDataAppointmentRepository springDataRepository;

    @Override
    public Appointment save(Appointment appointment) {
        // Convert domain → entity
        AppointmentEntity entity = AppointmentEntity.fromDomain(appointment);

        // Save using Spring Data JPA
        AppointmentEntity savedEntity = springDataRepository.save(entity);

        // Convert entity → domain
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return springDataRepository.findById(id)
                .map(AppointmentEntity::toDomain);
    }

    @Override
    public List<Appointment> findByUserId(UUID userId) {
        return springDataRepository.findByAppUserId(userId)
                .stream()
                .map(AppointmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByProjectId(UUID projectId) {
        return springDataRepository.findByProjectId(projectId)
                .stream()
                .map(AppointmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        return springDataRepository.findByStatus(status)
                .stream()
                .map(AppointmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findPendingAppointmentsToStart(ZonedDateTime currentTime) {
        return springDataRepository.findPendingAppointmentsToStart(currentTime)
                .stream()
                .map(AppointmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findInProgressAppointmentsToComplete(ZonedDateTime currentTime) {
        return springDataRepository.findInProgressAppointmentsToComplete(currentTime)
                .stream()
                .map(AppointmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Appointment> findByProjectIdAndDateRange(
            UUID projectId,
            ZonedDateTime startDate,
            ZonedDateTime endDate) {
        return springDataRepository.findByProjectIdAndDateRange(projectId, startDate, endDate)
                .stream()
                .map(AppointmentEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isTimeSlotAvailable(
            UUID projectId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Long excludeId) {
        List<AppointmentEntity> conflicts = springDataRepository.findConflictingAppointments(
                projectId,
                startTime,
                endTime,
                excludeId);

        return conflicts.isEmpty();
    }

    @Override
    public void deleteById(Long id) {
        springDataRepository.deleteById(id);
    }

    @Override
    public long countByProjectIdAndStatus(UUID projectId, AppointmentStatus status) {
        return springDataRepository.countByProjectIdAndStatus(projectId, status);
    }

    /**
     * Spring Data JPA repository interface.
     * 
     * <p>
     * Spring automatically implements this interface at runtime.
     * Methods are implemented based on naming conventions or custom @Query
     * annotations.
     * </p>
     * 
     * <p>
     * <strong>Method naming conventions:</strong>
     * </p>
     * <ul>
     * <li>findBy[FieldName] → SELECT WHERE field = ?</li>
     * <li>countBy[FieldName] → SELECT COUNT WHERE field = ?</li>
     * <li>Custom queries use @Query annotation</li>
     * </ul>
     */
    interface SpringDataAppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

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
}
