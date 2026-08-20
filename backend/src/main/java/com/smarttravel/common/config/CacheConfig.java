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

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_ANALYTICS_OVERVIEW,
                CACHE_ANALYTICS_REVENUE,
                CACHE_ANALYTICS_BOOKINGS,
                CACHE_ANALYTICS_FLIGHTS,
                CACHE_ANALYTICS_SEATS,
                CACHE_ANALYTICS_PAYMENTS,
                CACHE_ANALYTICS_CUSTOMERS,
                CACHE_ANALYTICS_DASHBOARD
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.SECONDS)
                .recordStats());

        return cacheManager;
    }
}
