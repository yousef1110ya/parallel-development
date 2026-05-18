package com.ecommerce.ecommerce.cart.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private Long orderId;
    private Long userId;
    private String userEmail;
    private BigDecimal totalPrice;
    private LocalDateTime placedAt;
}