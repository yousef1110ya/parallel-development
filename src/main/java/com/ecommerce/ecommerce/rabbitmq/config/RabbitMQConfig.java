package com.ecommerce.ecommerce.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ---- Exchange ----
    public static final String EXCHANGE = "ecommerce.exchange";

    // ---- Queues ----
    public static final String ORDER_PLACED_QUEUE       = "order.placed.queue";
    public static final String REPORT_REQUESTED_QUEUE   = "report.requested.queue";
    public static final String DEPOSIT_COMPLETED_QUEUE  = "deposit.completed.queue";

    // ---- Routing Keys ----
    public static final String ORDER_PLACED_KEY      = "order.placed";
    public static final String REPORT_REQUESTED_KEY  = "report.requested";
    public static final String DEPOSIT_COMPLETED_KEY = "deposit.completed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    // ---- Queue Declarations ----
    @Bean public Queue orderPlacedQueue()      { return new Queue(ORDER_PLACED_QUEUE, true); }
    @Bean public Queue reportRequestedQueue()  { return new Queue(REPORT_REQUESTED_QUEUE, true); }
    @Bean public Queue depositCompletedQueue() { return new Queue(DEPOSIT_COMPLETED_QUEUE, true); }

    // ---- Bindings ----
    @Bean
    public Binding orderPlacedBinding(Queue orderPlacedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderPlacedQueue).to(exchange).with(ORDER_PLACED_KEY);
    }

    @Bean
    public Binding reportRequestedBinding(Queue reportRequestedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(reportRequestedQueue).to(exchange).with(REPORT_REQUESTED_KEY);
    }

    @Bean
    public Binding depositCompletedBinding(Queue depositCompletedQueue, TopicExchange exchange) {
        return BindingBuilder.bind(depositCompletedQueue).to(exchange).with(DEPOSIT_COMPLETED_KEY);
    }

    // ---- Serialization ----
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}