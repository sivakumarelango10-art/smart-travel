package com.smarttravel.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Enterprise Caffeine In-Memory Cache configuration for sub-10ms analytics and query performance.
 */
@Configuration
public class CacheConfig {

    public static final String CACHE_ANALYTICS_OVERVIEW = "analytics_overview";
    public static final String CACHE_ANALYTICS_REVENUE = "analytics_revenue";
    public static final String CACHE_ANALYTICS_BOOKINGS = "analytics_bookings";
    public static final String CACHE_ANALYTICS_FLIGHTS = "analytics_flights";
    public static final String CACHE_ANALYTICS_SEATS = "analytics_seats";
    public static final String CACHE_ANALYTICS_PAYMENTS = "analytics_payments";
    public static final String CACHE_ANALYTICS_CUSTOMERS = "analytics_customers";
    public static final String CACHE_ANALYTICS_DASHBOARD = "analytics_dashboard";
    public static final String CACHE_AIRPORTS = "airports";
    public static final String CACHE_FLIGHT_SEARCH = "flight_search";
    public static final String CACHE_FLIGHT_DETAILS = "flight_details";
    public static final String CACHE_HOTEL_STATIC = "hotel_static";
    public static final String CACHE_HOTEL_SEARCH = "hotel_search";
    public static final String CACHE_HOTEL_ROOMS = "hotel_rooms";
    public static final String CACHE_DYNAMIC_PRICING_RULES = "dynamic_pricing_rules";
    public static final String CACHE_RECOMMENDATIONS = "recommendations";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // 1. Static Reference Data (Long TTL: 24 hours)
        cacheManager.registerCustomCache(CACHE_AIRPORTS,
                Caffeine.newBuilder().initialCapacity(50).maximumSize(500)
                        .expireAfterWrite(24, TimeUnit.HOURS).recordStats().build());

        // 2. Business Rules & Catalogs (Medium TTL: 15-30 minutes)
        cacheManager.registerCustomCache(CACHE_DYNAMIC_PRICING_RULES,
                Caffeine.newBuilder().initialCapacity(20).maximumSize(200)
                        .expireAfterWrite(15, TimeUnit.MINUTES).recordStats().build());
        cacheManager.registerCustomCache(CACHE_HOTEL_STATIC,
                Caffeine.newBuilder().initialCapacity(100).maximumSize(1000)
                        .expireAfterWrite(30, TimeUnit.MINUTES).recordStats().build());
        cacheManager.registerCustomCache(CACHE_HOTEL_ROOMS,
                Caffeine.newBuilder().initialCapacity(100).maximumSize(1000)
                        .expireAfterWrite(15, TimeUnit.MINUTES).recordStats().build());

        // 3. Search & Operational Inventory (Short TTL: 45-60 seconds)
        cacheManager.registerCustomCache(CACHE_FLIGHT_DETAILS,
                Caffeine.newBuilder().initialCapacity(200).maximumSize(3000)
                        .expireAfterWrite(60, TimeUnit.SECONDS).recordStats().build());
        cacheManager.registerCustomCache(CACHE_FLIGHT_SEARCH,
                Caffeine.newBuilder().initialCapacity(500).maximumSize(5000)
                        .expireAfterWrite(5, TimeUnit.MINUTES).recordStats().build());
        cacheManager.registerCustomCache(CACHE_HOTEL_SEARCH,
                Caffeine.newBuilder().initialCapacity(200).maximumSize(2000)
                        .expireAfterWrite(5, TimeUnit.MINUTES).recordStats().build());

        // 4. Recommendation Signals (TTL: 2 minutes)
        cacheManager.registerCustomCache(CACHE_RECOMMENDATIONS,
                Caffeine.newBuilder().initialCapacity(100).maximumSize(2000)
                        .expireAfterWrite(120, TimeUnit.SECONDS).recordStats().build());

        // 5. Analytics Aggregations (TTL: 90 seconds)
        Caffeine<Object, Object> analyticsBuilder = Caffeine.newBuilder()
                .initialCapacity(50).maximumSize(500)
                .expireAfterWrite(90, TimeUnit.SECONDS).recordStats();
        cacheManager.registerCustomCache(CACHE_ANALYTICS_OVERVIEW, analyticsBuilder.build());
        cacheManager.registerCustomCache(CACHE_ANALYTICS_REVENUE, analyticsBuilder.build());
        cacheManager.registerCustomCache(CACHE_ANALYTICS_BOOKINGS, analyticsBuilder.build());
        cacheManager.registerCustomCache(CACHE_ANALYTICS_FLIGHTS, analyticsBuilder.build());
        cacheManager.registerCustomCache(CACHE_ANALYTICS_SEATS, analyticsBuilder.build());
        cacheManager.registerCustomCache(CACHE_ANALYTICS_PAYMENTS, analyticsBuilder.build());
        cacheManager.registerCustomCache(CACHE_ANALYTICS_CUSTOMERS, analyticsBuilder.build());
        cacheManager.registerCustomCache(CACHE_ANALYTICS_DASHBOARD, analyticsBuilder.build());

        // Default fallback configuration for any dynamically registered caches
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(90, TimeUnit.SECONDS)
                .recordStats());

        return cacheManager;
    }
}
