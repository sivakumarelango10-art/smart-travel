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
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_ANALYTICS_OVERVIEW,
                CACHE_ANALYTICS_REVENUE,
                CACHE_ANALYTICS_BOOKINGS,
                CACHE_ANALYTICS_FLIGHTS,
                CACHE_ANALYTICS_SEATS,
                CACHE_ANALYTICS_PAYMENTS,
                CACHE_ANALYTICS_CUSTOMERS,
                CACHE_ANALYTICS_DASHBOARD,
                CACHE_AIRPORTS,
                CACHE_FLIGHT_SEARCH,
                CACHE_FLIGHT_DETAILS,
                CACHE_HOTEL_STATIC,
                CACHE_HOTEL_SEARCH,
                CACHE_HOTEL_ROOMS,
                CACHE_DYNAMIC_PRICING_RULES,
                CACHE_RECOMMENDATIONS
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(300)
                .maximumSize(5000)
                .expireAfterWrite(90, TimeUnit.SECONDS)
                .recordStats());

        return cacheManager;
    }
}
