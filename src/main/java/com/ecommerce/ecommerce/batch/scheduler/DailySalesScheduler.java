package com.ecommerce.ecommerce.batch.scheduler;

import com.ecommerce.ecommerce.batch.config.DailySalesBatchConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
//@RequiredArgsConstructor
public class DailySalesScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailySalesJob;

    public DailySalesScheduler(JobLauncher jobLauncher,Job dailySalesJob) {
        this.jobLauncher = jobLauncher;
        this.dailySalesJob = dailySalesJob;
    }

    @Scheduled(cron = "${app.batch.schedule}")
    public void runDailySalesJob() {
        launchJob();
    }

    // Called by both the scheduler and the controller
    public void launchJob() {
        try {
            // Unique parameters allow the same job to run multiple times
            JobParameters params = new JobParametersBuilder()
                    .addLocalDateTime("runAt", LocalDateTime.now())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(dailySalesJob, params);
            //log.info("Daily sales job started with status: {}", execution.getStatus());
            System.out.println("Daily sales job started with status " + execution.getStatus());

        } catch (Exception e) {
        	throw new RuntimeException("failed to launch daily sales job: " + e.getMessage());
            //log.error("Failed to launch daily sales job: {}", e.getMessage());
        }
    }
}