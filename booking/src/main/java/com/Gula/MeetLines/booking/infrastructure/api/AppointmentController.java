package com.Gula.MeetLines.booking.infrastructure.api;

import com.Gula.MeetLines.booking.application.BookAppointmentUseCase;
import com.Gula.MeetLines.booking.application.BookAppointmentUseCase.BookAppointmentCommand;
import com.Gula.MeetLines.booking.application.BookAppointmentUseCase.TimeSlotNotAvailableException;
import com.Gula.MeetLines.booking.application.BookAppointmentUseCase.EmployeeNotAvailableException;
import com.Gula.MeetLines.booking.application.CancelAppointmentUseCase;
import com.Gula.MeetLines.booking.application.CancelAppointmentUseCase.CancelAppointmentCommand;
import com.Gula.MeetLines.booking.application.GetAppointmentUseCase;
import com.Gula.MeetLines.booking.application.GetAppointmentUseCase.AppointmentNotFoundException;
import com.Gula.MeetLines.booking.application.GetAvailableSlotsUseCase;
import com.Gula.MeetLines.booking.application.GetProjectWorkingHoursUseCase;
import com.Gula.MeetLines.booking.application.GetProjectWorkingHoursUseCase.WorkingHoursInfo;
import com.Gula.MeetLines.booking.application.GetUserActiveAppointmentsUseCase;
import com.Gula.MeetLines.booking.application.GetUserAppointmentsUseCase;
import com.Gula.MeetLines.booking.application.ListAppointmentsUseCase;
import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentStatus;
import com.Gula.MeetLines.booking.infrastructure.api.dto.BookAppointmentRequest;
import com.Gula.MeetLines.booking.infrastructure.api.dto.CancelAppointmentRequest;
import com.Gula.MeetLines.booking.infrastructure.api.dto.AppointmentResponse;
import com.Gula.MeetLines.booking.infrastructure.api.dto.AvailableSlotsResponse;
import com.Gula.MeetLines.booking.infrastructure.api.dto.ErrorResponse;
import com.Gula.MeetLines.booking.infrastructure.api.dto.WorkingHoursResponse;
import com.Gula.MeetLines.booking.domain.TimeSlot;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for appointment management.
 * 
 * <p>
 * This is the <strong>API Layer</strong> in Hexagonal Architecture, which
 * means:
 * </p>
 * <ul>
 * <li>It handles HTTP requests and responses</li>
 * <li>It validates input format (not business rules)</li>
 * <li>It maps DTOs to domain commands</li>
 * <li>It delegates business logic to use cases</li>
 * </ul>
 * 
 * <p>
 * <strong>Responsibilities:</strong>
 * </p>
 * <ul>
 * <li>HTTP protocol handling (status codes, headers)</li>
 * <li>Request/Response serialization (JSON)</li>
 * <li>Input validation (@Valid)</li>
 * <li>Exception handling (@ExceptionHandler)</li>
 * </ul>
 * 
 * <p>
 * <strong>What this class does NOT do:</strong>
 * </p>
 * <ul>
 * <li>❌ Business logic (that's in use cases and domain)</li>
 * <li>❌ Database access (that's in repositories)</li>
 * <li>❌ Event publishing (that's in use cases)</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Appointments", description = "Appointment booking and management operations")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

        private final BookAppointmentUseCase bookAppointmentUseCase;
        private final CancelAppointmentUseCase cancelAppointmentUseCase;
        private final GetAppointmentUseCase getAppointmentUseCase;
        private final ListAppointmentsUseCase listAppointmentsUseCase;
        private final GetAvailableSlotsUseCase getAvailableSlotsUseCase;
        private final GetProjectWorkingHoursUseCase getProjectWorkingHoursUseCase;
        private final GetUserAppointmentsUseCase getUserAppointmentsUseCase;
        private final GetUserActiveAppointmentsUseCase getUserActiveAppointmentsUseCase;

        /**
         * Books a new appointment.
         * 
         * <p>
         * <strong>Endpoint:</strong> POST /api/v1/appointments
         * </p>
         * 
         * <p>
         * <strong>Request body example:</strong>
         * </p>
         * 
         * <pre>
         * {
         *   "projectId": "550e8400-e29b-41d4-a716-446655440000",
         *   "userId": "660e8400-e29b-41d4-a716-446655440000",
         *   "serviceId": 1,
         *   "startTime": "2025-12-05T10:00:00-05:00",
         *   "endTime": "2025-12-05T11:00:00-05:00",
         *   "price": 50000.00,
         *   "currency": "COP",
         *   "userNotes": "I need to discuss my project"
         * }
         * </pre>
         * 
         * <p>
         * <strong>Success response (201 Created):</strong>
         * </p>
         * 
         * <pre>
         * {
         *   "id": 123,
         *   "projectId": "550e8400-e29b-41d4-a716-446655440000",
         *   "userId": "660e8400-e29b-41d4-a716-446655440000",
         *   "serviceId": 1,
         *   "startTime": "2025-12-05T10:00:00-05:00",
         *   "endTime": "2025-12-05T11:00:00-05:00",
         *   "status": "PENDING",
         *   "price": 50000.00,
         *   "currency": "COP",
         *   "userNotes": "I need to discuss my project",
         *   "createdAt": "2025-12-03T19:45:00-05:00"
         * }
         * </pre>
         * 
         * <p>
         * <strong>Error responses:</strong>
         * </p>
         * <ul>
         * <li>400 Bad Request - Invalid input (validation errors)</li>
         * <li>409 Conflict - Time slot not available</li>
         * <li>500 Internal Server Error - Unexpected error</li>
         * </ul>
         * 
         * @param request The booking request
         * @return ResponseEntity with the created appointment
         */
        @Operation(
                summary = "Book a new appointment",
                description = "Creates a new appointment booking for a user with a specific service and employee"
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "201", description = "Appointment created successfully",
                        content = @Content(schema = @Schema(implementation = AppointmentResponse.class))),
                @ApiResponse(responseCode = "400", description = "Invalid request data"),
                @ApiResponse(responseCode = "409", description = "Time slot or employee not available"),
                @ApiResponse(responseCode = "401", description = "Not authenticated")
        })
        @PostMapping
        public ResponseEntity<AppointmentResponse> bookAppointment(
                        @Valid @RequestBody BookAppointmentRequest request) {
                log.info("Received booking request for project {} by user {}",
                                request.projectId(),
                                request.userId());

                // Map DTO to domain command
                BookAppointmentCommand command = new BookAppointmentCommand(
                                request.projectId(),
                                request.userId(),
                                request.serviceId(),
                                request.employeeId(),
                                request.startTime(),
                                request.endTime(),
                                request.price(),
                                request.currency(),
                                request.userNotes());

                // Execute use case
                Appointment appointment = bookAppointmentUseCase.execute(command);

                // Map domain to response DTO
                AppointmentResponse response = AppointmentResponse.from(appointment);

                log.info("Appointment {} created successfully", appointment.getId());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(response);
        }

        /**
         * Gets an appointment by ID.
         * 
         * @param id The appointment ID
         * @return The appointment details
         */
        @Operation(
                summary = "Get appointment by ID",
                description = "Retrieves the details of a specific appointment"
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Appointment found"),
                @ApiResponse(responseCode = "404", description = "Appointment not found")
        })
        @GetMapping("/{id}")
        public ResponseEntity<AppointmentResponse> getAppointment(
                        @Parameter(description = "Appointment ID") @PathVariable Long id) {
                Appointment appointment = getAppointmentUseCase.execute(id);
                return ResponseEntity.ok(AppointmentResponse.from(appointment));
        }

        /**
         * Cancels an appointment.
         * 
         * @param id      The appointment ID
         * @param request The cancellation request (reason)
         * @return The cancelled appointment details
         */
        @Operation(
                summary = "Cancel an appointment",
                description = "Cancels an existing appointment with an optional reason"
        )
        @ApiResponses(value = {
                @ApiResponse(responseCode = "200", description = "Appointment cancelled successfully"),
                @ApiResponse(responseCode = "404", description = "Appointment not found"),
                @ApiResponse(responseCode = "422", description = "Cannot cancel appointment (already completed or cancelled)")
        })
        @DeleteMapping("/{id}")
        public ResponseEntity<AppointmentResponse> cancelAppointment(
                        @Parameter(description = "Appointment ID") @PathVariable Long id,
                        @Valid @RequestBody(required = false) CancelAppointmentRequest request) {

                String reason = (request != null) ? request.reason() : null;

                CancelAppointmentCommand command = new CancelAppointmentCommand(id, reason);
                Appointment appointment = cancelAppointmentUseCase.execute(command);

                return ResponseEntity.ok(AppointmentResponse.from(appointment));
        }

        /**
         * Lists appointments with optional filters.
         * 
         * <p>
         * Supported filters:
         * </p>
         * <ul>
         * <li>userId: List appointments for a user</li>
         * <li>projectId: List appointments for a project</li>
         * <li>projectId + status: List appointments for a project with specific
         * status</li>
         * </ul>
         * 
         * @param userId    Optional user ID
         * @param projectId Optional project ID
         * @param status    Optional status
         * @return List of appointments
         */
        @Operation(
                summary = "List appointments",
                description = "Lists appointments with optional filters by user, project, or status. Returns empty list if no filters provided."
        )
        @ApiResponse(responseCode = "200", description = "List of appointments returned")
        @GetMapping
        public ResponseEntity<List<AppointmentResponse>> listAppointments(
                        @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,
                        @Parameter(description = "Filter by project ID") @RequestParam(required = false) UUID projectId,
                        @Parameter(description = "Filter by appointment status") @RequestParam(required = false) AppointmentStatus status) {

                List<Appointment> appointments;

                if (userId != null) {
                        appointments = listAppointmentsUseCase.executeByUser(userId);
                } else if (projectId != null) {
                        if (status != null) {
                                appointments = listAppointmentsUseCase.executeByProjectAndStatus(projectId, status);
                        } else {
                                appointments = listAppointmentsUseCase.executeByProject(projectId);
                        }
                } else {
                        // If no filters, return empty list or throw error depending on requirements.
                        // For now, returning empty list to avoid exposing all DB.
                        return ResponseEntity.ok(List.of());
                }

                List<AppointmentResponse> response = appointments.stream()
                                .map(AppointmentResponse::from)
                                .toList();

                return ResponseEntity.ok(response);
        }

        /**
         * Gets available time slots for a specific employee on a specific date.
         * 
         * <p>
         * <strong>Endpoint:</strong> GET
         * /api/v1/appointments/employees/{employeeId}/available-slots?projectId={projectId}&date=2025-12-15
         * </p>
         * 
         * <p>
         * This endpoint calculates availability at the EMPLOYEE level:
         * </p>
         * <ul>
         * <li>Uses project-level schedule configuration (business hours)</li>
         * <li>Filters out slots where the specific employee already has
         * appointments</li>
         * <li>Allows multiple employees to have appointments at the same time</li>
         * </ul>
         * 
         * @param employeeId Employee identifier
         * @param projectId  Project identifier (to get schedule configuration)
         * @param date       The date to check availability (format: yyyy-MM-dd)
         * @return List of available time slots for this employee
         */
        @Operation(
                summary = "Get employee available slots",
                description = "Returns available time slots for a specific employee on a given date based on project schedule"
        )
        @ApiResponse(responseCode = "200", description = "Available time slots returned")
        @GetMapping("/employees/{employeeId}/available-slots")
        public ResponseEntity<AvailableSlotsResponse> getEmployeeAvailableSlots(
                        @Parameter(description = "Employee UUID") @PathVariable UUID employeeId,
                        @Parameter(description = "Project UUID for schedule configuration") @RequestParam UUID projectId,
                        @Parameter(description = "Date to check availability (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

                log.info("Getting available slots for employee {} on date {}", employeeId, date);

                List<TimeSlot> availableSlots = getAvailableSlotsUseCase.execute(projectId, employeeId, date);
                AvailableSlotsResponse response = AvailableSlotsResponse.from(date, availableSlots);

                return ResponseEntity.ok(response);
        }

        /**
         * Gets working hours for a project on a specific date.
         * 
         * <p>
         * <strong>Endpoint:</strong> GET
         * /api/v1/appointments/projects/{projectId}/working-hours?date=2025-12-12
         * </p>
         * 
         * <p>
         * Returns the business hours (opening/closing times) for the project on a
         * specific date.
         * Useful for displaying business hours to users before they select appointment
         * times.
         * </p>
         * 
         * <p>
         * <strong>Response examples:</strong>
         * </p>
         * 
         * <pre>
         * // Business is open
         * {
         *   "date": "2025-12-12",
         *   "openingTime": "09:00:00",
         *   "closingTime": "18:00:00",
         *   "isOpen": true
         * }
         * 
         * // Business is closed (weekend, holiday, etc.)
         * {
         *   "date": "2025-12-14",
         *   "openingTime": null,
         *   "closingTime": null,
         *   "isOpen": false
         * }
         * </pre>
         * 
         * @param projectId Project identifier
         * @param date      The date to check (format: yyyy-MM-dd)
         * @return Working hours information
         */
        @Operation(
                summary = "Get project working hours",
                description = "Returns business hours (opening/closing times) for a project on a specific date"
        )
        @ApiResponse(responseCode = "200", description = "Working hours information returned")
        @GetMapping("/projects/{projectId}/working-hours")
        public ResponseEntity<WorkingHoursResponse> getProjectWorkingHours(
                        @Parameter(description = "Project UUID") @PathVariable UUID projectId,
                        @Parameter(description = "Date to check (yyyy-MM-dd)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

                log.info("Getting working hours for project {} on date {}", projectId, date);

                WorkingHoursInfo info = getProjectWorkingHoursUseCase.execute(projectId, date);

                WorkingHoursResponse response;
                if (info.getOpeningTime() != null && info.getClosingTime() != null) {
                        // Day has working hours configured
                        response = new WorkingHoursResponse(
                                        date.toString(),
                                        info.getOpeningTime().toString(),
                                        info.getClosingTime().toString(),
                                        info.isOpen());
                } else {
                        // Day is closed (no working hours)
                        response = WorkingHoursResponse.closed(date.toString());
                }

                return ResponseEntity.ok(response);
        }

        /**
         * 
         * Exception handler for TimeSlotNotAvailableException.
         * 
         * <p>
         * Returns 409 Conflict when the requested time slot is already booked.
         * </p>
         * 
         * @param ex The exception
         * @return ResponseEntity with error details
         */
        @ExceptionHandler(TimeSlotNotAvailableException.class)
        public ResponseEntity<ErrorResponse> handleTimeSlotNotAvailable(
                        TimeSlotNotAvailableException ex) {
                log.warn("Time slot not available: {}", ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                "TIME_SLOT_NOT_AVAILABLE",
                                ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(error);
        }

        /**
         * Exception handler for AppointmentNotFoundException.
         */
        @ExceptionHandler(AppointmentNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleAppointmentNotFound(
                        AppointmentNotFoundException ex) {
                log.warn("Appointment not found: {}", ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                "APPOINTMENT_NOT_FOUND",
                                ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(error);
        }

        /**
         * Exception handler for IllegalStateException (domain errors).
         */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ErrorResponse> handleIllegalState(
                        IllegalStateException ex) {
                log.warn("Domain error: {}", ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                "DOMAIN_ERROR",
                                ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                                .body(error);
        }

        /**
         * Exception handler for EmployeeNotAvailableException.
         * 
         * <p>
         * Returns 409 Conflict when the employee is not available for the requested
         * time slot.
         * </p>
         * 
         * @param ex The exception
         * @return ResponseEntity with error details
         */
        @ExceptionHandler(EmployeeNotAvailableException.class)
        public ResponseEntity<ErrorResponse> handleEmployeeNotAvailable(
                        EmployeeNotAvailableException ex) {
                log.warn("Employee not available: {}", ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                "EMPLOYEE_NOT_AVAILABLE",
                                ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(error);
        }

        /**
         * Exception handler for IllegalArgumentException.
         * 
         * <p>
         * Returns 400 Bad Request for business validation errors.
         * </p>
         * 
         * @param ex The exception
         * @return ResponseEntity with error details
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(
                        IllegalArgumentException ex) {
                log.warn("Invalid argument: {}", ex.getMessage());

                ErrorResponse error = new ErrorResponse(
                                "INVALID_ARGUMENT",
                                ex.getMessage());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(error);
        }

        /**
         * Exception handler for unexpected errors.
         * 
         * <p>
         * Returns 500 Internal Server Error for any unhandled exception.
         * </p>
         * 
         * @param ex The exception
         * @return ResponseEntity with error details
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
                log.error("Unexpected error occurred", ex);

                ErrorResponse error = new ErrorResponse(
                                "INTERNAL_ERROR",
                                "An unexpected error occurred. Please try again later.");

                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(error);
        }

        /**
         * Gets all appointments for a user (history).
         * 
         * <p>
         * <strong>Endpoint:</strong> GET
         * /api/v1/appointments/my/history?userId={userId}
         * </p>
         * 
         * @param userId The user ID
         * @return List of all user's appointments
         */
        @GetMapping("/my/history")
        public ResponseEntity<List<AppointmentResponse>> getMyAppointments(
                        @RequestParam UUID userId) {

                log.info("Getting appointment history for user: {}", userId);

                List<Appointment> appointments = getUserAppointmentsUseCase.execute(userId);

                List<AppointmentResponse> response = appointments.stream()
                                .map(AppointmentResponse::from)
                                .toList();

                return ResponseEntity.ok(response);
        }

        /**
         * Gets active (pending) appointments for a user.
         * 
         * <p>
         * <strong>Endpoint:</strong> GET /api/v1/appointments/my/active?userId={userId}
         * </p>
         * 
         * @param userId The user ID
         * @return List of user's pending appointments
         */
        @GetMapping("/my/active")
        public ResponseEntity<List<AppointmentResponse>> getMyActiveAppointments(
                        @RequestParam UUID userId) {

                log.info("Getting active appointments for user: {}", userId);

                List<Appointment> appointments = getUserActiveAppointmentsUseCase.execute(userId);

                List<AppointmentResponse> response = appointments.stream()
                                .map(AppointmentResponse::from)
                                .toList();

                return ResponseEntity.ok(response);
        }
}
