package com.ecommerce.ecommerce.repository;

import com.ecommerce.ecommerce.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}