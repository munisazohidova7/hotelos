package com.hotelos.housekeeping.config;

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
 * HotelOS - RabbitMQ configuration for Housekeeping Service.
 *
 * This service:
 *   SUBSCRIBES to: room.vacated (from Reception)
 *   PUBLISHES to:  room.status.changed (to Dashboard)
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange hotelExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE, true, false);
    }

    /**
     * Dedicated queue for Housekeeping.
     * Bound to the exchange with routing key "room.vacated".
     * durable=true: messages survive broker restart.
     */
    @Bean
    public Queue housekeepingQueue() {
        return new Queue(RabbitMQConstants.QUEUE_HOUSEKEEPING, true);
    }

    /**
     * Binding: this queue receives messages with routing key "room.vacated".
     */
    @Bean
    public Binding housekeepingBinding(Queue housekeepingQueue, TopicExchange hotelExchange) {
        return BindingBuilder.bind(housekeepingQueue)
                .to(hotelExchange)
                .with(RabbitMQConstants.ROOM_VACATED);
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
