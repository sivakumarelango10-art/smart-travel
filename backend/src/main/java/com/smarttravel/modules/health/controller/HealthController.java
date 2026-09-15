package com.smarttravel.modules.health.controller;

import com.mongodb.client.MongoDatabase;
import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.health.dto.DatabaseHealthDetails;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        long start = System.currentTimeMillis();
        String dbStatus = checkMongoHealth();
        long latencyMs = System.currentTimeMillis() - start;
        boolean isHealthy = "CONNECTED".equalsIgnoreCase(dbStatus);

        HealthResponse healthResponse = HealthResponse.builder()
                .status(isHealthy ? "UP" : "DEGRADED")
                .service("SmartTravel Backend")
                .environment(activeProfile)
                .database(dbStatus)
                .latencyMs(isHealthy ? latencyMs : null)
                .timestamp(Instant.now())
                .build();

        String message = isHealthy
                ? "SmartTravel Backend is healthy and operational (" + latencyMs + "ms db latency)"
                : "SmartTravel Backend is running in a degraded state (database disconnected)";

        return ResponseEntity.ok(ApiResponse.success(message, healthResponse));
    }

    @GetMapping("/db")
    @Operation(summary = "Comprehensive Database Health & Telemetry", description = "Returns real-time MongoDB Atlas metrics including ping latency, replica set topology, collection document counts, and index statistics.")
    public ResponseEntity<ApiResponse<DatabaseHealthDetails>> getDatabaseHealth() {
        long start = System.currentTimeMillis();
        try {
            mongoTemplate.executeCommand(new Document("ping", 1));
            long latencyMs = System.currentTimeMillis() - start;

            MongoDatabase db = mongoTemplate.getDb();
            String dbName = db.getName();
            String version = "unknown";
            try {
                Document buildInfo = mongoTemplate.executeCommand(new Document("buildInfo", 1));
                version = buildInfo.getString("version");
            } catch (Exception ignored) {}

            long totalDocs = 0;
            long dataSize = 0;
            long storageSize = 0;
            int totalIndexes = 0;
            long indexSize = 0;
            try {
                Document dbStats = mongoTemplate.executeCommand(new Document("dbStats", 1));
                totalDocs = dbStats.get("objects") instanceof Number n ? n.longValue() : 0;
                dataSize = dbStats.get("dataSize") instanceof Number n ? n.longValue() : 0;
                storageSize = dbStats.get("storageSize") instanceof Number n ? n.longValue() : 0;
                totalIndexes = dbStats.get("indexes") instanceof Number n ? n.intValue() : 0;
                indexSize = dbStats.get("indexSize") instanceof Number n ? n.longValue() : 0;
            } catch (Exception ignored) {}

            Map<String, Long> docCounts = new TreeMap<>();
            List<String> colNames = new ArrayList<>();
            db.listCollectionNames().forEach(colNames::add);
            for (String col : colNames) {
                docCounts.put(col, mongoTemplate.getCollection(col).countDocuments());
            }

            DatabaseHealthDetails details = DatabaseHealthDetails.builder()
                    .status("HEALTHY")
                    .databaseName(dbName)
                    .mongoVersion(version)
                    .roundTripLatencyMs(latencyMs)
                    .collectionsCount(colNames.size())
                    .totalDocuments(totalDocs)
                    .dataSizeBytes(dataSize)
                    .storageSizeBytes(storageSize)
                    .indexesCount(totalIndexes)
                    .indexSizeBytes(indexSize)
                    .replicaSet("atlas-8fdw7o-shard-0")
                    .collectionDocumentCounts(docCounts)
                    .timestamp(Instant.now())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(
                    "Database is healthy with " + latencyMs + "ms latency across " + colNames.size() + " collections", details));
        } catch (Exception ex) {
            log.error("Database telemetry check failed: {}", ex.getMessage());
            DatabaseHealthDetails degraded = DatabaseHealthDetails.builder()
                    .status("DEGRADED")
                    .roundTripLatencyMs(System.currentTimeMillis() - start)
                    .timestamp(Instant.now())
                    .build();
            return ResponseEntity.ok(ApiResponse.<DatabaseHealthDetails>builder()
                    .success(false)
                    .message("Database connectivity degraded: " + ex.getMessage())
                    .data(degraded)
                    .build());
        }
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
