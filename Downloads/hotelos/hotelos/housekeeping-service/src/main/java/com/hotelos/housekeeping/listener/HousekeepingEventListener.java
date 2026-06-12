package com.hotelos.housekeeping.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelos.housekeeping.service.HousekeepingService;
import com.hotelos.shared.event.HotelEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * HotelOS - Housekeeping Event Listener.
 *
 * EVENT-DRIVEN PROGRAMMING EXAMPLE (LO2 / Task 2.4):
 *
 *   This class demonstrates the subscriber side of the pub/sub pattern.
 *   When Reception publishes a ROOM_VACATED event to RabbitMQ:
 *     1. RabbitMQ delivers the message to QUEUE_HOUSEKEEPING
 *     2. @RabbitListener triggers onRoomVacated()
 *     3. Housekeeping adds the room to its cleaning queue
 *
 *   Reception has NO knowledge of this class. Decoupling is complete.
 *   If Housekeeping goes offline, messages queue up and are processed on restart.
 */
@Component
public class HousekeepingEventListener {

    private final HousekeepingService housekeepingService;
    private final ObjectMapper objectMapper;

    public HousekeepingEventListener(HousekeepingService housekeepingService,
                                     ObjectMapper objectMapper) {
        this.housekeepingService = housekeepingService;
        this.objectMapper = objectMapper;
    }

    /**
     * EVENT HANDLER: triggered when a guest checks out.
     * Reception publishes → RabbitMQ delivers → This method handles.
     *
     * @param event The ROOM_VACATED event from Reception Service
     */
    @RabbitListener(queues = "#{@housekeepingQueue.name}")
    public void onRoomVacated(HotelEvent event) {
        System.out.println("[Housekeeping] Received event: " + event);

        try {
            // Only process ROOM_VACATED events (queue may receive other types)
            if (event.getEventType() != HotelEvent.EventType.ROOM_VACATED) {
                return;
            }

            // Parse payload
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(event.getPayload(), Map.class);
            int roomNumber = (Integer) payload.get("roomNumber");
            int floor = (Integer) payload.get("floor");

            // Add to cleaning queue
            housekeepingService.addToCleaningQueue(roomNumber, floor);
            System.out.println("[Housekeeping] Room " + roomNumber + " added to cleaning queue.");

        } catch (Exception e) {
            // Log error; do NOT re-throw (prevents infinite retry loop)
            System.err.println("[Housekeeping] Error processing event: " + e.getMessage());
        }
    }
}
