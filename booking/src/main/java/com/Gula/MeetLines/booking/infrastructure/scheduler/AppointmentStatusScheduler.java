package com.Gula.MeetLines.booking.infrastructure.scheduler;

import com.Gula.MeetLines.booking.domain.Appointment;
import com.Gula.MeetLines.booking.domain.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Scheduler for automatic appointment status updates.
 * 
 * <p>
 * This component acts as an automated agent that transitions appointments
 * through their lifecycle based on time.
 * </p>
 * 
 * <p>
 * <strong>Transitions handled:</strong>
 * </p>
 * <ul>
 * <li>PENDING → IN_PROGRESS (when start time arrives)</li>
 * <li>IN_PROGRESS → COMPLETED (when end time passes)</li>
 * </ul>
 * 
 * <p>
 * <strong>Frequency:</strong>
 * </p>
 * <p>
 * Runs every minute (60,000 ms) to ensure timely updates.
 * </p>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentStatusScheduler {

    private final AppointmentRepository appointmentRepository;

    /**
     * Updates appointment statuses based on current time.
     * 
     * <p>
     * This method is triggered automatically by Spring's scheduler.
     * </p>
     * 
     * <p>
     * <strong>Transaction management:</strong>
     * </p>
     * <p>
     * We use @Transactional to ensure that all updates in a single run
     * are committed together, or rolled back if something catastrophic happens.
     * </p>
     */
    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    @Transactional
    public void updateAppointmentStatuses() {
        ZonedDateTime now = ZonedDateTime.now();
        log.debug("Running appointment status scheduler at {}", now);

        startPendingAppointments(now);
        completeInProgressAppointments(now);
    }

    /**
     * Finds PENDING appointments that should start and starts them.
     */
    private void startPendingAppointments(ZonedDateTime now) {
        List<Appointment> toStart = appointmentRepository.findPendingAppointmentsToStart(now);

        if (!toStart.isEmpty()) {
            log.info("Found {} pending appointments to start", toStart.size());

            for (Appointment appointment : toStart) {
                try {
                    // Domain logic: start the appointment
                    // Note: In automatic mode, we don't have a meeting link, so we pass null
                    appointment.start(null);

                    // Save updated state
                    appointmentRepository.save(appointment);

                    log.info("Started appointment {}", appointment.getId());

                    // Future: Publish AppointmentStartedEvent here

                } catch (Exception e) {
                    log.error("Failed to auto-start appointment {}", appointment.getId(), e);
                }
            }
        }
    }

    /**
     * Finds IN_PROGRESS appointments that should complete and completes them.
     */
    private void completeInProgressAppointments(ZonedDateTime now) {
        List<Appointment> toComplete = appointmentRepository.findInProgressAppointmentsToComplete(now);

        if (!toComplete.isEmpty()) {
            log.info("Found {} in-progress appointments to complete", toComplete.size());

            for (Appointment appointment : toComplete) {
                try {
                    // Domain logic: complete the appointment
                    appointment.complete();

                    // Save updated state
                    appointmentRepository.save(appointment);

                    log.info("Completed appointment {}", appointment.getId());

                    // Future: Publish AppointmentCompletedEvent here

                } catch (Exception e) {
                    log.error("Failed to auto-complete appointment {}", appointment.getId(), e);
                }
            }
        }
    }
}
