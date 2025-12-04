package com.Gula.MeetLines.booking.domain;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Appointment aggregate.
 * 
 * <p>
 * This is a <strong>Port</strong> in Hexagonal Architecture (Output Port),
 * which means:
 * </p>
 * <ul>
 * <li>It's defined in the DOMAIN layer (not infrastructure)</li>
 * <li>It uses domain language (save, find, not insert, select)</li>
 * <li>It works with domain entities (Appointment), not database entities</li>
 * <li>The implementation is in the INFRASTRUCTURE layer</li>
 * </ul>
 * 
 * <p>
 * <strong>Why is this an interface in the domain?</strong>
 * </p>
 * <p>
 * This is the <strong>Dependency Inversion Principle</strong> in action:
 * </p>
 * <ul>
 * <li>Domain defines WHAT it needs (this interface)</li>
 * <li>Infrastructure provides HOW it's done (JPA implementation)</li>
 * <li>Domain doesn't depend on infrastructure (it depends on abstraction)</li>
 * <li>Infrastructure depends on domain (implements this interface)</li>
 * </ul>
 * 
 * <p>
 * <strong>Hexagonal Architecture flow:</strong>
 * </p>
 * 
 * <pre>
 *     UseCase (Application) → AppointmentRepository (Domain Interface)
 *                                        ↑
 *                                        | implements
 *                                        |
 *                          JpaAppointmentRepository (Infrastructure)
 * </pre>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
public interface AppointmentRepository {

    /**
     * Saves an appointment (create or update).
     * 
     * <p>
     * This method handles both creation and updates:
     * </p>
     * <ul>
     * <li>If appointment.getId() is null → INSERT (new appointment)</li>
     * <li>If appointment.getId() exists → UPDATE (existing appointment)</li>
     * </ul>
     * 
     * <p>
     * <strong>Why "save" instead of "insert" or "update"?</strong>
     * </p>
     * <p>
     * We use domain language, not database language. The domain doesn't care
     * about SQL operations, it just wants to "save" an appointment.
     * </p>
     * 
     * @param appointment The appointment to save
     * @return The saved appointment with generated ID (if it was new)
     */
    Appointment save(Appointment appointment);

    /**
     * Finds an appointment by its unique identifier.
     * 
     * <p>
     * <strong>Why Optional?</strong>
     * </p>
     * <p>
     * Using Optional makes it explicit that the appointment might not exist,
     * forcing callers to handle the "not found" case explicitly.
     * </p>
     * 
     * @param id The appointment ID
     * @return Optional containing the appointment if found, empty otherwise
     */
    Optional<Appointment> findById(Long id);

    /**
     * Finds all appointments for a specific user.
     * 
     * <p>
     * <strong>Use case:</strong> User wants to see their appointment history.
     * </p>
     * 
     * @param userId The user identifier
     * @return List of appointments (empty list if none found)
     */
    List<Appointment> findByUserId(UUID userId);

    /**
     * Finds all appointments for a specific project.
     * 
     * <p>
     * <strong>Use case:</strong> Business owner wants to see all appointments
     * for their business/project.
     * </p>
     * 
     * @param projectId The project identifier
     * @return List of appointments (empty list if none found)
     */
    List<Appointment> findByProjectId(UUID projectId);

    /**
     * Finds appointments by status.
     * 
     * <p>
     * <strong>Use cases:</strong>
     * </p>
     * <ul>
     * <li>Find all PENDING appointments to send reminders</li>
     * <li>Find all IN_PROGRESS appointments to monitor</li>
     * <li>Find all COMPLETED appointments for reports</li>
     * </ul>
     * 
     * @param status The appointment status
     * @return List of appointments with the given status
     */
    List<Appointment> findByStatus(AppointmentStatus status);

    /**
     * Finds appointments that should transition to a new status.
     * 
     * <p>
     * This is used by a scheduler to automatically update appointment statuses:
     * </p>
     * <ul>
     * <li>PENDING → IN_PROGRESS (when start time arrives)</li>
     * <li>IN_PROGRESS → COMPLETED (when end time passes)</li>
     * </ul>
     * 
     * <p>
     * <strong>Example for starting appointments:</strong>
     * </p>
     * 
     * <pre>
     * // Find PENDING appointments whose start time has arrived
     * List&lt;Appointment&gt; toStart = repository.findPendingAppointmentsToStart(now);
     * toStart.forEach(apt -&gt; apt.start(meetingLink));
     * </pre>
     * 
     * @param currentTime The current time to compare against
     * @return List of PENDING appointments where start_time <= currentTime
     */
    List<Appointment> findPendingAppointmentsToStart(ZonedDateTime currentTime);

    /**
     * Finds IN_PROGRESS appointments that should be completed.
     * 
     * <p>
     * <strong>Example for completing appointments:</strong>
     * </p>
     * 
     * <pre>
     * // Find IN_PROGRESS appointments whose end time has passed
     * List&lt;Appointment&gt; toComplete = repository.findInProgressAppointmentsToComplete(now);
     * toComplete.forEach(Appointment::complete);
     * </pre>
     * 
     * @param currentTime The current time to compare against
     * @return List of IN_PROGRESS appointments where end_time <= currentTime
     */
    List<Appointment> findInProgressAppointmentsToComplete(ZonedDateTime currentTime);

    /**
     * Finds appointments within a date range.
     * 
     * <p>
     * <strong>Use cases:</strong>
     * </p>
     * <ul>
     * <li>Calendar view: Show appointments for a specific week/month</li>
     * <li>Reports: Generate statistics for a time period</li>
     * <li>Availability check: See if a time slot is available</li>
     * </ul>
     * 
     * @param projectId The project identifier
     * @param startDate Start of the date range (inclusive)
     * @param endDate   End of the date range (inclusive)
     * @return List of appointments within the date range
     */
    List<Appointment> findByProjectIdAndDateRange(
            UUID projectId,
            ZonedDateTime startDate,
            ZonedDateTime endDate);

    /**
     * Checks if a time slot is available for booking.
     * 
     * <p>
     * A time slot is available if there are NO active appointments
     * (PENDING or IN_PROGRESS) that overlap with the requested time.
     * </p>
     * 
     * <p>
     * <strong>Overlap detection:</strong>
     * </p>
     * <p>
     * Two appointments overlap if:
     * </p>
     * 
     * <pre>
     * (requestedStart &lt; existingEnd) AND (requestedEnd &gt; existingStart)
     * </pre>
     * 
     * <p>
     * <strong>Use case:</strong> Before creating an appointment, check if
     * the time slot is available to prevent double-booking.
     * </p>
     * 
     * @param projectId The project identifier
     * @param startTime Requested start time
     * @param endTime   Requested end time
     * @param excludeId Appointment ID to exclude (for rescheduling), null for new
     *                  appointments
     * @return true if the time slot is available, false if there's a conflict
     */
    boolean isTimeSlotAvailable(
            UUID projectId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            Long excludeId);

    /**
     * Deletes an appointment by ID.
     * 
     * <p>
     * <strong>Note:</strong> In most business cases, you should use
     * {@link Appointment#cancel(String)} instead of physically deleting.
     * This preserves history and allows for analytics.
     * </p>
     * 
     * <p>
     * <strong>Use this only for:</strong>
     * </p>
     * <ul>
     * <li>GDPR/data deletion requests</li>
     * <li>Test data cleanup</li>
     * <li>Administrative corrections</li>
     * </ul>
     * 
     * @param id The appointment ID to delete
     */
    void deleteById(Long id);

    /**
     * Counts appointments by status for a project.
     * 
     * <p>
     * <strong>Use case:</strong> Dashboard statistics showing:
     * </p>
     * <ul>
     * <li>10 pending appointments</li>
     * <li>3 in progress</li>
     * <li>50 completed</li>
     * </ul>
     * 
     * @param projectId The project identifier
     * @param status    The status to count
     * @return Number of appointments with the given status
     */
    long countByProjectIdAndStatus(UUID projectId, AppointmentStatus status);
}
