package com.Gula.MeetLines.booking.application;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import com.Gula.MeetLines.booking.domain.events.AppointmentBookedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Use Case for booking a new appointment.
 * 
 * <p>
 * This is an <strong>Application Service</strong> in DDD terms, which means:
 * </p>
 * <ul>
 * <li>It orchestrates domain objects and infrastructure</li>
 * <li>It contains NO business logic (that's in the domain)</li>
 * <li>It represents a user action: "Book an appointment"</li>
 * <li>It's the entry point from the API/Controller layer</li>
 * </ul>
 * 
 * <p>
 * <strong>Responsibilities:</strong>
 * </p>
 * <ul>
 * <li>Validate availability (business rule delegation)</li>
 * <li>Create appointment (domain factory method)</li>
 * <li>Save appointment (repository)</li>
 * <li>Publish domain event (event publisher)</li>
 * <li>Manage transaction boundaries (@Transactional)</li>
 * </ul>
 * 
 * <p>
 * <strong>What this class does NOT do:</strong>
 * </p>
 * <ul>
 * <li>❌ Validate business rules (that's in Appointment.create())</li>
 * <li>❌ Know about database details (that's in JpaAppointmentRepository)</li>
 * <li>❌ Send notifications directly (that's in event listeners)</li>
 * <li>❌ Know about HTTP/REST (that's in the controller)</li>
 * </ul>
 * 
 * <p>
 * <strong>Hexagonal Architecture position:</strong>
 * </p>
 * 
 * <pre>
 *     Controller (API) → BookAppointmentUseCase (Application)
 *                              ↓ uses
 *                        Domain (Appointment, Repository interface)
 *                              ↓ implemented by
 *                        Infrastructure (JpaRepository, EventListener)
 * </pre>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Service
@RequiredArgsConstructor
public class BookAppointmentUseCase {

    /**
     * Repository for appointment persistence.
     * This is an interface from the domain layer, implemented in infrastructure.
     */
    private final AppointmentRepository appointmentRepository;

    /**
     * Spring's event publisher for domain events.
     * Used to publish AppointmentBookedEvent after successful booking.
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Books a new appointment.
     * 
     * <p>
     * <strong>Process flow:</strong>
     * </p>
     * <ol>
     * <li>Check if time slot is available (prevent double-booking)</li>
     * <li>Create appointment using domain factory method (validates business
     * rules)</li>
     * <li>Save appointment to database</li>
     * <li>Publish AppointmentBookedEvent (triggers notifications via n8n)</li>
     * <li>Return the created appointment</li>
     * </ol>
     * 
     * <p>
     * <strong>Transaction management:</strong>
     * </p>
     * <p>
     * The @Transactional annotation ensures that:
     * </p>
     * <ul>
     * <li>If any step fails, the entire operation is rolled back</li>
     * <li>The event is only published if the save succeeds</li>
     * <li>Database consistency is maintained</li>
     * </ul>
     * 
     * <p>
     * <strong>Example usage from controller:</strong>
     * </p>
     * 
     * <pre>
     * BookAppointmentCommand command = new BookAppointmentCommand(...);
     * Appointment appointment = bookAppointmentUseCase.execute(command);
     * return ResponseEntity.ok(AppointmentResponse.from(appointment));
     * </pre>
     * 
     * @param command The command containing all data needed to book an appointment
     * @return The created appointment with generated ID
     * @throws TimeSlotNotAvailableException if the requested time slot is already
     *                                       booked
     * @throws IllegalArgumentException      if any business validation fails (from
     *                                       Appointment.create())
     */
    @Transactional
    public Appointment execute(BookAppointmentCommand command) {
        // Step 1: Check availability
        boolean isAvailable = appointmentRepository.isTimeSlotAvailable(
                command.projectId(),
                command.startTime(),
                command.endTime(),
                null // null because this is a new appointment
        );

        if (!isAvailable) {
            throw new TimeSlotNotAvailableException(
                    String.format(
                            "Time slot from %s to %s is already booked",
                            command.startTime(),
                            command.endTime()));
        }

        // Step 2: Create appointment (domain validates business rules)
        Appointment appointment = Appointment.create(
                command.projectId(),
                command.userId(),
                command.serviceId(),
                command.startTime(),
                command.endTime(),
                command.price(),
                command.currency(),
                command.userNotes());

        // Step 3: Save to database
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Step 4: Publish domain event (triggers n8n notification)
        AppointmentBookedEvent event = AppointmentBookedEvent.from(savedAppointment);
        eventPublisher.publishEvent(event);

        // Step 5: Return the created appointment
        return savedAppointment;
    }

    /**
     * Command object containing all data needed to book an appointment.
     * 
     * <p>
     * This is a <strong>Command</strong> pattern (input DTO for the use case).
     * </p>
     * 
     * <p>
     * <strong>Why use a record?</strong>
     * </p>
     * <ul>
     * <li>Immutable by default (all fields are final)</li>
     * <li>Concise syntax (no boilerplate)</li>
     * <li>Built-in equals/hashCode/toString</li>
     * <li>Clear intent: this is just data, no behavior</li>
     * </ul>
     * 
     * <p>
     * <strong>Why not use Appointment directly?</strong>
     * </p>
     * <ul>
     * <li>Separation of concerns: API layer uses DTOs, domain uses entities</li>
     * <li>Flexibility: Command can have different fields than Appointment</li>
     * <li>Validation: Can add validation annotations here without polluting
     * domain</li>
     * </ul>
     * 
     * @param projectId Project/business identifier
     * @param userId    User who is booking the appointment
     * @param serviceId Service being booked
     * @param startTime Appointment start time
     * @param endTime   Appointment end time
     * @param price     Price of the service at booking time
     * @param currency  Currency code (e.g., "COP", "USD")
     * @param userNotes Optional notes from the user
     */
    public record BookAppointmentCommand(
            UUID projectId,
            UUID userId,
            Integer serviceId,
            ZonedDateTime startTime,
            ZonedDateTime endTime,
            BigDecimal price,
            String currency,
            String userNotes) {
        /**
         * Compact constructor for validation.
         * 
         * <p>
         * This is called automatically when creating the record.
         * We validate that required fields are not null.
         * </p>
         * 
         * <p>
         * <strong>Note:</strong> Business validations (like "start before end")
         * are in Appointment.create(), not here. This only validates nulls.
         * </p>
         */
        public BookAppointmentCommand {
            if (projectId == null) {
                throw new IllegalArgumentException("Project ID is required");
            }
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (serviceId == null) {
                throw new IllegalArgumentException("Service ID is required");
            }
            if (startTime == null) {
                throw new IllegalArgumentException("Start time is required");
            }
            if (endTime == null) {
                throw new IllegalArgumentException("End time is required");
            }
            if (price == null) {
                throw new IllegalArgumentException("Price is required");
            }
            if (currency == null || currency.isBlank()) {
                throw new IllegalArgumentException("Currency is required");
            }
        }
    }

    /**
     * Exception thrown when a requested time slot is not available.
     * 
     * <p>
     * This is a <strong>domain exception</strong> that represents a business rule
     * violation:
     * "Cannot book two appointments at the same time".
     * </p>
     * 
     * <p>
     * <strong>Usage in controller:</strong>
     * </p>
     * 
     * <pre>
     * @ExceptionHandler(TimeSlotNotAvailableException.class)
     * public ResponseEntity&lt;ErrorResponse&gt; handleTimeSlotNotAvailable(
     *         TimeSlotNotAvailableException ex) {
     *     return ResponseEntity
     *             .status(HttpStatus.CONFLICT)
     *             .body(new ErrorResponse(ex.getMessage()));
     * }
     * </pre>
     */
    public static class TimeSlotNotAvailableException extends RuntimeException {
        public TimeSlotNotAvailableException(String message) {
            super(message);
        }
    }
}
