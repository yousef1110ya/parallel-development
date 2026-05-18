package com.ecommerce.ecommerce.batch.repository;

import com.ecommerce.ecommerce.batch.entity.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface DailySalesSummaryRepository extends JpaRepository<DailySalesSummary, Long> {
    List<DailySalesSummary> findBySaleDate(LocalDate date);
}