package com.hotelos.reception.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelos.shared.event.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HotelOS - RabbitMQ configuration for Reception Service.
 *
 * Topology:
 *   Exchange type: Topic (allows wildcard routing patterns)
 *   Reception only PUBLISHES — it does not subscribe to any queue.
 *   This enforces the microservice principle: Reception doesn't know
 *   who listens to its events.
 */
@Configuration
public class RabbitMQConfig {

    /**
     * Declare the shared topic exchange.
     * All microservices bind their queues to this exchange.
     * durable=true: survives broker restart.
     */
    @Bean
    public TopicExchange hotelExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE, true, false);
    }

    /**
     * Jackson message converter: serialize/deserialize events as JSON.
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper());
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Support LocalDateTime
        return mapper;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
