package com.smarttravel.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Production-grade Request Correlation & Traceability Filter.
 * <p>
 * Inspects incoming requests for an {@code X-Request-ID} or {@code X-Correlation-ID} header.
 * Sanitizes the identifier to prevent log injection / header manipulation.
 * If absent or invalid, generates a unique UUID.
 * Injects the identifier into SLF4J MDC and sets the {@code X-Request-ID} response header.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_REQUEST_ID_KEY = "requestId";

    private static final Pattern SAFE_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-]{1,64}$");

    @Override
    protected void doFilterInternal(@org.springframework.lang.NonNull HttpServletRequest request,
                                    @org.springframework.lang.NonNull HttpServletResponse response,
                                    @org.springframework.lang.NonNull FilterChain filterChain) throws ServletException, IOException {
        String requestId = extractAndSanitizeRequestId(request);

        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        request.setAttribute(MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    private String extractAndSanitizeRequestId(HttpServletRequest request) {
        String headerId = request.getHeader(REQUEST_ID_HEADER);
        if (headerId == null || headerId.isBlank()) {
            headerId = request.getHeader(CORRELATION_ID_HEADER);
        }

        if (headerId != null && !headerId.isBlank()) {
            String trimmed = headerId.trim();
            if (SAFE_ID_PATTERN.matcher(trimmed).matches()) {
                return trimmed;
            }
        }

        return UUID.randomUUID().toString();
    }
}
