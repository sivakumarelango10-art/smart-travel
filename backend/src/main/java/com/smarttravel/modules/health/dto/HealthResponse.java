package com.smarttravel.modules.health.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Health Check Response Status")
public class HealthResponse {

    @Schema(description = "Overall system status", example = "UP")
    private String status;

    @Schema(description = "Service name identifier", example = "SmartTravel Backend")
    private String service;

    @Schema(description = "Active environment profile", example = "dev")
    private String environment;

    @Schema(description = "Database connectivity health", example = "CONNECTED")
    private String database;

    @Schema(description = "Response timestamp", example = "2026-08-18T16:25:00.000Z")
    private Instant timestamp = Instant.now();

    public HealthResponse() {
    }

    public HealthResponse(String status, String service, String environment, String database, Instant timestamp) {
        this.status = status;
        this.service = service;
        this.environment = environment;
        this.database = database;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String status;
        private String service;
        private String environment;
        private String database;
        private Instant timestamp = Instant.now();

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder service(String service) {
            this.service = service;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public HealthResponse build() {
            return new HealthResponse(status, service, environment, database, timestamp);
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
