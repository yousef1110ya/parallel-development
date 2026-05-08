package com.ecommerce.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String brand;
    private String specifications;
    private double price;
    private int quantity;


    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @Version
    private Integer version;//1

}