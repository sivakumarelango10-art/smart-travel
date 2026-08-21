package com.smarttravel.modules.flight.aviationstack;

import com.smarttravel.modules.flight.config.AviationstackProperties;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackCacheManager;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackQuotaGuard;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackFlightItem;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackFlightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AviationstackQuotaAndCacheTest {

    private AviationstackProperties properties;
    private AviationstackQuotaGuard quotaGuard;
    private AviationstackCacheManager cacheManager;

    @BeforeEach
    void setUp() {
        properties = new AviationstackProperties();
        properties.setMonthlyRequestLimit(100);
        properties.setSearchCacheSeconds(180);
        properties.setFlightCacheSeconds(60);

        quotaGuard = new AviationstackQuotaGuard(properties);
        quotaGuard.resetForTesting();

        cacheManager = new AviationstackCacheManager(properties);
        cacheManager.clearAll();
    }

    @Test
    @DisplayName("1. Cache stores and retrieves search query and single flight with TTL")
    void testCacheHitsAndMisses() {
        AviationstackFlightResponse response = new AviationstackFlightResponse();
        AviationstackFlightItem item = new AviationstackFlightItem();
        item.setFlightStatus("active");
        response.setData(List.of(item));

        // Cache Miss
        assertTrue(cacheManager.getCachedSearch("search:DEL->BOM:2026-08-21").isEmpty());
        assertTrue(cacheManager.getCachedFlight("AI-101").isEmpty());

        // Store in Cache
        cacheManager.putCachedSearch("search:DEL->BOM:2026-08-21", response);
        cacheManager.putCachedFlight("AI-101", response);

        // Cache Hit
        Optional<AviationstackCacheManager.CachedResponse<AviationstackFlightResponse>> cachedSearch =
                cacheManager.getCachedSearch("search:DEL->BOM:2026-08-21");
        assertTrue(cachedSearch.isPresent());
        assertEquals("active", cachedSearch.get().data().getData().get(0).getFlightStatus());

        Optional<AviationstackCacheManager.CachedResponse<AviationstackFlightResponse>> cachedFlight =
                cacheManager.getCachedFlight("AI-101");
        assertTrue(cachedFlight.isPresent());
        assertEquals("active", cachedFlight.get().data().getData().get(0).getFlightStatus());
    }

    @Test
    @DisplayName("2. Monthly Quota Guard tracks request budget and blocks requests when limit reached")
    void testMonthlyQuotaGuard() {
        assertEquals(0, quotaGuard.getCurrentUsage());
        assertEquals(100, quotaGuard.getRemainingQuota());
        assertTrue(quotaGuard.isQuotaAvailable());

        // Record 50 requests
        for (int i = 0; i < 50; i++) {
            quotaGuard.recordRequest();
        }
        assertEquals(50, quotaGuard.getCurrentUsage());
        assertEquals(50, quotaGuard.getRemainingQuota());
        assertTrue(quotaGuard.isQuotaAvailable());

        // Set to max limit (100)
        quotaGuard.setUsageForTesting(100);
        assertEquals(100, quotaGuard.getCurrentUsage());
        assertEquals(0, quotaGuard.getRemainingQuota());
        assertFalse(quotaGuard.isQuotaAvailable(), "Quota guard must block requests when monthly limit is reached");
    }

    @Test
    @DisplayName("3. Request coalescing deduplicates 20 concurrent identical requests into exactly 1 loader invocation")
    void testConcurrentCallDeduplication() throws Exception {
        AtomicInteger loaderCallCount = new AtomicInteger(0);
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    cacheManager.executeWithCoalescing("flight:AI-204", () -> {
                        loaderCallCount.incrementAndGet();
                        try {
                            Thread.sleep(50); // Simulate network latency
                        } catch (InterruptedException ignored) {
                        }
                        AviationstackFlightResponse resp = new AviationstackFlightResponse();
                        resp.setData(Collections.emptyList());
                        return resp;
                    });
                } catch (Exception ignored) {
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Release all threads at the exact same instant
        startLatch.countDown();
        boolean completed = doneLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed);
        assertEquals(1, loaderCallCount.get(), "20 concurrent requests must coalesce into exactly 1 loader execution");
    }
}
