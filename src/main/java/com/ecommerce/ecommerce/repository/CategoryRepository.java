package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}