package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleItemRepository extends JpaRepository<SaleItem, Long> {
}