package com.smarttravel.modules.health.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.health.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping({"/api/health", "/api/v1/health", "/v1/health", "/health"})
@Tag(name = "Health", description = "System and Database Connectivity Health Checks")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    private final MongoTemplate mongoTemplate;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    public HealthController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping
    @Operation(summary = "System Health Check", description = "Returns the operational status of the backend service and database connectivity.")
    public ResponseEntity<ApiResponse<HealthResponse>> getHealth() {
        String dbStatus = checkMongoHealth();
        boolean isHealthy = "CONNECTED".equalsIgnoreCase(dbStatus);

        HealthResponse healthResponse = HealthResponse.builder()
                .status("UP")
                .service("SmartTravel Backend")
                .environment(activeProfile)
                .database(dbStatus)
                .timestamp(Instant.now())
                .build();

        String message = isHealthy
                ? "SmartTravel Backend is healthy and operational"
                : "SmartTravel Backend is running in a degraded state (database disconnected)";

        return ResponseEntity.ok(ApiResponse.success(message, healthResponse));
    }

    private String checkMongoHealth() {
        try {
            Document pingResult = mongoTemplate.executeCommand(new Document("ping", 1));
            if (pingResult != null && pingResult.containsKey("ok")) {
                return "CONNECTED";
            }
            return "UNKNOWN";
        } catch (Exception ex) {
            log.debug("MongoDB health check could not reach database or authenticate: {}", ex.getMessage());
            return "DISCONNECTED";
        }
    }
}
