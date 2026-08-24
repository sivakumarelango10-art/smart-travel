package com.smarttravel.modules.payment.refund.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * Encapsulates the platform's time-based cancellation refund policy.
 *
 * <p>Policy tiers (based on hours before flight departure at time of cancellation):
 * <ul>
 *   <li>&gt; 168 hours (7 days) before departure  → 100% refund</li>
 *   <li>24–168 hours before departure             → 50% refund</li>
 *   <li>&lt; 24 hours before departure            → 0% refund (no refund)</li>
 *   <li>After departure (departure in the past)   → 0% refund</li>
 * </ul>
 *
 * <p><strong>Important</strong>: All arithmetic is performed on {@code long} paise values to ensure
 * integer-exact money arithmetic.  BigDecimal is used only for the final INR display value.
 */
@Service
public class CancellationRefundPolicy {

    /** Threshold: more than 7 days → full refund. */
    private static final long FULL_REFUND_THRESHOLD_HOURS = 168L;

    /** Threshold: 24h – 7 days → 50% partial refund. */
    private static final long PARTIAL_REFUND_THRESHOLD_HOURS = 24L;

    /**
     * Returns the refund percentage label as a human-readable string for API responses.
     *
     * @param departureTime The scheduled flight departure instant
     * @param cancelledAt   The instant at which cancellation is being processed (usually {@code Instant.now()})
     * @return Description such as "100%", "50%", or "No refund"
     */
    public String getRefundPercentageLabel(Instant departureTime, Instant cancelledAt) {
        if (departureTime == null) {
            return "100%"; // no departure data → default to full refund
        }
        long hoursUntilDeparture = hoursUntilDeparture(departureTime, cancelledAt);
        if (hoursUntilDeparture > FULL_REFUND_THRESHOLD_HOURS) {
            return "100%";
        } else if (hoursUntilDeparture >= PARTIAL_REFUND_THRESHOLD_HOURS) {
            return "50%";
        } else {
            return "0%";
        }
    }

    /**
     * Returns the eligible refund amount in paise (integer arithmetic, no floating point).
     *
     * @param originalAmountPaise Original paid amount in paise
     * @param departureTime       Scheduled flight departure instant
     * @param cancelledAt         Instant of cancellation
     * @return Refund amount in paise (0 if ineligible by policy)
     */
    public long calculateRefundAmountPaise(long originalAmountPaise, Instant departureTime, Instant cancelledAt) {
        if (departureTime == null) {
            return originalAmountPaise; // default full refund if no departure data
        }
        long hoursUntilDeparture = hoursUntilDeparture(departureTime, cancelledAt);
        if (hoursUntilDeparture > FULL_REFUND_THRESHOLD_HOURS) {
            return originalAmountPaise;
        } else if (hoursUntilDeparture >= PARTIAL_REFUND_THRESHOLD_HOURS) {
            // Integer division by 2 is exact for paise (no rounding issues for whole-rupee amounts)
            return originalAmountPaise / 2L;
        } else {
            return 0L;
        }
    }

    /**
     * Returns the eligible refund amount as INR {@link BigDecimal} (2 decimal places, HALF_UP rounding).
     *
     * @param originalAmountPaise Original paid amount in paise
     * @param departureTime       Scheduled flight departure instant
     * @param cancelledAt         Instant of cancellation
     * @return Refund amount in INR
     */
    public BigDecimal calculateRefundAmountInr(long originalAmountPaise, Instant departureTime, Instant cancelledAt) {
        long refundPaise = calculateRefundAmountPaise(originalAmountPaise, departureTime, cancelledAt);
        return BigDecimal.valueOf(refundPaise).divide(BigDecimal.valueOf(100L), 2, RoundingMode.HALF_UP);
    }

    /**
     * Returns the policy tier description (for audit logging and API transparency).
     *
     * @param departureTime Scheduled departure instant
     * @param cancelledAt   Instant of cancellation
     * @return Policy tier description
     */
    public String getPolicyDescription(Instant departureTime, Instant cancelledAt) {
        if (departureTime == null) {
            return "Full refund (no departure data available)";
        }
        long hoursUntilDeparture = hoursUntilDeparture(departureTime, cancelledAt);
        if (hoursUntilDeparture > FULL_REFUND_THRESHOLD_HOURS) {
            return "Full refund: cancellation more than 7 days (" + hoursUntilDeparture + "h) before departure";
        } else if (hoursUntilDeparture >= PARTIAL_REFUND_THRESHOLD_HOURS) {
            return "Partial refund (50%): cancellation " + hoursUntilDeparture + "h before departure (24h–7 days window)";
        } else if (hoursUntilDeparture >= 0) {
            return "No refund: cancellation within 24h of departure (" + hoursUntilDeparture + "h remaining)";
        } else {
            return "No refund: flight has already departed";
        }
    }

    // -----------------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------------

    private long hoursUntilDeparture(Instant departureTime, Instant cancelledAt) {
        Duration duration = Duration.between(cancelledAt, departureTime);
        return duration.toHours(); // negative if departure is in the past
    }
}
