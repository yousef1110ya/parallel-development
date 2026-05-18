package com.ecommerce.ecommerce.batch.processor;

import com.ecommerce.ecommerce.batch.entity.DailySalesSummary;
import com.ecommerce.ecommerce.order.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
public class SalesItemProcessor implements ItemProcessor<Order, DailySalesSummary> {

    @Override
    public DailySalesSummary process(Order order) {
        //log.info("Processing order id: {}", order.getId());

        DailySalesSummary summary = new DailySalesSummary();
        summary.setOrderId(order.getId());
        summary.setUserId(order.getUser().getId());
        summary.setUserEmail(order.getUser().getEmail());
        summary.setOrderTotal(order.getTotalPrice());
        summary.setItemCount(order.getItems().size());
        summary.setSaleDate(LocalDate.now());
        summary.setProcessedAt(LocalDateTime.now());

        return summary;
    }
}