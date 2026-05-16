package com.ecommerce.ecommerce.capacity;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Component
public class RequestCapacityGuard {

    private final Semaphore semaphore;
    private final int maxConcurrentRequests;
    private final Counter rejectedRequestsCounter;

    @Autowired
    public RequestCapacityGuard(
            @Value("${app.capacity.max-concurrent-requests:30}") int maxConcurrentRequests,
            MeterRegistry meterRegistry) {
        this(maxConcurrentRequests,
                Counter.builder("app.capacity.requests.rejected")
                        .description("Number of API requests rejected by the capacity guard")
                        .register(meterRegistry));
        registerMetrics(meterRegistry);
    }

    RequestCapacityGuard(int maxConcurrentRequests) {
        this(maxConcurrentRequests, (Counter) null);
    }

    RequestCapacityGuard(int maxConcurrentRequests, Counter rejectedRequestsCounter) {
        this.maxConcurrentRequests = maxConcurrentRequests;
        this.semaphore = new Semaphore(maxConcurrentRequests, true);
        this.rejectedRequestsCounter = rejectedRequestsCounter;
    }

    public boolean tryAcquire(long waitMillis) throws InterruptedException {
        boolean acquired = semaphore.tryAcquire(waitMillis, TimeUnit.MILLISECONDS);
        if (!acquired && rejectedRequestsCounter != null) {
            rejectedRequestsCounter.increment();
        }
        return acquired;
    }

    public void release() {
        semaphore.release();
    }

    public int getAvailablePermits() {
        return semaphore.availablePermits();
    }

    public int getInFlightRequests() {
        return maxConcurrentRequests - semaphore.availablePermits();
    }

    public int getMaxConcurrentRequests() {
        return maxConcurrentRequests;
    }

    private void registerMetrics(MeterRegistry meterRegistry) {
        Gauge.builder("app.capacity.requests.in_flight", this, RequestCapacityGuard::getInFlightRequests)
                .description("Number of in-flight API requests currently admitted by the capacity guard")
                .register(meterRegistry);

        Gauge.builder("app.capacity.requests.available", this, RequestCapacityGuard::getAvailablePermits)
                .description("Available request-capacity permits")
                .register(meterRegistry);

        Gauge.builder("app.capacity.requests.max", this, RequestCapacityGuard::getMaxConcurrentRequests)
                .description("Maximum concurrent API requests allowed by the capacity guard")
                .register(meterRegistry);
    }
}
