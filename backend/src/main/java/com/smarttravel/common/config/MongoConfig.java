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
                        .minSize(15)
                        .maxSize(100)
                        .maxWaitTime(2000, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(30000, TimeUnit.MILLISECONDS)
                        .maxConnectionLifeTime(30, TimeUnit.MINUTES))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(3000, TimeUnit.MILLISECONDS)
                        .readTimeout(5000, TimeUnit.MILLISECONDS))
                .applyToClusterSettings(cluster -> cluster
                        .serverSelectionTimeout(2000, TimeUnit.MILLISECONDS));
    }
}
