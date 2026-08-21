package com.smarttravel.modules.flight.provider.aviationstack;

import com.smarttravel.modules.flight.config.AviationstackProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Thread-safe monthly request quota guard for Aviationstack Free Tier protection.
 * Prevents unintentional quota exhaustion (100 requests/month default limit).
 */
@Component
public class AviationstackQuotaGuard {

    private static final Logger log = LoggerFactory.getLogger(AviationstackQuotaGuard.class);

    private final AviationstackProperties properties;
    private final AtomicReference<YearMonth> currentMonth = new AtomicReference<>(YearMonth.now());
    private final AtomicInteger requestCount = new AtomicInteger(0);

    public AviationstackQuotaGuard(AviationstackProperties properties) {
        this.properties = properties;
    }

    /**
     * Checks whether an external API call to Aviationstack is permitted within the monthly budget.
     */
    public synchronized boolean isQuotaAvailable() {
        checkAndResetMonthlyCounter();
        int limit = properties.getMonthlyRequestLimit();
        int current = requestCount.get();

        if (current >= limit) {
            log.warn("Aviationstack monthly request quota exhausted: {}/{} calls used for {}. External calls blocked; serving cache/mock fallback.",
                    current, limit, currentMonth.get());
            return false;
        }

        if (current >= (limit * 0.8)) {
            log.warn("Aviationstack monthly request quota at 80%+ capacity: {}/{} calls used for {}.",
                    current, limit, currentMonth.get());
        }

        return true;
    }

    /**
     * Atomically records a successfully initiated external Aviationstack request.
     */
    public synchronized int recordRequest() {
        checkAndResetMonthlyCounter();
        int newCount = requestCount.incrementAndGet();
        log.info("Aviationstack API call executed. Monthly usage: {}/{} (Month: {})",
                newCount, properties.getMonthlyRequestLimit(), currentMonth.get());
        return newCount;
    }

    public int getCurrentUsage() {
        checkAndResetMonthlyCounter();
        return requestCount.get();
    }

    public int getRemainingQuota() {
        checkAndResetMonthlyCounter();
        return Math.max(0, properties.getMonthlyRequestLimit() - requestCount.get());
    }

    public synchronized void resetForTesting() {
        currentMonth.set(YearMonth.now());
        requestCount.set(0);
    }

    public synchronized void setUsageForTesting(int usage) {
        requestCount.set(usage);
    }

    private void checkAndResetMonthlyCounter() {
        YearMonth now = YearMonth.now();
        if (!now.equals(currentMonth.get())) {
            log.info("Aviationstack quota rollover: New calendar month detected ({}) vs previous ({}). Resetting counter.",
                    now, currentMonth.get());
            currentMonth.set(now);
            requestCount.set(0);
        }
    }
}
