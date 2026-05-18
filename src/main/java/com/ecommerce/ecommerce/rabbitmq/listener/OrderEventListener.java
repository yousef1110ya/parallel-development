package com.ecommerce.ecommerce.rabbitmq.listener;

import com.ecommerce.ecommerce.cart.event.OrderPlacedEvent;
import com.ecommerce.ecommerce.rabbitmq.config.RabbitMQConfig;
import com.ecommerce.ecommerce.report.EmailService;
import com.ecommerce.ecommerce.transaction.Transaction;
import com.ecommerce.ecommerce.transaction.TransactionRepository;
import com.ecommerce.ecommerce.transaction.TransactionType;
import com.ecommerce.ecommerce.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final EmailService emailNotificationService;

    public OrderEventListener(TransactionRepository transactionRepository,
                               UserRepository userRepository,
                               EmailService emailNotificationService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.emailNotificationService = emailNotificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.ORDER_PLACED_QUEUE)
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Order placed event received for orderId: {}", event.getOrderId());

        // 1. Record transaction
        userRepository.findByEmail(event.getUserEmail()).ifPresent(user -> {
            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(event.getTotalPrice());
            transaction.setType(TransactionType.PURCHASE);
            transaction.setCreatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            log.info("Transaction recorded for orderId: {}", event.getOrderId());
        });

        // 2. Send confirmation email to customer
        emailNotificationService.sendOrderConfirmation(
            event.getUserEmail(),
            event.getOrderId(),
            event.getTotalPrice().toString()
        );
    }
}