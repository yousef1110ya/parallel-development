package com.ecommerce.ecommerce.report;

import com.ecommerce.ecommerce.order.OrderItemRepository;
import com.ecommerce.ecommerce.transaction.Transaction;
import com.ecommerce.ecommerce.transaction.TransactionRepository;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmailService emailService;

    @Value("${app.report.recipient}")
    private String reportRecipient;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportService(TransactionRepository transactionRepository,
                         OrderItemRepository orderItemRepository,
                         EmailService emailService) {
        this.transactionRepository = transactionRepository;
        this.orderItemRepository = orderItemRepository;
        this.emailService = emailService;
    }

    // ---- Weekly Transactions Report ----

    public void sendWeeklyTransactionsReport() {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(7);

        List<Transaction> transactions =
                transactionRepository.findByCreatedAtBetween(from, to);

        byte[] csv = buildTransactionsCsv(transactions);

        String filename = "transactions_" +
                from.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "_to_" +
                to.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                ".csv";

        emailService.sendEmailWithAttachment(
                reportRecipient,
                "Weekly Transactions Report",
                "Please find attached the transactions report for the last 7 days.\n" +
                "Period: " + from.format(FORMATTER) + " to " + to.format(FORMATTER) + "\n" +
                "Total transactions: " + transactions.size(),
                csv,
                filename
        );
    }

    @Scheduled(cron = "${app.report.schedule.transactions}")
    public void scheduledWeeklyTransactionsReport() {
        sendWeeklyTransactionsReport();
    }

    // ---- Best Sellers Report ----

    public void sendBestSellersReport() {
        List<Object[]> results = orderItemRepository.findBestSellersByCategory();
        byte[] csv = buildBestSellersCsv(results);

        String filename = "best_sellers_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                ".csv";

        emailService.sendEmailWithAttachment(
                reportRecipient,
                "Best Sellers Report by Category",
                "Please find attached the best sellers report grouped by category.\n" +
                "Generated at: " + LocalDateTime.now().format(FORMATTER),
                csv,
                filename
        );
    }

    @Scheduled(cron = "${app.report.schedule.bestsellers}")
    public void scheduledBestSellersReport() {
        sendBestSellersReport();
    }

    // ---- CSV Builders ----

    private byte[] buildTransactionsCsv(List<Transaction> transactions) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {

            writer.writeNext(new String[]{
                "Transaction ID", "User ID", "User Email",
                "Amount", "Type", "Date"
            });

            for (Transaction t : transactions) {
                writer.writeNext(new String[]{
                    String.valueOf(t.getId()),
                    String.valueOf(t.getUser().getId()),
                    t.getUser().getEmail(),
                    t.getAmount().toString(),
                    t.getType().name(),
                    t.getCreatedAt().format(FORMATTER)
                });
            }

            writer.flush();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate transactions CSV: " + e.getMessage());
        }
    }

    private byte[] buildBestSellersCsv(List<Object[]> results) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {

            writer.writeNext(new String[]{
                "Category", "Product Name", "Total Units Sold"
            });

            for (Object[] row : results) {
                writer.writeNext(new String[]{
                    (String) row[0],
                    (String) row[1],
                    String.valueOf(row[2])
                });
            }

            writer.flush();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate best sellers CSV: " + e.getMessage());
        }
    }
}