package com.smarttravel.modules.pricing.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * MongoDB document representing a configurable dynamic pricing rule.
 * Rules are evaluated in priority order and applied cumulatively.
 */
@Document(collection = "dynamic_pricing_rules")
public class DynamicPricingRule {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private DynamicPricingRuleType type;

    private boolean enabled = true;

    /** Lower number = evaluated first */
    private int priority = 10;

    /** Percentage adjustment, e.g. 15.0 = +15%, -10.0 = -10% */
    private double percentageAdjustment;

    /** Optional date range for time-bound rules (holidays/seasons) */
    private Instant startDate;
    private Instant endDate;

    /** Human-readable description of why this rule activates */
    private String description;

    /** Minimum occupancy % threshold to trigger DEMAND rule (0.0 - 1.0) */
    private Double minOccupancyThreshold;

    /** Maximum occupancy % threshold (exclusive) for this rule band */
    private Double maxOccupancyThreshold;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private String createdBy;

    public DynamicPricingRule() {}

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final DynamicPricingRule r = new DynamicPricingRule();
        public Builder id(String v) { r.id = v; return this; }
        public Builder name(String v) { r.name = v; return this; }
        public Builder type(DynamicPricingRuleType v) { r.type = v; return this; }
        public Builder enabled(boolean v) { r.enabled = v; return this; }
        public Builder priority(int v) { r.priority = v; return this; }
        public Builder percentageAdjustment(double v) { r.percentageAdjustment = v; return this; }
        public Builder startDate(Instant v) { r.startDate = v; return this; }
        public Builder endDate(Instant v) { r.endDate = v; return this; }
        public Builder description(String v) { r.description = v; return this; }
        public Builder minOccupancyThreshold(Double v) { r.minOccupancyThreshold = v; return this; }
        public Builder maxOccupancyThreshold(Double v) { r.maxOccupancyThreshold = v; return this; }
        public Builder createdBy(String v) { r.createdBy = v; return this; }
        public DynamicPricingRule build() { return r; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public DynamicPricingRuleType getType() { return type; }
    public void setType(DynamicPricingRuleType type) { this.type = type; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public double getPercentageAdjustment() { return percentageAdjustment; }
    public void setPercentageAdjustment(double percentageAdjustment) { this.percentageAdjustment = percentageAdjustment; }
    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }
    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getMinOccupancyThreshold() { return minOccupancyThreshold; }
    public void setMinOccupancyThreshold(Double minOccupancyThreshold) { this.minOccupancyThreshold = minOccupancyThreshold; }
    public Double getMaxOccupancyThreshold() { return maxOccupancyThreshold; }
    public void setMaxOccupancyThreshold(Double maxOccupancyThreshold) { this.maxOccupancyThreshold = maxOccupancyThreshold; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
