package com.ecommerce.ecommerce.order.dto;

import com.ecommerce.ecommerce.order.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private List<OrderItemResponseDTO> items;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public OrderResponseDTO(Long id, Long userId, String userName,
                             List<OrderItemResponseDTO> items, BigDecimal totalPrice,
                             OrderStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public List<OrderItemResponseDTO> getItems() { return items; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}