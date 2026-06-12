package com.hotelos.dashboard.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelos.shared.event.HotelEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HotelOS - Dashboard Event Listener + WebSocket Broadcaster.
 *
 * EVENT-DRIVEN + WEBSOCKET EXAMPLE (LO2 / Task 2.4):
 *
 *   Step 1: @RabbitListener receives events from ALL microservices.
 *   Step 2: Updates in-memory dashboard state.
 *   Step 3: Broadcasts update to ALL connected browsers via WebSocket.
 *
 *   The browser does NOT poll. It receives a push the instant something changes.
 *   This is the real-time requirement: no page refresh needed.
 *
 *   Security (Task 3.2 - Data Disclosure):
 *   The payload broadcast via WebSocket deliberately OMITS sensitive guest data
 *   (payment details, passport numbers). Only operational data is sent over the wire.
 */
@Component
public class DashboardBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    // In-memory dashboard state — rebuilt from events
    private final Map<Integer, Map<String, Object>> roomStates = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> activeOrders = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> openIssues = new ConcurrentHashMap<>();

    public DashboardBroadcaster(SimpMessagingTemplate messagingTemplate,
                                 ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        initializeRoomStates();
    }

    /**
     * Subscribes to the dashboard queue — receives ALL hotel events.
     * Routes to appropriate handler based on event type.
     */
    @RabbitListener(queues = "#{@dashboardQueue.name}")
    public void onHotelEvent(HotelEvent event) {
        System.out.println("[Dashboard] Received: " + event);
        try {
            switch (event.getEventType()) {
                case ROOM_STATUS_CHANGED -> handleRoomStatusChange(event.getPayload());
                case GUEST_CHECKED_IN    -> handleGuestCheckedIn(event.getPayload());
                case GUEST_CHECKED_OUT   -> handleGuestCheckedOut(event.getPayload());
                case ORDER_STATUS_CHANGED -> handleOrderStatusChange(event.getPayload());
                case ISSUE_STATUS_CHANGED -> handleIssueStatusChange(event.getPayload());
                default -> System.out.println("[Dashboard] Unhandled event: " + event.getEventType());
            }
        } catch (Exception e) {
            System.err.println("[Dashboard] Error processing event: " + e.getMessage());
        }
    }

    // --- Event Handlers ---

    @SuppressWarnings("unchecked")
    private void handleRoomStatusChange(String payload) throws Exception {
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        int roomNumber = (Integer) data.get("roomNumber");
        String newStatus = (String) data.get("newStatus");

        roomStates.computeIfAbsent(roomNumber, k -> new LinkedHashMap<>())
                  .put("status", newStatus);

        broadcastUpdate("ROOM_STATUS", Map.of(
                "roomNumber", roomNumber,
                "status", newStatus
                // NOTE: no guest payment data here (security requirement)
        ));
    }

    @SuppressWarnings("unchecked")
    private void handleGuestCheckedIn(String payload) throws Exception {
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        int roomNumber = (Integer) data.get("roomNumber");

        // Only broadcast operational info — NOT sensitive guest data
        Map<String, Object> roomState = roomStates.computeIfAbsent(roomNumber, k -> new LinkedHashMap<>());
        roomState.put("status", "OCCUPIED");
        roomState.put("guestName", data.get("guestName")); // name ok; no payment/passport
        roomState.put("roomType", data.get("roomType"));

        broadcastUpdate("ROOM_STATUS", Map.of(
                "roomNumber", roomNumber,
                "status", "OCCUPIED",
                "guestName", data.get("guestName"),
                "floor", data.get("floor")
        ));
    }

    @SuppressWarnings("unchecked")
    private void handleGuestCheckedOut(String payload) throws Exception {
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        int roomNumber = (Integer) data.get("roomNumber");

        Map<String, Object> roomState = roomStates.computeIfAbsent(roomNumber, k -> new LinkedHashMap<>());
        roomState.put("status", "DIRTY");
        roomState.remove("guestName");

        broadcastUpdate("ROOM_STATUS", Map.of(
                "roomNumber", roomNumber,
                "status", "DIRTY"
                // totalBill intentionally omitted from WebSocket broadcast
        ));
    }

    @SuppressWarnings("unchecked")
    private void handleOrderStatusChange(String payload) throws Exception {
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        String orderId = (String) data.get("orderId");
        String status = (String) data.get("status");

        if ("DELIVERED".equals(status)) {
            activeOrders.remove(orderId);
        } else {
            activeOrders.put(orderId, data);
        }

        broadcastUpdate("ORDER_STATUS", data);
    }

    @SuppressWarnings("unchecked")
    private void handleIssueStatusChange(String payload) throws Exception {
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);
        String issueId = (String) data.get("issueId");
        String status = (String) data.get("status");

        if ("RESOLVED".equals(status)) {
            openIssues.remove(issueId);
        } else {
            openIssues.put(issueId, data);
        }

        broadcastUpdate("ISSUE_STATUS", data);
    }

    /**
     * Broadcast a typed update to ALL connected dashboard browsers.
     * Topic: /topic/dashboard
     *
     * WEBSOCKET PUSH: browser receives this instantly, no polling needed.
     */
    private void broadcastUpdate(String updateType, Map<String, Object> data) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("type", updateType);
        message.put("data", data);
        messagingTemplate.convertAndSend("/topic/dashboard", message);
    }

    /**
     * Provide current full state snapshot when a new browser connects.
     */
    public Map<String, Object> getCurrentState() {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("rooms", roomStates);
        state.put("activeOrders", activeOrders);
        state.put("openIssues", openIssues);
        return state;
    }

    private void initializeRoomStates() {
        // Initialize 10 rooms with CLEAN status
        int[] rooms = {101, 102, 103, 104, 105, 201, 202, 203, 204, 205};
        for (int room : rooms) {
            Map<String, Object> state = new LinkedHashMap<>();
            state.put("status", "CLEAN");
            state.put("floor", room >= 200 ? 2 : 1);
            roomStates.put(room, state);
        }
    }
}
