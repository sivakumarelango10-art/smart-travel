package com.smarttravel.common.config;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.concurrent.TimeUnit;

/**
 * MongoDB Configuration, Auditing, and Production Connection Pool Settings.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsCustomizer() {
        return builder -> builder
                .applyToConnectionPoolSettings(pool -> pool
                        .minSize(5)
                        .maxSize(100)
                        .maxWaitTime(10000, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(30000, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(30, TimeUnit.MINUTES))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(10000, TimeUnit.MILLISECONDS)
                        .readTimeout(15000, TimeUnit.MILLISECONDS))
                .applyToClusterSettings(cluster -> cluster
                        .serverSelectionTimeout(10000, TimeUnit.MILLISECONDS));
    }
}
