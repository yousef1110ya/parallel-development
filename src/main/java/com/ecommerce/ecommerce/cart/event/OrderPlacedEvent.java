package com.ecommerce.ecommerce.cart.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderPlacedEvent {

    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal totalPrice;
    private LocalDateTime placedAt;

    public OrderPlacedEvent() {}

    public OrderPlacedEvent(Long orderId, Long userId, String userEmail,
                             BigDecimal totalPrice, LocalDateTime placedAt) {
        this.orderId = orderId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.totalPrice = totalPrice;
        this.placedAt = placedAt;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getPlacedAt() { return placedAt; }
    public void setPlacedAt(LocalDateTime placedAt) { this.placedAt = placedAt; }
}