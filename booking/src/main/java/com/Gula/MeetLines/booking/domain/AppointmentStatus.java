package com.Gula.MeetLines.booking.domain;

import java.time.ZonedDateTime;

/**
 * Value Object representing the status of an appointment in its lifecycle.
 * 
 * <p>
 * This is a <strong>Value Object</strong> in DDD terms, which means:
 * </p>
 * <ul>
 * <li>It's identified by its value, not by an ID</li>
 * <li>It's immutable (enum values cannot change)</li>
 * <li>It represents a domain concept (appointment lifecycle)</li>
 * <li>It encapsulates business logic related to status transitions</li>
 * </ul>
 * 
 * <p>
 * <strong>Status Lifecycle (Automatic for businesses like
 * barbershops):</strong>
 * </p>
 * 
 * <pre>
 *     User books → PENDING
 *                     ↓ (when start time arrives)
 *                  IN_PROGRESS
 *                     ↓ (when end time passes)
 *                  COMPLETED
 *     
 *     Can be cancelled at any time before COMPLETED:
 *     PENDING/IN_PROGRESS → CANCELLED
 * </pre>
 * 
 * <p>
 * <strong>Future extension for businesses requiring manual
 * confirmation:</strong>
 * </p>
 * 
 * <pre>
 *     PENDING → (manual confirm) → CONFIRMED → IN_PROGRESS → COMPLETED
 * </pre>
 * 
 * <p>
 * <strong>Why use an enum instead of String?</strong>
 * </p>
 * <ul>
 * <li>Type safety: Cannot assign invalid values like "PENDNG" (typo)</li>
 * <li>Compile-time validation: Errors caught before runtime</li>
 * <li>Behavior encapsulation: Can add methods to the enum</li>
 * <li>Better IDE support: Autocomplete, refactoring</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
public enum AppointmentStatus {

    /**
     * Initial status when an appointment is created.
     * The appointment is scheduled and waiting for its time.
     * 
     * <p>
     * <strong>Automatic transition:</strong>
     * </p>
     * <ul>
     * <li>→ IN_PROGRESS (when current time >= start time)</li>
     * </ul>
     * 
     * <p>
     * <strong>Manual transitions:</strong>
     * </p>
     * <ul>
     * <li>→ CANCELLED (if user or admin cancels)</li>
     * </ul>
     */
    PENDING("pending", "Pending"),

    /**
     * The appointment is currently in progress.
     * This status is automatically set when the appointment start time arrives.
     * 
     * <p>
     * <strong>Automatic transition:</strong>
     * </p>
     * <ul>
     * <li>→ COMPLETED (when current time >= end time)</li>
     * </ul>
     * 
     * <p>
     * <strong>Manual transitions:</strong>
     * </p>
     * <ul>
     * <li>→ CANCELLED (if cancelled during the appointment)</li>
     * </ul>
     */
    IN_PROGRESS("in_progress", "In Progress"),

    /**
     * The appointment has been successfully completed.
     * This is a final state (no further transitions).
     * 
     * <p>
     * <strong>Allowed transitions:</strong> None (terminal state)
     * </p>
     */
    COMPLETED("completed", "Completed"),

    /**
     * The appointment has been cancelled.
     * This is a final state (no further transitions).
     * 
     * <p>
     * <strong>Allowed transitions:</strong> None (terminal state)
     * </p>
     */
    CANCELLED("cancelled", "Cancelled");

    // ==================== FIELDS ====================

    /**
     * Database value for this status.
     * This is what gets stored in the database VARCHAR column.
     * Using lowercase with underscores for consistency with database conventions.
     */
    private final String databaseValue;

    /**
     * Human-readable display name for this status.
     * Can be used in UI or reports.
     */
    private final String displayName;

    // ==================== CONSTRUCTOR ====================

    /**
     * Private constructor for enum values.
     * 
     * @param databaseValue Value to store in database
     * @param displayName   Human-readable name
     */
    AppointmentStatus(String databaseValue, String displayName) {
        this.databaseValue = databaseValue;
        this.displayName = displayName;
    }

    // ==================== GETTERS ====================

    /**
     * Gets the database representation of this status.
     * 
     * @return Database value (e.g., "pending", "in_progress")
     */
    public String getDatabaseValue() {
        return databaseValue;
    }

    /**
     * Gets the human-readable display name.
     * 
     * @return Display name (e.g., "Pending", "In Progress")
     */
    public String getDisplayName() {
        return displayName;
    }

    // ==================== BUSINESS LOGIC METHODS ====================

    /**
     * Checks if this status allows the appointment to be modified (rescheduled).
     * 
     * <p>
     * Business rule: Only PENDING appointments can be rescheduled.
     * Once in progress, completed, or cancelled, modifications are not allowed.
     * </p>
     * 
     * @return true if the appointment can be modified in this status
     */
    public boolean canBeModified() {
        return this == PENDING;
    }

    /**
     * Checks if this status allows the appointment to be cancelled.
     * 
     * <p>
     * Business rule: Only PENDING and IN_PROGRESS appointments can be cancelled.
     * Already cancelled or completed appointments cannot be cancelled again.
     * </p>
     * 
     * @return true if the appointment can be cancelled in this status
     */
    public boolean canBeCancelled() {
        return this == PENDING || this == IN_PROGRESS;
    }

    /**
     * Checks if this status allows transition to IN_PROGRESS.
     * 
     * <p>
     * Business rule: Only PENDING appointments can start.
     * </p>
     * 
     * @return true if the appointment can transition to IN_PROGRESS
     */
    public boolean canStart() {
        return this == PENDING;
    }

    /**
     * Checks if this status allows transition to COMPLETED.
     * 
     * <p>
     * Business rule: Only IN_PROGRESS appointments can be completed.
     * </p>
     * 
     * @return true if the appointment can be completed in this status
     */
    public boolean canBeCompleted() {
        return this == IN_PROGRESS;
    }

    /**
     * Checks if this is a terminal status (no further transitions allowed).
     * 
     * @return true if this is COMPLETED or CANCELLED
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * Checks if this is an active status (appointment is still ongoing or
     * scheduled).
     * 
     * @return true if this is PENDING or IN_PROGRESS
     */
    public boolean isActive() {
        return this == PENDING || this == IN_PROGRESS;
    }

    /**
     * Determines the correct status based on appointment times and current time.
     * 
     * <p>
     * This method implements the automatic status transition logic:
     * </p>
     * <ul>
     * <li>If current time < start time → PENDING</li>
     * <li>If start time <= current time < end time → IN_PROGRESS</li>
     * <li>If current time >= end time → COMPLETED</li>
     * </ul>
     * 
     * <p>
     * <strong>Use case:</strong> A scheduler job can call this method periodically
     * to update appointment statuses automatically.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong>
     * </p>
     * 
     * <pre>
     * ZonedDateTime now = ZonedDateTime.now();
     * ZonedDateTime start = ZonedDateTime.parse("2025-12-05T10:00:00Z");
     * ZonedDateTime end = ZonedDateTime.parse("2025-12-05T11:00:00Z");
     * 
     * AppointmentStatus status = AppointmentStatus.determineStatusByTime(now, start, end);
     * </pre>
     * 
     * @param currentTime The current time
     * @param startTime   The appointment start time
     * @param endTime     The appointment end time
     * @return The appropriate status based on time
     * @throws IllegalArgumentException if any parameter is null or if start >= end
     */
    public static AppointmentStatus determineStatusByTime(
            ZonedDateTime currentTime,
            ZonedDateTime startTime,
            ZonedDateTime endTime) {
        if (currentTime == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Time parameters cannot be null");
        }

        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Before start time → PENDING
        if (currentTime.isBefore(startTime)) {
            return PENDING;
        }

        // Between start and end → IN_PROGRESS
        if (currentTime.isBefore(endTime)) {
            return IN_PROGRESS;
        }

        // After end time → COMPLETED
        return COMPLETED;
    }

    // ==================== FACTORY METHODS ====================

    /**
     * Converts a database value to an AppointmentStatus enum.
     * 
     * <p>
     * This is useful when reading from the database, where statuses are stored
     * as VARCHAR values like "pending", "in_progress", etc.
     * </p>
     * 
     * <p>
     * <strong>Example:</strong>
     * </p>
     * 
     * <pre>
     * AppointmentStatus status = AppointmentStatus.fromDatabaseValue("pending");
     * // Returns AppointmentStatus.PENDING
     * </pre>
     * 
     * @param databaseValue The database value (e.g., "pending", "in_progress")
     * @return The corresponding AppointmentStatus enum
     * @throws IllegalArgumentException if the database value is invalid
     */
    public static AppointmentStatus fromDatabaseValue(String databaseValue) {
        if (databaseValue == null) {
            throw new IllegalArgumentException("Database value cannot be null");
        }

        for (AppointmentStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                "Invalid appointment status: " + databaseValue +
                        ". Valid values are: pending, in_progress, completed, cancelled");
    }

    // ==================== OVERRIDES ====================

    /**
     * Returns the database value as the string representation.
     * This makes it easier to use in logging and debugging.
     * 
     * @return Database value of this status
     */
    @Override
    public String toString() {
        return databaseValue;
    }
}
