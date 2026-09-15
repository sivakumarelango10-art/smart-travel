package com.smarttravel.audit;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("dev")
public class DatabaseHealthAuditTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    @DisplayName("Run Live Database Health and Collection Audit")
    void auditDatabaseHealth() {
        System.out.println("===============================================================");
        System.out.println("   LIVE DATABASE HEALTH AUDIT & DIAGNOSTIC REPORT");
        System.out.println("===============================================================");

        long startPing = System.currentTimeMillis();
        Document pingResult = mongoTemplate.executeCommand(new Document("ping", 1));
        long pingDuration = System.currentTimeMillis() - startPing;
        System.out.println("MongoDB Ping: " + pingResult.toJson() + " (Round-trip Latency: " + pingDuration + " ms)");

        MongoDatabase db = mongoTemplate.getDb();
        System.out.println("Database Name: " + db.getName());

        try {
            Document buildInfo = mongoTemplate.executeCommand(new Document("buildInfo", 1));
            System.out.println("MongoDB Version: " + buildInfo.getString("version"));
        } catch (Exception e) {
            System.out.println("MongoDB Version: Could not query buildInfo: " + e.getMessage());
        }

        try {
            Document dbStats = mongoTemplate.executeCommand(new Document("dbStats", 1));
            System.out.println("dbStats: collections=" + dbStats.get("collections")
                    + ", views=" + dbStats.get("views")
                    + ", objects=" + dbStats.get("objects")
                    + ", dataSize=" + dbStats.get("dataSize") + " bytes"
                    + ", storageSize=" + dbStats.get("storageSize") + " bytes"
                    + ", indexes=" + dbStats.get("indexes")
                    + ", indexSize=" + dbStats.get("indexSize") + " bytes");
        } catch (Exception e) {
            System.out.println("Could not query dbStats: " + e.getMessage());
        }

        System.out.println("\n--- Collection Inventory & Document Counts ---");
        List<String> collectionNames = new ArrayList<>();
        db.listCollectionNames().forEach(collectionNames::add);
        System.out.println("Found " + collectionNames.size() + " collections:");

        for (String colName : collectionNames) {
            long docCount = mongoTemplate.getCollection(colName).countDocuments();
            System.out.println("  • " + colName + ": " + docCount + " documents");

            // List indexes
            System.out.println("    Indexes for [" + colName + "]:");
            mongoTemplate.getCollection(colName).listIndexes().forEach(idx -> {
                System.out.println("      - " + idx.getString("name") + " -> " + idx.get("key") + (idx.getBoolean("unique", false) ? " [UNIQUE]" : ""));
            });
        }
        System.out.println("===============================================================\n");
    }
}
