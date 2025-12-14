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
         * Finds appointments by user ID and status.
         * 
         * <p>
         * Spring generates: SELECT * FROM appointments WHERE app_users_id = ? AND status = ?
         * </p>
         * 
         * @param appUserId User identifier
         * @param status Appointment status
         * @return List of appointment entities
         */
        List<AppointmentEntity> findByAppUserIdAndStatus(UUID appUserId, AppointmentStatus status);

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
         * Spring Data JPA query method derivation:
         * </p>
         * <pre>
         * SELECT * FROM appointments 
         * WHERE status = 'PENDING' 
         * AND start_time <= ?
         * ORDER BY start_time ASC
         * </pre>
         * 
         * @param currentTime Current time
         * @return List of pending appointments to start
         */
        List<AppointmentEntity> findByStatusAndStartTimeLessThanEqualOrderByStartTimeAsc(
                        AppointmentStatus status, ZonedDateTime currentTime);

        /**
         * Finds IN_PROGRESS appointments that should complete (end_time <= currentTime).
         * 
         * <p>
         * Spring Data JPA query method derivation:
         * </p>
         * <pre>
         * SELECT * FROM appointments 
         * WHERE status = 'IN_PROGRESS' 
         * AND end_time <= ?
         * ORDER BY end_time ASC
         * </pre>
         * 
         * @param currentTime Current time
         * @return List of in-progress appointments to complete
         */
        List<AppointmentEntity> findByStatusAndEndTimeLessThanEqualOrderByEndTimeAsc(
                        AppointmentStatus status, ZonedDateTime currentTime);

        /**
         * Finds appointments within a date range for a project.
         * 
         * <p>
         * ⚠️ Why @Query? Spring Data derived method would be too verbose:
         * findByProjectIdAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc
         * @Query provides better readability and explicit control.
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
         * ⚠️ Why @Query? OBLIGATORIO - Spring Data NO puede derivar:
         * </p>
         * <ul>
         * <li>Lógica de overlapping: (start < endTime) AND (end > startTime)</li>
         * <li>Condiciones OR con IS NULL: (:excludeId IS NULL OR a.id != :excludeId)</li>
         * <li>IN clause con múltiples estados</li>
         * </ul>
         * 
         * <p>
         * Two appointments conflict if they overlap in time:
         * </p>
         * <pre>
         * (requestedStart &lt; existingEnd) AND (requestedEnd &gt; existingStart)
         * </pre>
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
                        AND a.status IN ('pending', 'in_progress')
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
         * Finds appointments for an employee that conflict with a given time slot.
         * 
         * <p>
         * Used to check if an employee is available for a specific time slot.
         * Only considers active appointments (PENDING or IN_PROGRESS).
         * </p>
         * 
         * @param employeeId Employee identifier
         * @param startTime  Requested start time
         * @param endTime    Requested end time
         * @param excludeId  Appointment ID to exclude (for rescheduling), can be null
         * @return List of conflicting appointments for this employee (empty if no
         *         conflicts)
         */
        @Query("""
                        SELECT a FROM AppointmentEntity a
                        WHERE a.employeeId = :employeeId
                        AND a.status IN ('pending', 'in_progress')
                        AND a.startTime < :endTime
                        AND a.endTime > :startTime
                        AND (:excludeId IS NULL OR a.id != :excludeId)
                        """)
        List<AppointmentEntity> findConflictingAppointmentsForEmployee(
                        @Param("employeeId") UUID employeeId,
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

        /**
         * Finds appointments for a project on a specific date.
         * 
         * <p>
         * ⚠️ Why @Query? OBLIGATORIO - Requires SQL function:
         * </p>
         * <ul>
         * <li>CAST(startTime AS date) - Spring Data cannot derive this</li>
         * <li>IN clause with multiple statuses</li>
         * </ul>
         * 
         * @param projectId Project identifier
         * @param date      The date to search
         * @return List of appointments on that date
         */
        @Query("""
                        SELECT a FROM AppointmentEntity a
                        WHERE a.projectId = :projectId
                        AND CAST(a.startTime AS date) = :date
                        AND a.status IN ('pending', 'in_progress')
                        ORDER BY a.startTime ASC
                        """)
        List<AppointmentEntity> findByProjectIdAndDate(
                        @Param("projectId") UUID projectId,
                        @Param("date") java.time.LocalDate date);

        /**
         * Finds appointments for a specific employee on a specific date.
         * 
         * <p>
         * Used to check which slots are already booked for an employee on a given day.
         * </p>
         * 
         * @param employeeId Employee identifier
         * @param date       The date to search
         * @return List of appointments for that employee on that date
         */
        @Query("""
                        SELECT a FROM AppointmentEntity a
                        WHERE a.employeeId = :employeeId
                        AND CAST(a.startTime AS date) = :date
                        AND a.status IN ('pending', 'in_progress')
                        ORDER BY a.startTime ASC
                        """)
        List<AppointmentEntity> findByEmployeeIdAndDate(
                        @Param("employeeId") UUID employeeId,
                        @Param("date") java.time.LocalDate date);
}
