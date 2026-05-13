package com.ecommerce.ecommerce.report;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // POST /api/reports/transactions/weekly
    @PostMapping("/transactions/weekly")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendWeeklyTransactionsReport() {
        reportService.sendWeeklyTransactionsReport();
        return ResponseEntity.ok(Map.of("message",
                "Weekly transactions report sent successfully"));
    }

    // POST /api/reports/products/best-sellers
    @PostMapping("/products/best-sellers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendBestSellersReport() {
        reportService.sendBestSellersReport();
        return ResponseEntity.ok(Map.of("message",
                "Best sellers report sent successfully"));
    }
}