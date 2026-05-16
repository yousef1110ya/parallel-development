package com.ecommerce.ecommerce.capacity;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestCapacityFilterTest {

    @Test
    void shouldRejectSecondConcurrentApiRequestWhenCapacityIsFull() throws Exception {
        RequestCapacityGuard requestCapacityGuard = new RequestCapacityGuard(1);
        RequestCapacityFilter filter = new RequestCapacityFilter(requestCapacityGuard, 0);

        CountDownLatch firstRequestEnteredChain = new CountDownLatch(1);
        CountDownLatch releaseFirstRequest = new CountDownLatch(1);

        FilterChain blockingChain = (request, response) -> {
            firstRequestEnteredChain.countDown();
            try {
                assertTrue(releaseFirstRequest.await(2, TimeUnit.SECONDS));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ex);
            }
        };

        ExecutorService executor = Executors.newSingleThreadExecutor();
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/products");
        MockHttpServletResponse firstResponse = new MockHttpServletResponse();

        Future<?> firstRequestFuture = executor.submit(() -> {
            try {
                filter.doFilter(firstRequest, firstResponse, blockingChain);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        assertTrue(firstRequestEnteredChain.await(1, TimeUnit.SECONDS));

        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/orders/me");
        MockHttpServletResponse secondResponse = new MockHttpServletResponse();
        AtomicBoolean secondChainInvoked = new AtomicBoolean(false);

        filter.doFilter(secondRequest, secondResponse, (request, response) -> secondChainInvoked.set(true));

        assertEquals(429, secondResponse.getStatus());
        assertFalse(secondChainInvoked.get());
        assertTrue(secondResponse.getContentAsString().contains("request capacity"));

        releaseFirstRequest.countDown();
        firstRequestFuture.get(2, TimeUnit.SECONDS);
        executor.shutdownNow();
    }

    @Test
    void shouldSkipPublicEndpoints() throws Exception {
        RequestCapacityGuard requestCapacityGuard = new RequestCapacityGuard(1);
        RequestCapacityFilter filter = new RequestCapacityFilter(requestCapacityGuard, 0);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (req, res) -> chainInvoked.set(true));

        assertTrue(chainInvoked.get());
        assertEquals(200, response.getStatus());
        assertEquals(1, requestCapacityGuard.getAvailablePermits());
    }
}
