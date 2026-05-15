package com.ecommerce.ecommerce.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Override
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findAll();

    @Override
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    java.util.Optional<Order> findById(Long id);

    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findByUserId(Long userId);
}
