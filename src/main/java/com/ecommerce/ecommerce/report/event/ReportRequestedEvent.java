package com.ecommerce.ecommerce.report.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestedEvent {
    // "WEEKLY_TRANSACTIONS" or "BEST_SELLERS"
    private String reportType;
    private LocalDateTime requestedAt;
}