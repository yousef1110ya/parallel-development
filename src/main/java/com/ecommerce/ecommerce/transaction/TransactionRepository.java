package com.ecommerce.ecommerce.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserId(Long userId);
    List<Transaction> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}