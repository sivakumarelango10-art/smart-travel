package com.smarttravel.modules.health.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * Detailed real-time database telemetry and health diagnostics.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed MongoDB Atlas Telemetry and Health Diagnostics")
public class DatabaseHealthDetails {

    @Schema(description = "Overall database health status", example = "HEALTHY")
    private String status;

    @Schema(description = "Database name", example = "smarttravel")
    private String databaseName;

    @Schema(description = "MongoDB engine version", example = "8.0.32")
    private String mongoVersion;

    @Schema(description = "Round-trip ping latency in milliseconds", example = "26")
    private long roundTripLatencyMs;

    @Schema(description = "Total number of collections", example = "24")
    private int collectionsCount;

    @Schema(description = "Total document objects across all collections", example = "71930")
    private long totalDocuments;

    @Schema(description = "Uncompressed data size in bytes", example = "53029138")
    private long dataSizeBytes;

    @Schema(description = "Compressed storage size on disk in bytes", example = "15335424")
    private long storageSizeBytes;

    @Schema(description = "Total active indexes across all collections", example = "136")
    private int indexesCount;

    @Schema(description = "Memory-resident index size in bytes", example = "11718656")
    private long indexSizeBytes;

    @Schema(description = "Replica set identifier", example = "atlas-8fdw7o-shard-0")
    private String replicaSet;

    @Schema(description = "Active primary server address")
    private String primaryServer;

    @Schema(description = "Document counts mapped by collection name")
    private Map<String, Long> collectionDocumentCounts;

    @Schema(description = "Measurement timestamp")
    private Instant timestamp = Instant.now();

    public DatabaseHealthDetails() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final DatabaseHealthDetails details = new DatabaseHealthDetails();

        public Builder status(String status) { details.status = status; return this; }
        public Builder databaseName(String databaseName) { details.databaseName = databaseName; return this; }
        public Builder mongoVersion(String mongoVersion) { details.mongoVersion = mongoVersion; return this; }
        public Builder roundTripLatencyMs(long roundTripLatencyMs) { details.roundTripLatencyMs = roundTripLatencyMs; return this; }
        public Builder collectionsCount(int collectionsCount) { details.collectionsCount = collectionsCount; return this; }
        public Builder totalDocuments(long totalDocuments) { details.totalDocuments = totalDocuments; return this; }
        public Builder dataSizeBytes(long dataSizeBytes) { details.dataSizeBytes = dataSizeBytes; return this; }
        public Builder storageSizeBytes(long storageSizeBytes) { details.storageSizeBytes = storageSizeBytes; return this; }
        public Builder indexesCount(int indexesCount) { details.indexesCount = indexesCount; return this; }
        public Builder indexSizeBytes(long indexSizeBytes) { details.indexSizeBytes = indexSizeBytes; return this; }
        public Builder replicaSet(String replicaSet) { details.replicaSet = replicaSet; return this; }
        public Builder primaryServer(String primaryServer) { details.primaryServer = primaryServer; return this; }
        public Builder collectionDocumentCounts(Map<String, Long> counts) { details.collectionDocumentCounts = counts; return this; }
        public Builder timestamp(Instant timestamp) { details.timestamp = timestamp; return this; }

        public DatabaseHealthDetails build() {
            return details;
        }
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }

    public String getMongoVersion() { return mongoVersion; }
    public void setMongoVersion(String mongoVersion) { this.mongoVersion = mongoVersion; }

    public long getRoundTripLatencyMs() { return roundTripLatencyMs; }
    public void setRoundTripLatencyMs(long roundTripLatencyMs) { this.roundTripLatencyMs = roundTripLatencyMs; }

    public int getCollectionsCount() { return collectionsCount; }
    public void setCollectionsCount(int collectionsCount) { this.collectionsCount = collectionsCount; }

    public long getTotalDocuments() { return totalDocuments; }
    public void setTotalDocuments(long totalDocuments) { this.totalDocuments = totalDocuments; }

    public long getDataSizeBytes() { return dataSizeBytes; }
    public void setDataSizeBytes(long dataSizeBytes) { this.dataSizeBytes = dataSizeBytes; }

    public long getStorageSizeBytes() { return storageSizeBytes; }
    public void setStorageSizeBytes(long storageSizeBytes) { this.storageSizeBytes = storageSizeBytes; }

    public int getIndexesCount() { return indexesCount; }
    public void setIndexesCount(int indexesCount) { this.indexesCount = indexesCount; }

    public long getIndexSizeBytes() { return indexSizeBytes; }
    public void setIndexSizeBytes(long indexSizeBytes) { this.indexSizeBytes = indexSizeBytes; }

    public String getReplicaSet() { return replicaSet; }
    public void setReplicaSet(String replicaSet) { this.replicaSet = replicaSet; }

    public String getPrimaryServer() { return primaryServer; }
    public void setPrimaryServer(String primaryServer) { this.primaryServer = primaryServer; }

    public Map<String, Long> getCollectionDocumentCounts() { return collectionDocumentCounts; }
    public void setCollectionDocumentCounts(Map<String, Long> collectionDocumentCounts) { this.collectionDocumentCounts = collectionDocumentCounts; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
