package com.ecommerce.ecommerce.rabbitmq.publisher;

import com.ecommerce.ecommerce.rabbitmq.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(String routingKey, Object event) {
        log.info("Publishing event with routing key: {}", routingKey);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
    }
}