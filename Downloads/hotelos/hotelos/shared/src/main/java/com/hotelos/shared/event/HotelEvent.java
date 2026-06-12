package com.hotelos.shared.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * HotelOS - All domain events exchanged via RabbitMQ.
 *
 * Event flow:
 *   Reception  --> ROOM_VACATED       --> Housekeeping
 *   Housekeeping -> ROOM_STATUS_CHANGED -> Dashboard
 *   RoomService --> ORDER_STATUS_CHANGED -> Dashboard
 *   Maintenance --> ISSUE_STATUS_CHANGED -> Dashboard
 *   Reception  --> GUEST_CHECKED_IN   --> Dashboard
 */
public class HotelEvent {

    public enum EventType {
        ROOM_VACATED,           // Guest checked out; room needs cleaning
        ROOM_STATUS_CHANGED,    // Room status update (DIRTY->CLEANING->CLEAN)
        ORDER_STATUS_CHANGED,   // Room service order status update
        ISSUE_STATUS_CHANGED,   // Maintenance issue status update
        GUEST_CHECKED_IN,       // New guest assigned to a room
        GUEST_CHECKED_OUT       // Guest fully checked out with bill
    }

    private EventType eventType;
    private String sourceService;   // Which microservice published this
    private String payload;         // JSON payload (serialized data)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public HotelEvent() {}

    public HotelEvent(EventType eventType, String sourceService, String payload) {
        this.eventType = eventType;
        this.sourceService = sourceService;
        this.payload = payload;
        this.timestamp = LocalDateTime.now();
    }

    public EventType getEventType() { return eventType; }
    public void setEventType(EventType eventType) { this.eventType = eventType; }

    public String getSourceService() { return sourceService; }
    public void setSourceService(String sourceService) { this.sourceService = sourceService; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "HotelEvent{type=" + eventType + ", from=" + sourceService + ", at=" + timestamp + "}";
    }
}
