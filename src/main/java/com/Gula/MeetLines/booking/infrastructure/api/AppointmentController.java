package com.Gula.MeetLines.booking.infrastructure.api;

import com.Gula.MeetLines.booking.application.BookAppointmentUseCase;
import com.Gula.MeetLines.booking.application.BookAppointmentUseCase.BookAppointmentCommand;
import com.Gula.MeetLines.booking.application.BookAppointmentUseCase.TimeSlotNotAvailableException;
import com.Gula.MeetLines.booking.application.CancelAppointmentUseCase;
import com.Gula.MeetLines.booking.application.CancelAppointmentUseCase.CancelAppointmentCommand;
import com.Gula.MeetLines.booking.application.GetAppointmentUseCase;
import com.Gula.MeetLines.booking.application.GetAppointmentUseCase.AppointmentNotFoundException;
import com.Gula.MeetLines.booking.application.GetAvailableSlotsUseCase;
import com.Gula.MeetLines.booking.application.ListAppointmentsUseCase;
import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentStatus;
import com.Gula.MeetLines.booking.infrastructure.api.dto.BookAppointmentRequest;
import com.Gula.MeetLines.booking.infrastructure.api.dto.CancelAppointmentRequest;
import com.Gula.MeetLines.booking.infrastructure.api.dto.AppointmentResponse;
import com.Gula.MeetLines.booking.infrastructure.api.dto.AvailableSlotsResponse;
import com.Gula.MeetLines.booking.infrastructure.api.dto.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.Gula.MeetLines.booking.domain.TimeSlot;
import org.springframework.format.annotation.DateTimeFormat;

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
public class AppointmentController {

        private final BookAppointmentUseCase bookAppointmentUseCase;
        private final CancelAppointmentUseCase cancelAppointmentUseCase;
        private final GetAppointmentUseCase getAppointmentUseCase;
        private final ListAppointmentsUseCase listAppointmentsUseCase;
        private final GetAvailableSlotsUseCase getAvailableSlotsUseCase;

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
        @GetMapping("/{id}")
        public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable Long id) {
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
        @DeleteMapping("/{id}")
        public ResponseEntity<AppointmentResponse> cancelAppointment(
                        @PathVariable Long id,
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
        @GetMapping
        public ResponseEntity<List<AppointmentResponse>> listAppointments(
                        @RequestParam(required = false) UUID userId,
                        @RequestParam(required = false) UUID projectId,
                        @RequestParam(required = false) AppointmentStatus status) {

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
         * Gets available time slots for a project on a specific date.
         * 
         * <p>
         * <strong>Endpoint:</strong> GET
         * /api/v1/projects/{projectId}/available-slots?date=2025-12-15
         * </p>
         * 
         * @param projectId The project identifier
         * @param date      The date to check availability (format: yyyy-MM-dd)
         * @return List of available time slots
         */
        @GetMapping("/projects/{projectId}/available-slots")
        public ResponseEntity<AvailableSlotsResponse> getAvailableSlots(
                        @PathVariable UUID projectId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

                log.info("Getting available slots for project {} on date {}", projectId, date);

                List<TimeSlot> availableSlots = getAvailableSlotsUseCase.execute(projectId, date);
                AvailableSlotsResponse response = AvailableSlotsResponse.from(date, availableSlots);

                return ResponseEntity.ok(response);
        }

        /**
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
}
