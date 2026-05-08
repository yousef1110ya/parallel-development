package com.ecommerce.ecommerce.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties.Producer;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "category")
    private List<Product> products;
}