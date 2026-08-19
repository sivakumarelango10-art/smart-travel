package com.smarttravel.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RequestIdFilterTest {

    private RequestIdFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RequestIdFilter();
        filterChain = mock(FilterChain.class);
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate UUID when no request ID header is provided")
    void shouldGenerateUuidWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY)).isNotBlank();
            assertThat(request.getAttribute(RequestIdFilter.MDC_REQUEST_ID_KEY)).isNotNull();
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        String responseHeader = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertThat(responseHeader).isNotBlank();
        assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY)).isNull(); // Cleaned up in finally
    }

    @Test
    @DisplayName("Should propagate valid X-Request-ID header")
    void shouldPropagateValidRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "custom-req-12345");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY)).isEqualTo("custom-req-12345");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("custom-req-12345");
        assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should fallback to X-Correlation-ID header if X-Request-ID is absent")
    void shouldFallbackToCorrelationId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.CORRELATION_ID_HEADER, "corr-trace-999");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY)).isEqualTo("corr-trace-999");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo("corr-trace-999");
        assertThat(MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY)).isNull();
    }

    @Test
    @DisplayName("Should reject and sanitize unsafe request ID header containing invalid characters")
    void shouldSanitizeUnsafeRequestId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "malicious\r\nHeader: Injection<script>");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            String mdcId = MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY);
            assertThat(mdcId).isNotBlank();
            assertThat(mdcId).doesNotContain("malicious");
            assertThat(mdcId).doesNotContain("<script>");
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        String responseHeader = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertThat(responseHeader).isNotBlank();
        assertThat(responseHeader).doesNotContain("<script>");
    }
}
