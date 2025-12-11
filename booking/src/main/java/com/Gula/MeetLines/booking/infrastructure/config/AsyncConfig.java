package com.Gula.MeetLines.booking.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for asynchronous processing and scheduling.
 * 
 * <p>
 * This enables:
 * </p>
 * <ul>
 * <li>@Async: Methods run in a separate thread</li>
 * <li>@Scheduled: Methods run periodically (cron jobs)</li>
 * </ul>
 * 
 * @author MeetLines Team
 * @version 1.0
 * @since 2025-12-03
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Default configuration is sufficient for now
    // Future: Configure custom ThreadPoolTaskExecutor for better control
}
