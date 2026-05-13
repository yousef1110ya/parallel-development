package com.ecommerce.ecommerce.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
        SELECT oi.product.category.name, oi.product.name,
               SUM(oi.quantity) as totalSold
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.status = 'SUCCESS'
        GROUP BY oi.product.category.name, oi.product.name
        ORDER BY oi.product.category.name, totalSold DESC
    """)
    List<Object[]> findBestSellersByCategory();
}