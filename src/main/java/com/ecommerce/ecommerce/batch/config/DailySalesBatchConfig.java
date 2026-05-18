package com.ecommerce.ecommerce.batch.config;

import com.ecommerce.ecommerce.batch.entity.DailySalesSummary;
import com.ecommerce.ecommerce.batch.listener.DailySalesJobListener;
import com.ecommerce.ecommerce.batch.processor.SalesItemProcessor;
import com.ecommerce.ecommerce.batch.reader.SalesItemReader;
import com.ecommerce.ecommerce.batch.writer.SalesItemWriter;
import com.ecommerce.ecommerce.order.Order;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class DailySalesBatchConfig {

    private final SalesItemReader salesItemReader;
    private final SalesItemProcessor salesItemProcessor;
    private final SalesItemWriter salesItemWriter;
    private final DailySalesJobListener jobListener;

    public static final String JOB_NAME = "dailySalesJob";
    public DailySalesBatchConfig(SalesItemReader salesItemReader,
                                  SalesItemProcessor salesItemProcessor,
                                  SalesItemWriter salesItemWriter,
                                  DailySalesJobListener jobListener) {
        this.salesItemReader = salesItemReader;
        this.salesItemProcessor = salesItemProcessor;
        this.salesItemWriter = salesItemWriter;
        this.jobListener = jobListener;
    }

    @Bean
    public Job dailySalesJob(JobRepository jobRepository, Step dailySalesStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(jobListener)        // ← fires afterJob() when all chunks done
                .start(dailySalesStep)
                .build();
    }

    @Bean
    public Step dailySalesStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        return new StepBuilder("dailySalesStep", jobRepository)
                .<Order, DailySalesSummary>chunk(10, transactionManager)
                .reader(salesItemReader.createReader())
                .processor(salesItemProcessor)
                .writer(salesItemWriter)
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(0)
                .build();
    }
}