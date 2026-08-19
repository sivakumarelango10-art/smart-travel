package com.smarttravel.modules.analytics.dto;

import java.time.Instant;

/**
 * Validated date-range request for analytics queries.
 * Periods resolve to UTC Instant boundaries to avoid server-locale ambiguity.
 */
public class AnalyticsDateRangeRequest {

    public enum Period {
        today, yesterday, last7days, last30days, thisMonth, lastMonth, custom
    }

    private Period period = Period.last30days;
    private Instant from;
    private Instant to;

    public AnalyticsDateRangeRequest() {
    }

    public AnalyticsDateRangeRequest(Period period, Instant from, Instant to) {
        this.period = period != null ? period : Period.last30days;
        this.from = from;
        this.to = to;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public Instant getFrom() {
        return from;
    }

    public void setFrom(Instant from) {
        this.from = from;
    }

    public Instant getTo() {
        return to;
    }

    public void setTo(Instant to) {
        this.to = to;
    }
}
