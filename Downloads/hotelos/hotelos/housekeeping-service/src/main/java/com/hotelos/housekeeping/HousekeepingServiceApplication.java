package com.hotelos.housekeeping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HotelOS - Housekeeping Service
 *
 * Responsibilities:
 *   - Subscribe to ROOM_VACATED events (from Reception via RabbitMQ)
 *   - Manage cleaning queue
 *   - Track room status: DIRTY → CLEANING → CLEAN
 *   - Publish ROOM_STATUS_CHANGED events (Dashboard subscribes)
 *
 * Port: 8082
 */
@SpringBootApplication
public class HousekeepingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HousekeepingServiceApplication.class, args);
    }
}
