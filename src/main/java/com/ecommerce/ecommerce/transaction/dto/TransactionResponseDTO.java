package com.ecommerce.ecommerce.transaction.dto;

import com.ecommerce.ecommerce.transaction.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponseDTO {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private LocalDateTime createdAt;

    public TransactionResponseDTO(Long id, BigDecimal amount,
                                   TransactionType type, LocalDateTime createdAt) {
        this.id = id;
        this.amount = amount;
        this.type = type;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }
    public TransactionType getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}