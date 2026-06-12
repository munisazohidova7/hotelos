package com.hotelos.dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * HotelOS - WebSocket Configuration.
 *
 * WEBSOCKET / EVENT-DRIVEN EXAMPLE (LO2 / Task 2.4):
 *
 *   This configures STOMP over WebSocket.
 *   The browser dashboard connects to /ws endpoint.
 *   Server pushes updates to /topic/dashboard without the client polling.
 *
 *   Flow:
 *     1. RabbitMQ delivers event to Dashboard queue
 *     2. DashboardEventListener processes event
 *     3. SimpMessagingTemplate.convertAndSend("/topic/dashboard", update)
 *     4. All connected browsers receive the update INSTANTLY
 *
 *   No page refresh needed — this is the real-time requirement satisfied.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory broker for /topic
        config.enableSimpleBroker("/topic");
        // Prefix for messages FROM client TO server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Browser connects to: ws://localhost:8085/ws
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // SockJS fallback for older browsers
    }
}
