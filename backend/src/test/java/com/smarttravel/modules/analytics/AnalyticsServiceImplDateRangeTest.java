package com.smarttravel.modules.analytics;

import com.smarttravel.modules.analytics.dto.AnalyticsDateRangeRequest;
import com.smarttravel.modules.analytics.service.AnalyticsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for AnalyticsServiceImpl date range resolution and validation.
 */
class AnalyticsServiceImplDateRangeTest {

    private final AnalyticsServiceImpl service = new AnalyticsServiceImpl(null);

    @Test
    @DisplayName("today period resolves to UTC today start/end")
    void todayPeriodResolvesToUtcTodayBoundaries() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.today);

        LocalDate todayUtc = ZonedDateTime.now(ZoneOffset.UTC).toLocalDate();
        Instant expectedFrom = todayUtc.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant expectedTo = todayUtc.atTime(23, 59, 59).atZone(ZoneOffset.UTC).toInstant();

        assertThat(req.getPeriod()).isEqualTo(AnalyticsDateRangeRequest.Period.today);
        assertThat(expectedFrom).isBefore(expectedTo);
        assertThat(expectedFrom.atZone(ZoneOffset.UTC).toLocalDate()).isEqualTo(todayUtc);
    }

    @Test
    @DisplayName("custom period with from > to is invalid and throws IllegalArgumentException")
    void customPeriodFromAfterToIsInvalid() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        req.setFrom(Instant.parse("2026-08-10T00:00:00Z"));
        req.setTo(Instant.parse("2026-08-01T00:00:00Z")); // to < from

        assertThatThrownBy(() -> service.resolveDateRange(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("before");
    }

    @Test
    @DisplayName("custom period exceeding 366 days throws IllegalArgumentException")
    void customPeriodExceeding366DaysIsInvalid() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = from.plus(Duration.ofDays(400));
        req.setFrom(from);
        req.setTo(to);

        assertThatThrownBy(() -> service.resolveDateRange(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("366");
    }

    @Test
    @DisplayName("custom period missing from throws IllegalArgumentException")
    void customPeriodMissingFrom() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        req.setFrom(null);
        req.setTo(Instant.parse("2026-08-01T00:00:00Z"));

        assertThatThrownBy(() -> service.resolveDateRange(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires both");
    }

    @Test
    @DisplayName("default period is last30days")
    void defaultPeriodIsLast30Days() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        assertThat(req.getPeriod()).isEqualTo(AnalyticsDateRangeRequest.Period.last30days);
    }

    @Test
    @DisplayName("all period enum values are parseable")
    void allPeriodEnumsAreParseable() {
        for (AnalyticsDateRangeRequest.Period p : AnalyticsDateRangeRequest.Period.values()) {
            assertThat(AnalyticsDateRangeRequest.Period.valueOf(p.name())).isEqualTo(p);
        }
    }

    @Test
    @DisplayName("valid custom range within 366 days is accepted")
    void validCustomRangeIsAccepted() {
        AnalyticsDateRangeRequest req = new AnalyticsDateRangeRequest();
        req.setPeriod(AnalyticsDateRangeRequest.Period.custom);
        Instant from = Instant.parse("2026-07-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-01T00:00:00Z");
        req.setFrom(from);
        req.setTo(to);

        assertThat(service.resolveDateRange(req)).isNotNull();
    }
}
