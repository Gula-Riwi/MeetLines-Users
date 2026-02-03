package com.Gula.MeetLines.booking.domain;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Domain entity representing an appointment/booking in the system.
 * 
 * <p>
 * This is a RICH entity (not anemic), meaning it contains business logic
 * related to appointments. It has no infrastructure dependencies (no JPA
 * annotations,
 * no knowledge of the database).
 * </p>
 * 
 * <p>
 * <strong>DDD Principles Applied:</strong>
 * </p>
 * <ul>
 * <li>Encapsulation: Setters are private, modifications only through business
 * methods</li>
 * <li>Invariants: Constructor and methods ensure the object is always in a
 * valid state</li>
 * <li>Ubiquitous Language: Names reflect business language (confirm, cancel,
 * reschedule)</li>
 * </ul>
 * 
 * <p>
 * <strong>Why no public constructor?</strong>
 * </p>
 * <p>
 * We use a Factory Method pattern ({@link #create}) instead of a public
 * constructor because:
 * </p>
 * <ul>
 * <li>It hides internal complexity (default values, validations)</li>
 * <li>It provides a clear, intention-revealing name</li>
 * <li>It ensures all appointments start in a valid PENDING state</li>
 * <li>It's easier to evolve (add logic without breaking callers)</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Getter
@AllArgsConstructor // Public constructor
public class Appointment {

    /**
     * Unique identifier for the appointment.
     * In DDD, this could be a Value Object (AppointmentId), but for initial
     * simplicity
     * we use Integer. Can be refactored later.
     */
    private Integer id;

    /**
     * Identifier of the project this appointment belongs to.
     * Enables multi-tenancy or project separation.
     */
    private UUID projectId;

    /**
     * Identifier of the user who books the appointment.
     * This will be managed by your teammate's authentication module.
     */
    private UUID appUserId;

    /**
     * Identifier of the service to be provided.
     * References a service catalog (e.g., medical consultation, legal advice, etc.)
     */
    private Integer serviceId;

    /**
     * Identifier of the employee who will handle this appointment.
     * References the employees table. Nullable for backward compatibility with
     * existing appointments.
     */
    private UUID employeeId;

    /**
     * Start date and time of the appointment (with timezone).
     * Using ZonedDateTime is crucial for handling different timezones correctly.
     */
    private ZonedDateTime startTime;

    /**
     * End date and time of the appointment (with timezone).
     */
    private ZonedDateTime endTime;

    /**
     * Current status of the appointment.
     * In DDD, this should be a Value Object (AppointmentStatus enum),
     * which we'll create in the next file.
     */
    private AppointmentStatus status;

    /**
     * Price snapshot at the moment of creating the appointment.
     * We save a "snapshot" of the price so future changes to the service
     * don't affect already booked appointments.
     */
    private BigDecimal priceSnapshot;

    /**
     * Currency snapshot at the moment of creating the appointment.
     * In DDD, this could be a Value Object (Currency), but for now we use String.
     */
    private String currencySnapshot;

    /**
     * Virtual meeting link (optional).
     * Can be automatically generated when confirming the appointment (e.g., Zoom,
     * Google Meet).
     */
    private String meetingLink;

    /**
     * User notes about the appointment.
     * E.g., "I need to discuss my thesis project"
     */
    private String userNotes;

    /**
     * Internal administrator notes (not visible to the user).
     * E.g., "VIP client, prioritize attention"
     */
    private String adminNotes;

    /**
     * Record creation timestamp.
     */
    private ZonedDateTime createdAt;

    /**
     * Last update timestamp.
     */
    private ZonedDateTime updatedAt;

    // ==================== FACTORY METHODS ====================

    /**
     * Creates a new appointment in PENDING status.
     * 
     * <p>
     * This is a <strong>Factory Method</strong>, a DDD pattern that encapsulates
     * the creation logic and ensures the object is always created in a valid state.
     * </p>
     * 
     * @param projectId        Project identifier
     * @param appUserId        User identifier who books the appointment
     * @param serviceId        Service identifier
     * @param employeeId       Employee identifier (can be null)
     * @param startTime        Start time
     * @param endTime          End time
     * @param priceSnapshot    Service price at the moment of booking
     * @param currencySnapshot Price currency
     * @param userNotes        User notes (can be null)
     * @return New Appointment instance in PENDING status
     * @throws IllegalArgumentException if any business validation fails
     */
    public static Appointment create(
            UUID projectId,
            UUID appUserId,
            Integer serviceId,
            UUID employeeId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            BigDecimal priceSnapshot,
            String currencySnapshot,
            String userNotes) {
        // Business validations
        validateTimeRange(startTime, endTime);
        validatePrice(priceSnapshot);

        ZonedDateTime now = ZonedDateTime.now();

        return new Appointment(
                null, // ID is assigned by the database
                projectId,
                appUserId,
                serviceId,
                employeeId,
                startTime,
                endTime,
                AppointmentStatus.pending, // Initial status is always pending
                priceSnapshot,
                currencySnapshot,
                null, // Meeting link is generated later
                userNotes,
                null, // Admin notes empty on creation
                now,
                now);
    }

    // ==================== BUSINESS METHODS ====================

    /**
     * Starts the appointment, changing its status to IN_PROGRESS.
     * 
     * <p>
     * This method encapsulates the business rule: "Only pending appointments can be
     * started".
     * </p>
     * <p>
     * Note: In automatic mode, this is called by a scheduler when start time
     * arrives.
     * </p>
     * 
     * @param meetingLink Virtual meeting link (optional)
     * @throws IllegalStateException if the appointment is not in PENDING status
     */
    public void start(String meetingLink) {
        if (this.status != AppointmentStatus.pending) {
            throw new IllegalStateException(
                    "Only pending appointments can be started. Current status: " + this.status);
        }

        this.status = AppointmentStatus.in_progress;
        this.meetingLink = meetingLink;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Cancels the appointment, changing its status to CANCELLED.
     * 
     * @param adminNotes Cancellation reason (optional)
     * @throws IllegalStateException if the appointment is already completed or
     *                               cancelled
     */
    public void cancel(String adminNotes) {
        if (this.status == AppointmentStatus.cancelled) {
            throw new IllegalStateException("The appointment is already cancelled");
        }

        if (this.status == AppointmentStatus.completed) {
            throw new IllegalStateException("Cannot cancel an already completed appointment");
        }

        this.status = AppointmentStatus.cancelled;
        this.adminNotes = adminNotes;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Marks the appointment as completed.
     * 
     * <p>
     * Note: In automatic mode, this is called by a scheduler when end time passes.
     * </p>
     * 
     * @throws IllegalStateException if the appointment is not in progress
     */
    public void complete() {
        if (this.status != AppointmentStatus.in_progress) {
            throw new IllegalStateException(
                    "Only in_progress appointments can be completed. Current status: " + this.status);
        }

        this.status = AppointmentStatus.completed;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Reschedules the appointment to a new date/time.
     * 
     * @param newStartTime New start time
     * @param newEndTime   New end time
     * @throws IllegalStateException    if the appointment is cancelled or completed
     * @throws IllegalArgumentException if the time range is invalid
     */
    public void reschedule(ZonedDateTime newStartTime, ZonedDateTime newEndTime) {
        if (this.status == AppointmentStatus.cancelled || this.status == AppointmentStatus.completed) {
            throw new IllegalStateException(
                    "Cannot reschedule a cancelled or completed appointment");
        }

        validateTimeRange(newStartTime, newEndTime);

        this.startTime = newStartTime;
        this.endTime = newEndTime;
        this.updatedAt = ZonedDateTime.now();
    }

    /**
     * Adds administrator notes.
     * 
     * @param notes Notes to add
     */
    public void addAdminNotes(String notes) {
        this.adminNotes = notes;
        this.updatedAt = ZonedDateTime.now();
    }

    // ==================== PRIVATE VALIDATIONS ====================

    /**
     * Validates that the time range is coherent.
     * 
     * <p>
     * Business rules:
     * </p>
     * <ul>
     * <li>Start time must be before end time</li>
     * <li>Appointment must be in the future (cannot book appointments in the
     * past)</li>
     * </ul>
     * 
     * @param startTime Start time
     * @param endTime   End time
     * @throws IllegalArgumentException if validations fail
     */
    private static void validateTimeRange(ZonedDateTime startTime, ZonedDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }

        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException(
                    "Start time must be before end time");
        }

        if (startTime.isBefore(ZonedDateTime.now())) {
            throw new IllegalArgumentException(
                    "Cannot book appointments in the past");
        }
    }

    /**
     * Validates that the price is valid.
     * 
     * @param price Price to validate
     * @throws IllegalArgumentException if the price is null or negative
     */
    private static void validatePrice(BigDecimal price) {
        if (price == null) {
            throw new IllegalArgumentException("Price is required");
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    // ==================== QUERY METHODS ====================

    /**
     * Checks if the appointment is active (neither cancelled nor completed).
     * 
     * @return true if the appointment is pending or in progress
     */
    public boolean isActive() {
        return this.status == AppointmentStatus.pending ||
                this.status == AppointmentStatus.in_progress;
    }

    /**
     * Checks if the appointment can be modified.
     * 
     * @return true if the appointment is not cancelled or completed
     */
    public boolean canBeModified() {
        return this.status != AppointmentStatus.cancelled &&
                this.status != AppointmentStatus.completed;
    }

    /**
     * Calculates the appointment duration in minutes.
     * 
     * @return Duration in minutes
     */
    public long getDurationInMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
}
