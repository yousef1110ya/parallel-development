package com.ecommerce.ecommerce.batch.reader;

import com.ecommerce.ecommerce.order.Order;
import com.ecommerce.ecommerce.order.OrderRepository;
import com.ecommerce.ecommerce.order.OrderStatus;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SalesItemReader {

    private final OrderRepository orderRepository;

    public SalesItemReader(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public ListItemReader<Order> createReader() {
        LocalDateTime from = LocalDateTime.now().minusHours(24);
        LocalDateTime to   = LocalDateTime.now();

        // JOIN FETCH ensures user and items are loaded eagerly
        // so no lazy loading is needed in the processor
        List<Order> orders = orderRepository
                .findByStatusAndCreatedAtBetweenWithDetails(
                    OrderStatus.SUCCESS, from, to
                );

        return new ListItemReader<>(orders);
    }
}