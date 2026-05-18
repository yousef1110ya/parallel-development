package com.ecommerce.ecommerce.batch.writer;

import com.ecommerce.ecommerce.batch.entity.DailySalesSummary;
import com.ecommerce.ecommerce.batch.repository.DailySalesSummaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class SalesItemWriter implements ItemWriter<DailySalesSummary> {

    private static final Logger log = LoggerFactory.getLogger(SalesItemWriter.class);
    private final DailySalesSummaryRepository summaryRepository;

    public SalesItemWriter(DailySalesSummaryRepository summaryRepository) {
        this.summaryRepository = summaryRepository;
    }

    @Override
    public void write(Chunk<? extends DailySalesSummary> chunk) throws Exception {
        summaryRepository.saveAll(chunk.getItems());
        log.info("Saved {} sales summary records to DB", chunk.getItems().size());
    }
}