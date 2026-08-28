package com.smarttravel.modules.pricing.service;

import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown;
import com.smarttravel.modules.pricing.model.DynamicPricingRule;
import com.smarttravel.modules.pricing.model.DynamicPricingRuleType;
import com.smarttravel.modules.pricing.model.FlightPriceHistory;
import com.smarttravel.modules.pricing.repository.DynamicPricingRuleRepository;
import com.smarttravel.modules.pricing.repository.FlightPriceHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic pricing engine extending the existing static cabin multiplier calculation.
 *
 * Architecture:
 *   FareCalculationService (cabin multiplier, GST, fees)
 *          ↓
 *   DynamicPricingService (demand + seasonal + holiday adjustments)
 *          ↓
 *   Transparent DynamicPriceBreakdown returned to API and stored in price history
 *
 * Demand pricing bands (configurable via DynamicPricingRule entities):
 *   Occupancy  0–40%  → 0% adjustment
 *   Occupancy 40–60%  → +5%
 *   Occupancy 60–80%  → +10%
 *   Occupancy 80–90%  → +20%
 *   Occupancy 90–100% → +30%
 */
import com.smarttravel.modules.pricing.event.DynamicPricingEvent;
import com.smarttravel.modules.pricing.websocket.PricingWebSocketPublisher;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class DynamicPricingServiceImpl implements DynamicPricingService {

    private static final Logger log = LoggerFactory.getLogger(DynamicPricingServiceImpl.class);
    private static final BigDecimal TAX_RATE = new BigDecimal("0.12");

    // Minimum minutes between price history snapshots per flight+cabin
    private static final long MIN_SNAPSHOT_INTERVAL_MINUTES = 60;

    private final DynamicPricingRuleRepository ruleRepository;
    private final FlightPriceHistoryRepository historyRepository;
    private final PricingWebSocketPublisher pricingWebSocketPublisher;

    @Autowired
    public DynamicPricingServiceImpl(DynamicPricingRuleRepository ruleRepository,
                                     FlightPriceHistoryRepository historyRepository,
                                     @Autowired(required = false) PricingWebSocketPublisher pricingWebSocketPublisher) {
        this.ruleRepository = ruleRepository;
        this.historyRepository = historyRepository;
        this.pricingWebSocketPublisher = pricingWebSocketPublisher;
    }

    @Override
    public DynamicPriceBreakdown calculateDynamicPrice(Flight flight, CabinInventory inventory, int passengers) {
        int pax = Math.max(1, passengers);
        BigDecimal baseFare = inventory.getBasePrice() != null
                ? inventory.getBasePrice().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Calculate occupancy for this cabin
        double occupancyRatio = 0.0;
        if (inventory.getTotalSeats() > 0) {
            int occupied = inventory.getTotalSeats() - Math.max(0, inventory.getAvailableSeats());
            occupancyRatio = (double) occupied / inventory.getTotalSeats();
        }

        // --- Demand Adjustment ---
        double demandPercent = 0.0;
        String demandReason = null;
        List<DynamicPricingRule> demandRules = getEnabledDemandRules();
        if (!demandRules.isEmpty()) {
            demandPercent = selectDemandAdjustment(demandRules, occupancyRatio);
            if (demandPercent > 0) {
                demandReason = String.format("High demand (%.0f%% seats filled)", occupancyRatio * 100);
            } else if (demandPercent < 0) {
                demandReason = String.format("Low demand (%.0f%% seats filled)", occupancyRatio * 100);
            }
        } else {
            // Default demand bands if no rules are configured
            demandPercent = defaultDemandAdjustment(occupancyRatio);
            if (demandPercent != 0) {
                demandReason = String.format("Demand pricing (%.0f%% occupied)", occupancyRatio * 100);
            }
        }

        // --- Seasonal Adjustment ---
        double seasonalPercent = 0.0;
        String seasonalReason = null;
        Instant now = Instant.now();
        List<DynamicPricingRule> seasonalRules = getActiveTimeBoundRules(DynamicPricingRuleType.SEASONAL, now);
        if (!seasonalRules.isEmpty()) {
            DynamicPricingRule bestSeasonal = seasonalRules.get(0);
            seasonalPercent = bestSeasonal.getPercentageAdjustment();
            seasonalReason = bestSeasonal.getDescription() != null ? bestSeasonal.getDescription() : "Seasonal pricing";
        }

        // --- Holiday Adjustment ---
        double holidayPercent = 0.0;
        String holidayReason = null;
        List<DynamicPricingRule> holidayRules = getActiveTimeBoundRules(DynamicPricingRuleType.HOLIDAY, now);
        if (!holidayRules.isEmpty()) {
            DynamicPricingRule bestHoliday = holidayRules.get(0);
            holidayPercent = bestHoliday.getPercentageAdjustment();
            holidayReason = bestHoliday.getDescription() != null ? bestHoliday.getDescription() : "Holiday surcharge";
        }

        // --- Calculate Adjusted Fare ---
        BigDecimal demandAdjAmt = baseFare.multiply(BigDecimal.valueOf(demandPercent / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal seasonalAdjAmt = baseFare.multiply(BigDecimal.valueOf(seasonalPercent / 100.0))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal holidayAdjAmt = baseFare.multiply(BigDecimal.valueOf(holidayPercent / 100.0))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalDynamicAdj = demandAdjAmt.add(seasonalAdjAmt).add(holidayAdjAmt)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal adjustedBase = baseFare.add(totalDynamicAdj).setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = adjustedBase.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = inventory.getFeeAmount() != null && inventory.getFeeAmount().compareTo(BigDecimal.ZERO) > 0
                ? inventory.getFeeAmount().setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(150).setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPerPax = adjustedBase.add(tax).add(fee).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = totalPerPax.multiply(BigDecimal.valueOf(pax)).setScale(2, RoundingMode.HALF_UP);

        return DynamicPriceBreakdown.builder()
                .flightId(flight.getId())
                .cabinClass(inventory.getCabinClass())
                .passengerCount(pax)
                .baseFare(baseFare)
                .demandAdjustment(demandAdjAmt)
                .demandAdjustmentPercent(demandPercent)
                .demandReason(demandReason)
                .seasonalAdjustment(seasonalAdjAmt)
                .seasonalAdjustmentPercent(seasonalPercent)
                .seasonalReason(seasonalReason)
                .holidayAdjustment(holidayAdjAmt)
                .holidayAdjustmentPercent(holidayPercent)
                .holidayReason(holidayReason)
                .totalDynamicAdjustment(totalDynamicAdj)
                .taxes(tax)
                .fees(fee)
                .totalPerPassenger(totalPerPax)
                .grandTotal(grandTotal)
                .currency("INR")
                .occupancyRatio(occupancyRatio)
                .build();
    }

    @Override
    public void recordPriceSnapshot(Flight flight, CabinInventory inventory) {
        if (flight == null || inventory == null || inventory.getCabinClass() == null) return;

        // Rate-limit: don't snapshot more than once per 60 minutes per cabin
        Instant cutoff = Instant.now().minus(MIN_SNAPSHOT_INTERVAL_MINUTES, ChronoUnit.MINUTES);
        long recentCount = historyRepository.countByFlightIdAndCabinClassAndCapturedAtAfter(
                flight.getId(), inventory.getCabinClass(), cutoff);

        if (recentCount > 0) {
            log.debug("Skipping price snapshot for flight {} cabin {} (rate limited)", 
                    flight.getFlightNumber(), inventory.getCabinClass());
            return;
        }

        DynamicPriceBreakdown breakdown = calculateDynamicPrice(flight, inventory, 1);
        FlightPriceHistory history = toHistoryRecord(breakdown, flight.getFlightNumber());
        historyRepository.save(history);
        log.debug("Recorded price snapshot for flight {} cabin {} at {}", 
                flight.getFlightNumber(), inventory.getCabinClass(), Instant.now());
    }

    @Override
    public FlightPriceHistory toHistoryRecord(DynamicPriceBreakdown breakdown, String flightNumber) {
        return FlightPriceHistory.builder()
                .flightId(breakdown.getFlightId())
                .flightNumber(flightNumber)
                .cabinClass(breakdown.getCabinClass())
                .basePrice(breakdown.getBaseFare())
                .demandAdjustmentPercent(breakdown.getDemandAdjustmentPercent())
                .seasonalAdjustmentPercent(breakdown.getSeasonalAdjustmentPercent())
                .holidayAdjustmentPercent(breakdown.getHolidayAdjustmentPercent())
                .dynamicAdjustmentAmount(breakdown.getTotalDynamicAdjustment())
                .taxAmount(breakdown.getTaxes())
                .feeAmount(breakdown.getFees())
                .finalPrice(breakdown.getTotalPerPassenger())
                .occupancyRatio(breakdown.getOccupancyRatio())
                .reason(buildReasonString(breakdown))
                .build();
    }

    // ── Private Helpers ──────────────────────────────────────────────────────────

    /**
     * Select demand adjustment from DB-configured demand rules matching the current occupancy band.
     */
    private double selectDemandAdjustment(List<DynamicPricingRule> rules, double occupancy) {
        for (DynamicPricingRule rule : rules) {
            Double min = rule.getMinOccupancyThreshold();
            Double max = rule.getMaxOccupancyThreshold();
            if (min == null || max == null) continue;
            if (occupancy >= min && occupancy < max) {
                return rule.getPercentageAdjustment();
            }
        }
        return 0.0;
    }

    /**
     * Default demand pricing bands used when no DEMAND rules are configured in DB.
     */
    private double defaultDemandAdjustment(double occupancy) {
        if (occupancy < 0.40) return 0.0;
        if (occupancy < 0.60) return 5.0;
        if (occupancy < 0.80) return 10.0;
        if (occupancy < 0.90) return 20.0;
        return 30.0;
    }

    @Override
    public void publishPriceUpdate(Flight flight, CabinInventory inventory, BigDecimal oldPrice) {
        if (flight == null || inventory == null || pricingWebSocketPublisher == null) {
            return;
        }
        try {
            DynamicPriceBreakdown breakdown = calculateDynamicPrice(flight, inventory, 1);
            DynamicPricingEvent event = DynamicPricingEvent.builder()
                    .flightId(flight.getId())
                    .flightNumber(flight.getFlightNumber())
                    .cabinClass(inventory.getCabinClass())
                    .oldPrice(oldPrice)
                    .newPrice(breakdown.getTotalPerPassenger())
                    .currency(breakdown.getCurrency())
                    .demandAdjustmentPercent(breakdown.getDemandAdjustmentPercent())
                    .seasonalAdjustmentPercent(breakdown.getSeasonalAdjustmentPercent())
                    .holidayAdjustmentPercent(breakdown.getHolidayAdjustmentPercent())
                    .occupancyRatio(breakdown.getOccupancyRatio())
                    .availableSeats(inventory.getAvailableSeats())
                    .reason(buildReasonString(breakdown))
                    .timestamp(Instant.now())
                    .build();

            pricingWebSocketPublisher.publish(event);
        } catch (Exception ex) {
            log.error("Failed to publish price update for flight {}: {}", flight.getId(), ex.getMessage());
        }
    }

    private String buildReasonString(DynamicPriceBreakdown breakdown) {
        if (breakdown == null) return "Dynamic pricing calculation";
        List<String> reasons = new ArrayList<>();
        if (breakdown.getDemandReason() != null && !breakdown.getDemandReason().isBlank()) {
            reasons.add(breakdown.getDemandReason());
        }
        if (breakdown.getSeasonalReason() != null && !breakdown.getSeasonalReason().isBlank()) {
            reasons.add(breakdown.getSeasonalReason());
        }
        if (breakdown.getHolidayReason() != null && !breakdown.getHolidayReason().isBlank()) {
            reasons.add(breakdown.getHolidayReason());
        }
        return reasons.isEmpty() ? "Standard rate calculation" : String.join(" | ", reasons);
    }

    @org.springframework.cache.annotation.Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_DYNAMIC_PRICING_RULES, key = "'demand_rules'")
    public List<DynamicPricingRule> getEnabledDemandRules() {
        return ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND);
    }

    public List<DynamicPricingRule> getActiveTimeBoundRules(DynamicPricingRuleType type, Instant time) {
        return ruleRepository.findByTypeAndEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(type, time, time);
    }
}

