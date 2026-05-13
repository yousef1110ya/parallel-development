package com.ecommerce.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutResponseDTO {

    private Long orderId;
    private BigDecimal totalCharged;
    private List<String> skippedItems;
    private String message;

    public CheckoutResponseDTO(Long orderId, BigDecimal totalCharged,
                                List<String> skippedItems, String message) {
        this.orderId = orderId;
        this.totalCharged = totalCharged;
        this.skippedItems = skippedItems;
        this.message = message;
    }

    public Long getOrderId() { return orderId; }
    public BigDecimal getTotalCharged() { return totalCharged; }
    public List<String> getSkippedItems() { return skippedItems; }
    public String getMessage() { return message; }
}