package com.ecommerce.ecommerce.batch.listener;

import com.ecommerce.ecommerce.batch.entity.DailySalesSummary;
import com.ecommerce.ecommerce.batch.repository.DailySalesSummaryRepository;
import com.ecommerce.ecommerce.report.EmailService;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class DailySalesJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(DailySalesJobListener.class);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DailySalesSummaryRepository summaryRepository;
    private final EmailService emailService;

    @Value("${app.report.recipient}")
    private String reportRecipient;

    public DailySalesJobListener(DailySalesSummaryRepository summaryRepository,
                                  EmailService emailService) {
        this.summaryRepository = summaryRepository;
        this.emailService = emailService;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Daily sales job started at {}", LocalDateTime.now());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("Daily sales job completed — building full CSV and sending email");

            // Fetch all summaries saved today by this job
            List<DailySalesSummary> summaries =
                    summaryRepository.findBySaleDate(LocalDate.now());

            if (summaries.isEmpty()) {
                log.info("No sales found for today — skipping email");
                return;
            }

            byte[] csv = buildCsv(summaries);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            String filename = "daily_sales_" + timestamp + ".csv";

            emailService.sendEmailWithAttachment(
                    reportRecipient,
                    "Daily Sales Summary Report",
                    "Please find attached the daily sales summary.\n" +
                    "Total orders processed: " + summaries.size() + "\n" +
                    "Generated at: " + LocalDateTime.now().format(FORMATTER),
                    csv,
                    filename
            );

            log.info("Daily sales email sent with {} records", summaries.size());

        } else {
            log.warn("Daily sales job ended with status: {}", jobExecution.getStatus());
        }
    }

    private byte[] buildCsv(List<DailySalesSummary> summaries) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {

            writer.writeNext(new String[]{
                "Order ID", "User ID", "User Email",
                "Order Total", "Item Count", "Sale Date", "Processed At"
            });

            for (DailySalesSummary s : summaries) {
                writer.writeNext(new String[]{
                    String.valueOf(s.getOrderId()),
                    String.valueOf(s.getUserId()),
                    s.getUserEmail(),
                    s.getOrderTotal().toString(),
                    String.valueOf(s.getItemCount()),
                    s.getSaleDate().toString(),
                    s.getProcessedAt().format(FORMATTER)
                });
            }

            writer.flush();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to build sales CSV: " + e.getMessage());
        }
    }
}