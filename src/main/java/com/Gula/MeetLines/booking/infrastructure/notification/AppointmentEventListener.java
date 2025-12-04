package com.Gula.MeetLines.booking.infrastructure.notification;

import com.Gula.MeetLines.booking.domain.events.AppointmentBookedEvent;
import com.Gula.MeetLines.booking.domain.events.AppointmentCancelledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener that handles appointment events and triggers notifications via
 * n8n.
 * 
 * <p>
 * This is an <strong>Event Listener</strong> in event-driven architecture,
 * which means:
 * </p>
 * <ul>
 * <li>It's in the INFRASTRUCTURE layer (not domain)</li>
 * <li>It reacts to domain events (AppointmentBookedEvent)</li>
 * <li>It handles side effects (sending notifications)</li>
 * <li>It's decoupled from the domain (domain doesn't know about this)</li>
 * </ul>
 * 
 * <p>
 * <strong>Why use event listeners?</strong>
 * </p>
 * <ul>
 * <li><strong>Decoupling:</strong> Domain doesn't know about n8n, email, SMS,
 * etc.</li>
 * <li><strong>Async processing:</strong> Notifications don't block the main
 * flow</li>
 * <li><strong>Extensibility:</strong> Easy to add more listeners without
 * changing domain</li>
 * <li><strong>Resilience:</strong> If notification fails, appointment is still
 * created</li>
 * </ul>
 * 
 * <p>
 * <strong>Event flow:</strong>
 * </p>
 * 
 * <pre>
 *     1. BookAppointmentUseCase creates appointment
 *     2. UseCase publishes AppointmentBookedEvent
 *     3. Spring routes event to this listener
 *     4. Listener calls n8n webhook (async)
 *     5. n8n sends notification to user
 * </pre>
 * 
 * <p>
 * <strong>Async processing:</strong>
 * </p>
 * <p>
 * The @Async annotation makes this run in a separate thread, so:
 * </p>
 * <ul>
 * <li>The API responds immediately (doesn't wait for n8n)</li>
 * <li>If n8n is slow, it doesn't affect user experience</li>
 * <li>If n8n fails, the appointment is still created</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventListener {

    /**
     * Client for calling n8n webhooks.
     * This will be injected when you create the N8nClient class.
     * 
     * For now, we'll leave a placeholder comment.
     * TODO: Inject N8nClient when webhook URL is configured
     */
    // private final N8nClient n8nClient;

    /**
     * Handles the AppointmentBookedEvent.
     * 
     * <p>
     * This method is called automatically by Spring when an AppointmentBookedEvent
     * is published via ApplicationEventPublisher.
     * </p>
     * 
     * <p>
     * <strong>What happens here:</strong>
     * </p>
     * <ol>
     * <li>Log the event for debugging</li>
     * <li>Call n8n webhook with appointment data</li>
     * <li>n8n workflow sends notification to user</li>
     * <li>n8n can also schedule reminder notifications</li>
     * </ol>
     * 
     * <p>
     * <strong>Error handling:</strong>
     * </p>
     * <p>
     * If n8n call fails, we log the error but don't throw an exception.
     * This ensures the appointment creation succeeds even if notifications fail.
     * </p>
     * 
     * <p>
     * <strong>Example n8n payload:</strong>
     * </p>
     * 
     * <pre>
     * {
     *   "appointmentId": 123,
     *   "userId": "uuid-here",
     *   "projectId": "uuid-here",
     *   "startTime": "2025-12-05T10:00:00Z",
     *   "endTime": "2025-12-05T11:00:00Z",
     *   "eventType": "APPOINTMENT_BOOKED"
     * }
     * </pre>
     * 
     * @param event The AppointmentBookedEvent
     */
    @EventListener
    @Async
    public void handleAppointmentBooked(AppointmentBookedEvent event) {
        log.info("Handling AppointmentBookedEvent: {}", event);

        try {
            // TODO: Uncomment when N8nClient is implemented and webhook URL is configured
            /*
             * N8nNotificationRequest request = N8nNotificationRequest.builder()
             * .appointmentId(event.getAppointmentId())
             * .userId(event.getUserId())
             * .projectId(event.getProjectId())
             * .startTime(event.getStartTime())
             * .endTime(event.getEndTime())
             * .eventType("APPOINTMENT_BOOKED")
             * .build();
             * 
             * n8nClient.sendNotification(request);
             */

            // Placeholder: Log what would be sent to n8n
            log.info("Would send notification to n8n for appointment {} booked by user {}",
                    event.getAppointmentId(),
                    event.getUserId());

            log.info("Notification details: startTime={}, endTime={}",
                    event.getStartTime(),
                    event.getEndTime());

        } catch (Exception e) {
            // Log error but don't throw exception
            // This ensures appointment creation succeeds even if notification fails
            log.error("Failed to send notification to n8n for appointment {}: {}",
                    event.getAppointmentId(),
                    e.getMessage(),
                    e);
        }
    }

    /**
     * Future: Handle AppointmentStartedEvent.
     * 
     * <p>
     * This would be triggered when an appointment transitions to IN_PROGRESS.
     * </p>
     * <p>
     * n8n could send: "Your appointment has started"
     * </p>
     * 
     * @param event The AppointmentStartedEvent
     */
    // @EventListener
    // @Async
    // public void handleAppointmentStarted(AppointmentStartedEvent event) {
    // log.info("Handling AppointmentStartedEvent: {}", event);
    // // TODO: Send "appointment started" notification via n8n
    // }

    /**
     * Future: Handle AppointmentCompletedEvent.
     * 
     * <p>
     * This would be triggered when an appointment is completed.
     * </p>
     * <p>
     * n8n could send: "Thank you for your appointment. Please rate your
     * experience."
     * </p>
     * 
     * @param event The AppointmentCompletedEvent
     */
    // @EventListener
    // @Async
    // public void handleAppointmentCompleted(AppointmentCompletedEvent event) {
    // log.info("Handling AppointmentCompletedEvent: {}", event);
    // // TODO: Send "thank you" notification via n8n
    // }

    /**
     * Future: Handle AppointmentCancelledEvent.
     * 
     * <p>
     * This would be triggered when an appointment is cancelled.
     * </p>
     * <p>
     * Handles the AppointmentCancelledEvent.
     * 
     * <p>
     * This is triggered when an appointment is cancelled.
     * </p>
     * <p>
     * n8n should send: "Your appointment has been cancelled."
     * </p>
     * 
     * @param event The AppointmentCancelledEvent
     */
    @EventListener
    @Async
    public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
        log.info("Handling AppointmentCancelledEvent: {}", event);

        try {
            // TODO: Uncomment when N8nClient is implemented
            /*
             * N8nNotificationRequest request = N8nNotificationRequest.builder()
             * .appointmentId(event.getAppointmentId())
             * .userId(event.getUserId())
             * .projectId(event.getProjectId())
             * .startTime(event.getStartTime())
             * .endTime(event.getEndTime())
             * .eventType("APPOINTMENT_CANCELLED")
             * .reason(event.getCancellationReason())
             * .build();
             * 
             * n8nClient.sendNotification(request);
             */

            log.info("Would send CANCELLATION notification to n8n for appointment {} cancelled by user {}",
                    event.getAppointmentId(),
                    event.getUserId());

            log.info("Cancellation reason: {}", event.getCancellationReason());

        } catch (Exception e) {
            log.error("Failed to send cancellation notification to n8n for appointment {}: {}",
                    event.getAppointmentId(),
                    e.getMessage(),
                    e);
        }
    }
}
