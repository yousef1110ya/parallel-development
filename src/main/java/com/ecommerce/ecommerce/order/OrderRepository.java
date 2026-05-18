package com.ecommerce.ecommerce.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDateTime;
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
    
    List<Order> findByStatusAndCreatedAtBetween(
    	    OrderStatus status,
    	    LocalDateTime from,
    	    LocalDateTime to
    	);
    
    @Query("SELECT o FROM Order o JOIN FETCH o.user JOIN FETCH o.items WHERE o.status = :status AND o.createdAt BETWEEN :from AND :to")
    List<Order> findByStatusAndCreatedAtBetweenWithDetails(
        @Param("status") OrderStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
