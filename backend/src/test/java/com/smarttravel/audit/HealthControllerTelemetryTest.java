package com.smarttravel.audit;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.health.controller.HealthController;
import com.smarttravel.modules.health.dto.DatabaseHealthDetails;
import com.smarttravel.modules.health.dto.HealthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class HealthControllerTelemetryTest {

    @Autowired
    private HealthController healthController;

    @Test
    @DisplayName("Verify HealthController Root and /db Telemetry Endpoints")
    void verifyHealthTelemetry() {
        // 1. Basic Health Endpoint
        ResponseEntity<ApiResponse<HealthResponse>> healthEntity = healthController.getHealth();
        assertNotNull(healthEntity);
        assertEquals(200, healthEntity.getStatusCode().value());
        ApiResponse<HealthResponse> healthBody = healthEntity.getBody();
        assertNotNull(healthBody);
        assertTrue(healthBody.isSuccess());
        assertEquals("UP", healthBody.getData().getStatus());
        assertEquals("CONNECTED", healthBody.getData().getDatabase());
        assertNotNull(healthBody.getData().getLatencyMs());
        System.out.println("Basic Health Endpoint Result: " + healthBody.getMessage() + ", Latency: " + healthBody.getData().getLatencyMs() + "ms");

        // 2. Comprehensive Database Telemetry Endpoint
        ResponseEntity<ApiResponse<DatabaseHealthDetails>> dbEntity = healthController.getDatabaseHealth();
        assertNotNull(dbEntity);
        assertEquals(200, dbEntity.getStatusCode().value());
        ApiResponse<DatabaseHealthDetails> dbBody = dbEntity.getBody();
        assertNotNull(dbBody);
        assertTrue(dbBody.isSuccess());

        DatabaseHealthDetails dbData = dbBody.getData();
        assertNotNull(dbData);
        assertEquals("HEALTHY", dbData.getStatus());
        assertEquals("smarttravel", dbData.getDatabaseName());
        assertNotNull(dbData.getMongoVersion());
        assertTrue(dbData.getRoundTripLatencyMs() >= 0);
        assertTrue(dbData.getCollectionsCount() > 0);
        assertTrue(dbData.getTotalDocuments() > 0);
        assertTrue(dbData.getIndexesCount() > 0);
        assertNotNull(dbData.getCollectionDocumentCounts());

        System.out.println("Comprehensive DB Telemetry Result: Status=" + dbData.getStatus()
                + ", Engine=" + dbData.getMongoVersion()
                + ", Latency=" + dbData.getRoundTripLatencyMs() + "ms"
                + ", TotalCollections=" + dbData.getCollectionsCount()
                + ", TotalDocuments=" + dbData.getTotalDocuments()
                + ", TotalIndexes=" + dbData.getIndexesCount()
                + ", CollectionsTracked=" + dbData.getCollectionDocumentCounts().keySet());
    }
}
