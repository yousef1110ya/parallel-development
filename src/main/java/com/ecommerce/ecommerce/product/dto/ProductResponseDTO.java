package com.ecommerce.ecommerce.product.dto;


import com.ecommerce.ecommerce.category.dto.CategoryResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private CategoryResponseDTO category;
    private LocalDateTime createdAt;

    public ProductResponseDTO(Long id, String name, String description,
                               BigDecimal price, int stock,
                               CategoryResponseDTO category, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public CategoryResponseDTO getCategory() { return category; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}