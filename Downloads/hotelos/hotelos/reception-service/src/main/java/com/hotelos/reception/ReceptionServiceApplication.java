package com.hotelos.reception;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * HotelOS - Reception Service
 *
 * Responsibilities:
 *   - Guest check-in (triggers room assignment algorithm)
 *   - Guest check-out (calculates bill, publishes ROOM_VACATED event)
 *   - Room inventory queries
 *
 * Port: 8081
 */
@SpringBootApplication
public class ReceptionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReceptionServiceApplication.class, args);
    }
}
