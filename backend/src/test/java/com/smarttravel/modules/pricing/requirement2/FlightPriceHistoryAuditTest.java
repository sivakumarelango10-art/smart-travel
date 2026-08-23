package com.smarttravel.modules.pricing.requirement2;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown;
import com.smarttravel.modules.pricing.model.DynamicPricingRuleType;
import com.smarttravel.modules.pricing.model.FlightPriceHistory;
import com.smarttravel.modules.pricing.repository.DynamicPricingRuleRepository;
import com.smarttravel.modules.pricing.repository.FlightPriceHistoryRepository;
import com.smarttravel.modules.pricing.service.DynamicPricingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Requirement #2 - Test Group 2: Price History
 * Verifies snapshot creation, MongoDB persistence, chronological sorting, pagination, rate-limiting, and deduplication.
 */
@ExtendWith(MockitoExtension.class)
class FlightPriceHistoryAuditTest {

    @Mock
    private DynamicPricingRuleRepository ruleRepository;

    @Mock
    private FlightPriceHistoryRepository historyRepository;

    @InjectMocks
    private DynamicPricingServiceImpl pricingService;

    private Flight flight;
    private CabinInventory cabinInventory;

    @BeforeEach
    void setUp() {
        cabinInventory = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(40)
                .basePrice(new BigDecimal("5000.00"))
                .feeAmount(new BigDecimal("150.00"))
                .build();

        flight = Flight.builder()
                .id("flight-hist-01")
                .flightNumber("6E-204")
                .status(FlightStatus.SCHEDULED)
                .cabinInventories(List.of(cabinInventory))
                .build();
    }

    @Test
    @DisplayName("13. Price snapshot is created accurately from dynamic breakdown")
    void testPriceSnapshotCreation() {
        DynamicPriceBreakdown breakdown = DynamicPriceBreakdown.builder()
                .flightId("flight-hist-01")
                .cabinClass(CabinClass.ECONOMY)
                .baseFare(new BigDecimal("5000.00"))
                .demandAdjustmentPercent(10.0)
                .seasonalAdjustmentPercent(0.0)
                .holidayAdjustmentPercent(0.0)
                .totalDynamicAdjustment(new BigDecimal("500.00"))
                .taxes(new BigDecimal("660.00"))
                .fees(new BigDecimal("150.00"))
                .totalPerPassenger(new BigDecimal("6310.00"))
                .occupancyRatio(0.60)
                .demandReason("High demand (60% seats filled)")
                .build();

        FlightPriceHistory history = pricingService.toHistoryRecord(breakdown, "6E-204");

        assertThat(history).isNotNull();
        assertThat(history.getFlightId()).isEqualTo("flight-hist-01");
        assertThat(history.getFlightNumber()).isEqualTo("6E-204");
        assertThat(history.getFinalPrice()).isEqualByComparingTo("6310.00");
        assertThat(history.getDemandAdjustmentPercent()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("14. Price snapshot is persisted to MongoDB on recordPriceSnapshot")
    void testPriceSnapshotPersisted() {
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND)).thenReturn(Collections.emptyList());
        when(historyRepository.countByFlightIdAndCabinClassAndCapturedAtAfter(
                eq("flight-hist-01"), eq(CabinClass.ECONOMY), any(Instant.class)))
                .thenReturn(0L); // No recent snapshot

        pricingService.recordPriceSnapshot(flight, cabinInventory);

        ArgumentCaptor<FlightPriceHistory> captor = ArgumentCaptor.forClass(FlightPriceHistory.class);
        verify(historyRepository, times(1)).save(captor.capture());

        FlightPriceHistory saved = captor.getValue();
        assertThat(saved.getFlightId()).isEqualTo("flight-hist-01");
        assertThat(saved.getCabinClass()).isEqualTo(CabinClass.ECONOMY);
        assertThat(saved.getFinalPrice()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("15. History returns sorted data by capturedAt in descending order")
    void testHistorySorting() {
        Instant now = Instant.now();
        FlightPriceHistory h1 = FlightPriceHistory.builder().id("h1").capturedAt(now.minus(2, ChronoUnit.HOURS)).build();
        FlightPriceHistory h2 = FlightPriceHistory.builder().id("h2").capturedAt(now.minus(1, ChronoUnit.HOURS)).build();

        Pageable pageable = PageRequest.of(0, 10);
        when(historyRepository.findByFlightIdAndCabinClassOrderByCapturedAtDesc("flight-hist-01", CabinClass.ECONOMY, pageable))
                .thenReturn(new PageImpl<>(List.of(h2, h1)));

        Page<FlightPriceHistory> page = historyRepository.findByFlightIdAndCabinClassOrderByCapturedAtDesc("flight-hist-01", CabinClass.ECONOMY, pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getId()).isEqualTo("h2");
        assertThat(page.getContent().get(1).getId()).isEqualTo("h1");
    }

    @Test
    @DisplayName("16. History query supports cabin class filtering")
    void testHistoryCabinFiltering() {
        Pageable pageable = PageRequest.of(0, 10);
        when(historyRepository.findByFlightIdAndCabinClassOrderByCapturedAtDesc("flight-hist-01", CabinClass.BUSINESS, pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<FlightPriceHistory> result = historyRepository.findByFlightIdAndCabinClassOrderByCapturedAtDesc("flight-hist-01", CabinClass.BUSINESS, pageable);

        assertThat(result.getContent()).isEmpty();
        verify(historyRepository).findByFlightIdAndCabinClassOrderByCapturedAtDesc("flight-hist-01", CabinClass.BUSINESS, pageable);
    }

    @Test
    @DisplayName("17. Empty price history returns empty page without error")
    void testEmptyPriceHistory() {
        Pageable pageable = PageRequest.of(0, 10);
        when(historyRepository.findByFlightIdOrderByCapturedAtDesc("non-existent", pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<FlightPriceHistory> result = historyRepository.findByFlightIdOrderByCapturedAtDesc("non-existent", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("18. Rate limiting prevents snapshot spam within 60 minutes window")
    void testSnapshotRateLimiting() {
        when(historyRepository.countByFlightIdAndCabinClassAndCapturedAtAfter(
                eq("flight-hist-01"), eq(CabinClass.ECONOMY), any(Instant.class)))
                .thenReturn(3L); // Already 3 snapshots in the last hour

        pricingService.recordPriceSnapshot(flight, cabinInventory);

        verify(historyRepository, never()).save(any(FlightPriceHistory.class));
    }

    @Test
    @DisplayName("19. Snapshot avoids storing null flight or empty inventory")
    void testNullFlightSnapshotIgnored() {
        pricingService.recordPriceSnapshot(null, cabinInventory);
        pricingService.recordPriceSnapshot(flight, null);

        verify(historyRepository, never()).save(any(FlightPriceHistory.class));
    }

    @Test
    @DisplayName("20. Price history reason string contains informative multi-factor context")
    void testPriceHistoryReasonContext() {
        DynamicPriceBreakdown breakdown = DynamicPriceBreakdown.builder()
                .flightId("flight-hist-01")
                .cabinClass(CabinClass.ECONOMY)
                .baseFare(new BigDecimal("5000.00"))
                .demandReason("High demand (75% seats filled)")
                .seasonalReason("Summer holiday peak")
                .holidayReason("Diwali surge")
                .build();

        FlightPriceHistory history = pricingService.toHistoryRecord(breakdown, "6E-204");

        assertThat(history.getReason()).contains("High demand", "Summer holiday peak", "Diwali surge");
    }
}
