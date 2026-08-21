package com.smarttravel.modules.flight.provider.aviationstack;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.smarttravel.modules.flight.config.AviationstackProperties;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackFlightResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * High-performance, concurrency-safe Cache and Request-Coalescing Manager.
 * 1. Provides Caffeine TTL caching for search queries and single flight tracking.
 * 2. Provides in-flight request deduplication so concurrent requests for the same flight or route trigger only 1 external HTTP request.
 */
@Component
public class AviationstackCacheManager {

    private static final Logger log = LoggerFactory.getLogger(AviationstackCacheManager.class);

    private final AviationstackProperties properties;
    private final Cache<String, CachedResponse<AviationstackFlightResponse>> searchCache;
    private final Cache<String, CachedResponse<AviationstackFlightResponse>> flightCache;

    // Active in-flight requests coalescing map
    private final ConcurrentHashMap<String, CompletableFuture<AviationstackFlightResponse>> inFlightRequests = new ConcurrentHashMap<>();

    public AviationstackCacheManager(AviationstackProperties properties) {
        this.properties = properties;
        this.searchCache = Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(properties.getSearchCacheSeconds(), TimeUnit.SECONDS)
                .build();

        this.flightCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(properties.getFlightCacheSeconds(), TimeUnit.SECONDS)
                .build();
    }

    public record CachedResponse<T>(T data, Instant cachedAt, Instant expiresAt) {
        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    /**
     * Retrieves cached search response if present and valid.
     */
    public Optional<CachedResponse<AviationstackFlightResponse>> getCachedSearch(String cacheKey) {
        CachedResponse<AviationstackFlightResponse> cached = searchCache.getIfPresent(cacheKey);
        if (cached != null && !cached.isExpired()) {
            log.debug("Aviationstack search cache HIT for key: {}", cacheKey);
            return Optional.of(cached);
        }
        return Optional.empty();
    }

    /**
     * Stores search response in cache with configured TTL.
     */
    public void putCachedSearch(String cacheKey, AviationstackFlightResponse response) {
        if (response != null) {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(properties.getSearchCacheSeconds());
            searchCache.put(cacheKey, new CachedResponse<>(response, now, expiresAt));
            log.debug("Stored Aviationstack search in cache for key: {} (TTL: {}s)", cacheKey, properties.getSearchCacheSeconds());
        }
    }

    /**
     * Retrieves cached flight response if present and valid.
     */
    public Optional<CachedResponse<AviationstackFlightResponse>> getCachedFlight(String flightNumber) {
        String key = normalizeFlightKey(flightNumber);
        CachedResponse<AviationstackFlightResponse> cached = flightCache.getIfPresent(key);
        if (cached != null && !cached.isExpired()) {
            log.debug("Aviationstack single flight cache HIT for key: {}", key);
            return Optional.of(cached);
        }
        return Optional.empty();
    }

    /**
     * Stores flight response in cache with configured TTL.
     */
    public void putCachedFlight(String flightNumber, AviationstackFlightResponse response) {
        if (response != null) {
            String key = normalizeFlightKey(flightNumber);
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(properties.getFlightCacheSeconds());
            flightCache.put(key, new CachedResponse<>(response, now, expiresAt));
            log.debug("Stored Aviationstack flight in cache for key: {} (TTL: {}s)", key, properties.getFlightCacheSeconds());
        }
    }

    /**
     * Coalesces concurrent calls for the same key to execute exactly 1 upstream loader task.
     * Subsequent concurrent callers join the active CompletableFuture.
     */
    public AviationstackFlightResponse executeWithCoalescing(String key, Supplier<AviationstackFlightResponse> loader) {
        CompletableFuture<AviationstackFlightResponse> future = inFlightRequests.computeIfAbsent(key, k -> {
            log.debug("Initiating primary in-flight request for key: {}", k);
            return CompletableFuture.supplyAsync(loader);
        });

        try {
            return future.join();
        } finally {
            inFlightRequests.remove(key, future);
        }
    }

    public void clearAll() {
        searchCache.invalidateAll();
        flightCache.invalidateAll();
        inFlightRequests.clear();
    }

    private String normalizeFlightKey(String flightNumber) {
        return flightNumber != null ? flightNumber.trim().toUpperCase() : "UNKNOWN";
    }
}
