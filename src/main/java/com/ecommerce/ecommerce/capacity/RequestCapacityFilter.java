package com.ecommerce.ecommerce.capacity;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RequestCapacityFilter extends OncePerRequestFilter {

    private final RequestCapacityGuard requestCapacityGuard;
    private final long maxWaitMillis;

    public RequestCapacityFilter(RequestCapacityGuard requestCapacityGuard, long maxWaitMillis) {
        this.requestCapacityGuard = requestCapacityGuard;
        this.maxWaitMillis = maxWaitMillis;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || !path.startsWith("/api/")
                || path.startsWith("/api/auth/")
                || path.startsWith("/actuator/")
                || path.startsWith("/h2-console/")
                || "/error".equals(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        boolean acquired;
        try {
            acquired = requestCapacityGuard.tryAcquire(maxWaitMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Request handling was interrupted.\"}");
            return;
        }

        if (!acquired) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "1");
            response.getWriter().write("{\"error\":\"Server is at request capacity. Please retry.\"}");
            return;
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            requestCapacityGuard.release();
        }
    }
}
