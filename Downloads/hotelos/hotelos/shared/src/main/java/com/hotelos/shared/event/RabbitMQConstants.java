package com.hotelos.shared.event;

/**
 * HotelOS - RabbitMQ topology constants.
 *
 * All services import this class to ensure consistent
 * exchange/routing key names — no magic strings scattered in code.
 *
 * Topology:
 *   Exchange: hotelos.events (topic exchange)
 *   Routing keys follow pattern: <domain>.<action>
 */
public final class RabbitMQConstants {

    // Exchange
    public static final String EXCHANGE = "hotelos.events";

    // --- Routing Keys (published by Reception) ---
    public static final String ROOM_VACATED           = "room.vacated";
    public static final String GUEST_CHECKED_IN       = "guest.checkedin";
    public static final String GUEST_CHECKED_OUT      = "guest.checkedout";

    // --- Routing Keys (published by Housekeeping) ---
    public static final String ROOM_STATUS_CHANGED    = "room.status.changed";

    // --- Routing Keys (published by Room Service) ---
    public static final String ORDER_STATUS_CHANGED   = "order.status.changed";

    // --- Routing Keys (published by Maintenance) ---
    public static final String ISSUE_STATUS_CHANGED   = "issue.status.changed";

    // --- Queue Names ---
    public static final String QUEUE_HOUSEKEEPING     = "hotelos.housekeeping.queue";
    public static final String QUEUE_DASHBOARD        = "hotelos.dashboard.queue";

    // Prevent instantiation
    private RabbitMQConstants() {}
}
