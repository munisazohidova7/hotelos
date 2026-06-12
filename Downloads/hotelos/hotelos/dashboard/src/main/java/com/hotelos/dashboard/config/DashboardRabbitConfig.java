package com.hotelos.dashboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hotelos.shared.event.RabbitMQConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HotelOS - RabbitMQ configuration for Dashboard Service.
 *
 * Dashboard SUBSCRIBES to ALL event types using # wildcard:
 *   Binding: hotelos.events  --  #  -->  hotelos.dashboard.queue
 *   '#' matches any routing key, so every event reaches the dashboard.
 *
 * Dashboard does NOT publish any events.
 */
@Configuration
public class DashboardRabbitConfig {

    @Bean
    public TopicExchange hotelExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue dashboardQueue() {
        return new Queue(RabbitMQConstants.QUEUE_DASHBOARD, true);
    }

    /**
     * Wildcard binding: '#' matches ALL routing keys.
     * Dashboard receives every event published by any service.
     */
    @Bean
    public Binding dashboardBinding(Queue dashboardQueue, TopicExchange hotelExchange) {
        return BindingBuilder.bind(dashboardQueue)
                .to(hotelExchange)
                .with("#"); // Match all routing keys
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter(objectMapper());
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}
