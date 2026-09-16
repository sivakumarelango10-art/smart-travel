package com.smarttravel.modules.health.controller;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Keep-Alive Scheduler for Render.com free-tier deployment.
 *
 * <p>Render free-tier web services spin down after 15 minutes of inactivity,
 * causing 1–3 minute cold starts for the next visitor. This scheduler executes
 * a lightweight MongoDB ping every 10 minutes to keep the JVM warm and the
 * database connection pool alive, preventing expensive cold starts.
 *
 * <p>This is only effective if at least one user visits the app every 10 min,
 * since Render counts HTTP traffic — not internal JVM activity — as "activity".
 * The combined approach of frontend keep-alive + backend DB ping yields the
 * best results on Render's free plan.
 */
@Component
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    private final MongoTemplate mongoTemplate;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public KeepAliveScheduler(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Pings MongoDB every 10 minutes to keep the Atlas connection pool warm.
     * This prevents MongoDB Atlas from timing out idle connections, which would
     * cause a 500–2000ms reconnect penalty on the first request after inactivity.
     *
     * <p>Uses fixedDelay (not fixedRate) to avoid ping pile-up if a ping takes long.
     * Initial delay of 5 minutes avoids unnecessary ping right at startup.
     */
    @Scheduled(initialDelayString = "${keepalive.initial-delay-ms:300000}",
               fixedDelayString  = "${keepalive.fixed-delay-ms:600000}")
    public void pingDatabase() {
        try {
            long start = System.currentTimeMillis();
            Document result = mongoTemplate.executeCommand(new Document("ping", 1));
            long latencyMs = System.currentTimeMillis() - start;

            if (result != null && result.containsKey("ok")) {
                log.debug("Keep-alive ping: MongoDB Atlas CONNECTED ({}ms) [profile={}]", latencyMs, activeProfile);
            } else {
                log.warn("Keep-alive ping: MongoDB Atlas returned unexpected response");
            }
        } catch (Exception ex) {
            log.warn("Keep-alive ping: MongoDB Atlas connectivity degraded — {}", ex.getMessage());
        }
    }
}
