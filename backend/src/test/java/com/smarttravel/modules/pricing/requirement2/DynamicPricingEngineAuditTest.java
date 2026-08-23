package com.smarttravel.modules.pricing.requirement2;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown;
import com.smarttravel.modules.pricing.model.DynamicPricingRule;
import com.smarttravel.modules.pricing.model.DynamicPricingRuleType;
import com.smarttravel.modules.pricing.repository.DynamicPricingRuleRepository;
import com.smarttravel.modules.pricing.repository.FlightPriceHistoryRepository;
import com.smarttravel.modules.pricing.service.DynamicPricingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Requirement #2 - Test Group 1: Dynamic Pricing Engine
 * Verifies demand-based pricing bands, seasonal rules, holiday pricing, deterministic math, and tax calculation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DynamicPricingEngineAuditTest {

    @Mock
    private DynamicPricingRuleRepository ruleRepository;

    @Mock
    private FlightPriceHistoryRepository historyRepository;

    @InjectMocks
    private DynamicPricingServiceImpl pricingService;

    private Flight flight;

    @BeforeEach
    void setUp() {
        flight = Flight.builder()
                .id("fl-req2-01")
                .flightNumber("AI-101")
                .status(FlightStatus.SCHEDULED)
                .departureTime(Instant.now().plus(2, ChronoUnit.DAYS))
                .build();
    }

    private CabinInventory createInventory(int totalSeats, int availableSeats, BigDecimal basePrice) {
        return CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(totalSeats)
                .availableSeats(availableSeats)
                .basePrice(basePrice)
                .feeAmount(new BigDecimal("150.00"))
                .build();
    }

    @Test
    @DisplayName("1. Low demand (<40% occupancy) uses base price with 0% adjustment")
    void testLowDemand_UsesBasePrice() {
        // 20 out of 100 occupied = 20% occupancy
        CabinInventory inv = createInventory(100, 80, new BigDecimal("5000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        assertThat(breakdown.getDemandAdjustmentPercent()).isEqualTo(0.0);
        assertThat(breakdown.getDemandAdjustment()).isEqualByComparingTo("0.00");
        assertThat(breakdown.getBaseFare()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("2. Medium demand (40-60% occupancy) applies +5% adjustment")
    void testMediumDemand_Applies5PercentSurge() {
        // 50 out of 100 occupied = 50% occupancy
        CabinInventory inv = createInventory(100, 50, new BigDecimal("5000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        assertThat(breakdown.getDemandAdjustmentPercent()).isEqualTo(5.0);
        assertThat(breakdown.getDemandAdjustment()).isEqualByComparingTo("250.00");
    }

    @Test
    @DisplayName("3. High demand (60-80% occupancy) applies +10% adjustment")
    void testHighDemand_Applies10PercentSurge() {
        // 70 out of 100 occupied = 70% occupancy
        CabinInventory inv = createInventory(100, 30, new BigDecimal("5000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        assertThat(breakdown.getDemandAdjustmentPercent()).isEqualTo(10.0);
        assertThat(breakdown.getDemandAdjustment()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("4. Very high demand (80-90% -> +20%, 90-100% -> +30%) produces expected surge")
    void testVeryHighDemand_AppliesExpectedSurge() {
        // 85% occupancy -> +20%
        CabinInventory inv85 = createInventory(100, 15, new BigDecimal("5000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown breakdown85 = pricingService.calculateDynamicPrice(flight, inv85, 1);
        assertThat(breakdown85.getDemandAdjustmentPercent()).isEqualTo(20.0);
        assertThat(breakdown85.getDemandAdjustment()).isEqualByComparingTo("1000.00");

        // 95% occupancy -> +30%
        CabinInventory inv95 = createInventory(100, 5, new BigDecimal("5000.00"));
        DynamicPriceBreakdown breakdown95 = pricingService.calculateDynamicPrice(flight, inv95, 1);
        assertThat(breakdown95.getDemandAdjustmentPercent()).isEqualTo(30.0);
        assertThat(breakdown95.getDemandAdjustment()).isEqualByComparingTo("1500.00");
    }

    @Test
    @DisplayName("5. Holiday pricing rule increases price by 20% during holiday period")
    void testHolidayPricing_Applies20PercentSurge() {
        CabinInventory inv = createInventory(100, 80, new BigDecimal("5000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPricingRule holidayRule = DynamicPricingRule.builder()
                .name("Independence Day Surge")
                .type(DynamicPricingRuleType.HOLIDAY)
                .percentageAdjustment(20.0)
                .description("20% holiday peak surge")
                .build();

        when(ruleRepository.findByTypeAndEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(DynamicPricingRuleType.HOLIDAY), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(holidayRule));

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        assertThat(breakdown.getHolidayAdjustmentPercent()).isEqualTo(20.0);
        assertThat(breakdown.getHolidayAdjustment()).isEqualByComparingTo("1000.00");
    }

    @Test
    @DisplayName("6. Seasonal pricing rule applies 15% surge during peak season")
    void testSeasonalPricing_Applies15PercentSurge() {
        CabinInventory inv = createInventory(100, 80, new BigDecimal("5000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPricingRule seasonalRule = DynamicPricingRule.builder()
                .name("Summer Vacation Peak")
                .type(DynamicPricingRuleType.SEASONAL)
                .percentageAdjustment(15.0)
                .description("15% summer holiday surge")
                .build();

        when(ruleRepository.findByTypeAndEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(DynamicPricingRuleType.SEASONAL), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(seasonalRule));

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        assertThat(breakdown.getSeasonalAdjustmentPercent()).isEqualTo(15.0);
        assertThat(breakdown.getSeasonalAdjustment()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("7. Multiple pricing factors (demand + seasonal + holiday) combine correctly")
    void testCombinedPricingFactors() {
        // 70% occupancy (+10%), Seasonal (+15%), Holiday (+20%) on base fare 5000
        CabinInventory inv = createInventory(100, 30, new BigDecimal("5000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPricingRule seasonalRule = DynamicPricingRule.builder()
                .name("Summer Peak")
                .type(DynamicPricingRuleType.SEASONAL)
                .percentageAdjustment(15.0)
                .build();
        DynamicPricingRule holidayRule = DynamicPricingRule.builder()
                .name("Festival Surge")
                .type(DynamicPricingRuleType.HOLIDAY)
                .percentageAdjustment(20.0)
                .build();

        when(ruleRepository.findByTypeAndEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(DynamicPricingRuleType.SEASONAL), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(seasonalRule));
        when(ruleRepository.findByTypeAndEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(DynamicPricingRuleType.HOLIDAY), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(holidayRule));

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        // Demand (+500), Seasonal (+750), Holiday (+1000) -> Total dynamic adjustment = +2250
        assertThat(breakdown.getDemandAdjustment()).isEqualByComparingTo("500.00");
        assertThat(breakdown.getSeasonalAdjustment()).isEqualByComparingTo("750.00");
        assertThat(breakdown.getHolidayAdjustment()).isEqualByComparingTo("1000.00");
        assertThat(breakdown.getTotalDynamicAdjustment()).isEqualByComparingTo("2250.00");

        // Adjusted base = 7250, GST 12% = 870, Fees = 150 -> Total per pax = 8270
        assertThat(breakdown.getTaxes()).isEqualByComparingTo("870.00");
        assertThat(breakdown.getTotalPerPassenger()).isEqualByComparingTo("8270.00");
    }

    @Test
    @DisplayName("8. Pricing calculation is strictly deterministic for identical inputs")
    void testPricingIsDeterministic() {
        CabinInventory inv = createInventory(100, 40, new BigDecimal("6000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown b1 = pricingService.calculateDynamicPrice(flight, inv, 2);
        DynamicPriceBreakdown b2 = pricingService.calculateDynamicPrice(flight, inv, 2);

        assertThat(b1.getGrandTotal()).isEqualByComparingTo(b2.getGrandTotal());
        assertThat(b1.getTaxes()).isEqualByComparingTo(b2.getTaxes());
        assertThat(b1.getTotalPerPassenger()).isEqualByComparingTo(b2.getTotalPerPassenger());
    }

    @Test
    @DisplayName("9. Transparent price breakdown provides itemized values")
    void testPriceBreakdownCompleteness() {
        CabinInventory inv = createInventory(100, 50, new BigDecimal("4000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 2);

        assertThat(breakdown.getFlightId()).isEqualTo("fl-req2-01");
        assertThat(breakdown.getCabinClass()).isEqualTo(CabinClass.ECONOMY);
        assertThat(breakdown.getPassengerCount()).isEqualTo(2);
        assertThat(breakdown.getBaseFare()).isEqualByComparingTo("4000.00");
        assertThat(breakdown.getCurrency()).isEqualTo("INR");
        assertThat(breakdown.getGrandTotal()).isEqualByComparingTo(breakdown.getTotalPerPassenger().multiply(BigDecimal.valueOf(2)));
    }

    @Test
    @DisplayName("10. Aviation GST 12% is accurately calculated on adjusted base fare")
    void testTaxCalculationAccurate() {
        CabinInventory inv = createInventory(100, 100, new BigDecimal("10000.00")); // 0% surge
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        // 12% of 10000 = 1200.00
        assertThat(breakdown.getTaxes()).isEqualByComparingTo("1200.00");
    }

    @Test
    @DisplayName("11. Invalid occupancy (0 total seats, negative available) handled gracefully")
    void testInvalidOccupancy_HandledGracefully() {
        CabinInventory inv = createInventory(0, 0, new BigDecimal("5000.00"));
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        assertThat(breakdown).isNotNull();
        assertThat(breakdown.getOccupancyRatio()).isEqualTo(0.0);
        assertThat(breakdown.getBaseFare()).isEqualByComparingTo("5000.00");
    }

    @Test
    @DisplayName("12. Maximum occupancy (100% booked) applies top surge tier")
    void testMaximumOccupancy_AppliesTopSurge() {
        CabinInventory inv = createInventory(100, 0, new BigDecimal("5000.00")); // 100% occupancy
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, inv, 1);

        assertThat(breakdown.getOccupancyRatio()).isEqualTo(1.0);
        assertThat(breakdown.getDemandAdjustmentPercent()).isEqualTo(30.0);
        assertThat(breakdown.getDemandAdjustment()).isEqualByComparingTo("1500.00");
    }
}
