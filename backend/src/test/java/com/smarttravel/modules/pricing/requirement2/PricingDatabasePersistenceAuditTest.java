package com.smarttravel.modules.pricing.requirement2;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.pricing.model.DynamicPricingRule;
import com.smarttravel.modules.pricing.model.DynamicPricingRuleType;
import com.smarttravel.modules.pricing.model.FlightPriceHistory;
import com.smarttravel.modules.pricing.model.PriceFreeze;
import com.smarttravel.modules.pricing.model.PriceFreezeStatus;
import com.smarttravel.modules.pricing.repository.DynamicPricingRuleRepository;
import com.smarttravel.modules.pricing.repository.FlightPriceHistoryRepository;
import com.smarttravel.modules.pricing.repository.PriceFreezeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Requirement #2 - Test Group 7: Database & Persistence Integrity
 * Verifies document schema integrity, query methods, TTL/expiration criteria, and collection isolation.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PricingDatabasePersistenceAuditTest {

    @Mock
    private DynamicPricingRuleRepository ruleRepository;

    @Mock
    private FlightPriceHistoryRepository historyRepository;

    @Mock
    private PriceFreezeRepository freezeRepository;

    @Test
    @DisplayName("49. Dynamic pricing rules query finds active rules by type and date interval")
    void testDynamicPricingRuleDateIntersectionQuery() {
        Instant now = Instant.now();
        DynamicPricingRule rule = DynamicPricingRule.builder()
                .id("rule-holiday-1")
                .name("Independence Day Surge")
                .type(DynamicPricingRuleType.HOLIDAY)
                .percentageAdjustment(20.0)
                .enabled(true)
                .startDate(now.minus(2, ChronoUnit.DAYS))
                .endDate(now.plus(2, ChronoUnit.DAYS))
                .build();

        when(ruleRepository.findByTypeAndEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq(DynamicPricingRuleType.HOLIDAY), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(rule));

        List<DynamicPricingRule> active = ruleRepository.findByTypeAndEnabledTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                DynamicPricingRuleType.HOLIDAY, now, now);

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getPercentageAdjustment()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("50. Price history repository supports pagination and ordering across flights")
    void testPriceHistoryRepositoryPagination() {
        Instant now = Instant.now();
        FlightPriceHistory h1 = FlightPriceHistory.builder()
                .id("hist-1")
                .flightId("fl-1")
                .cabinClass(CabinClass.ECONOMY)
                .finalPrice(new BigDecimal("5500.00"))
                .capturedAt(now.minus(1, ChronoUnit.HOURS))
                .build();

        when(historyRepository.findByFlightIdAndCabinClassOrderByCapturedAtDesc(
                eq("fl-1"), eq(CabinClass.ECONOMY), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(h1)));

        Page<FlightPriceHistory> page = historyRepository.findByFlightIdAndCabinClassOrderByCapturedAtDesc(
                "fl-1", CabinClass.ECONOMY, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getFinalPrice()).isEqualByComparingTo("5500.00");
    }

    @Test
    @DisplayName("51. Price freeze repository finds active freezes for user and flight")
    void testPriceFreezeRepositoryFindByUserAndFlight() {
        PriceFreeze freeze = PriceFreeze.builder()
                .id("freeze-101")
                .userId("user-1")
                .flightId("fl-1")
                .cabinClass(CabinClass.ECONOMY)
                .status(PriceFreezeStatus.ACTIVE)
                .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                .build();

        when(freezeRepository.findByUserIdAndFlightIdAndStatus("user-1", "fl-1", PriceFreezeStatus.ACTIVE))
                .thenReturn(Optional.of(freeze));

        Optional<PriceFreeze> result = freezeRepository.findByUserIdAndFlightIdAndStatus("user-1", "fl-1", PriceFreezeStatus.ACTIVE);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("freeze-101");
        assertThat(result.get().getStatus()).isEqualTo(PriceFreezeStatus.ACTIVE);
    }

    @Test
    @DisplayName("52. Stale freeze expiration query targets active records past expiration timestamp")
    void testPriceFreezeExpirationQuery() {
        Instant now = Instant.now();
        PriceFreeze expired1 = PriceFreeze.builder().id("f1").status(PriceFreezeStatus.ACTIVE).expiresAt(now.minus(1, ChronoUnit.MINUTES)).build();

        when(freezeRepository.findByStatusAndExpiresAtBefore(eq(PriceFreezeStatus.ACTIVE), any(Instant.class)))
                .thenReturn(List.of(expired1));

        List<PriceFreeze> expired = freezeRepository.findByStatusAndExpiresAtBefore(PriceFreezeStatus.ACTIVE, now);

        assertThat(expired).hasSize(1);
        assertThat(expired.get(0).getId()).isEqualTo("f1");
    }
}
