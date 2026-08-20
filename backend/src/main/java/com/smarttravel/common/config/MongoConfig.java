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
                        .maxSize(50)
                        .minSize(5)
                        .maxWaitTime(3000, TimeUnit.MILLISECONDS)
                        .maxConnectionIdleTime(60000, TimeUnit.MILLISECONDS))
                .applyToSocketSettings(socket -> socket
                        .connectTimeout(5000, TimeUnit.MILLISECONDS)
                        .readTimeout(10000, TimeUnit.MILLISECONDS))
                .applyToClusterSettings(cluster -> cluster
                        .serverSelectionTimeout(3000, TimeUnit.MILLISECONDS));
    }
}
