package com.ecommerce.ecommerce.users.dto;


import com.ecommerce.ecommerce.users.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private BigDecimal balance;
    private LocalDateTime createdAt;

    public UserResponseDTO(Long id, String name, String email, UserRole role, BigDecimal balance, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public BigDecimal getBalance() { return balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}