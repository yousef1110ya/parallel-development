package com.ecommerce.ecommerce.batch.controller;

import com.ecommerce.ecommerce.batch.scheduler.DailySalesScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/batch")
//@RequiredArgsConstructor
public class BatchController {

    private final DailySalesScheduler dailySalesScheduler;

    public BatchController(DailySalesScheduler dailySalesScheduler) {
        this.dailySalesScheduler = dailySalesScheduler;
    }

    // POST /api/batch/daily-sales — admin only
    @PostMapping("/daily-sales")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> triggerDailySales() {
        dailySalesScheduler.launchJob();
        return ResponseEntity.accepted()
                .body(Map.of("message", "Daily sales batch job triggered successfully"));
    }
}