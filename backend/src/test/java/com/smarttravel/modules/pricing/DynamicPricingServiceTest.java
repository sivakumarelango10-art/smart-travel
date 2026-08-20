package com.smarttravel.modules.pricing;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.pricing.dto.DynamicPriceBreakdown;
import com.smarttravel.modules.pricing.model.DynamicPricingRule;
import com.smarttravel.modules.pricing.model.DynamicPricingRuleType;
import com.smarttravel.modules.pricing.model.FlightPriceHistory;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamicPricingServiceTest {

    @Mock
    private DynamicPricingRuleRepository ruleRepository;

    @Mock
    private FlightPriceHistoryRepository historyRepository;

    @InjectMocks
    private DynamicPricingServiceImpl pricingService;

    private Flight flight;
    private CabinInventory economyInventory;

    @BeforeEach
    void setUp() {
        economyInventory = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(25) // 75% occupancy -> High Demand (+10%)
                .basePrice(new BigDecimal("5000.00"))
                .feeAmount(new BigDecimal("150.00"))
                .build();

        flight = Flight.builder()
                .id("flight-01")
                .flightNumber("ST-101")
                .airline("SmartTravel Air")
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").city("Mumbai").build())
                .departureTime(Instant.now().plusSeconds(86400))
                .arrivalTime(Instant.now().plusSeconds(86400 + 7200))
                .status(FlightStatus.SCHEDULED)
                .cabinInventories(List.of(economyInventory))
                .build();
    }

    @Test
    @DisplayName("calculateDynamicPrice applies 10% surge for 75% occupancy with 12% GST tax")
    void testCalculateDynamicPrice_DemandSurge() {
        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND))
                .thenReturn(Collections.emptyList()); // Uses default demand band (60-80% -> +10%)

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, economyInventory, 1);

        assertThat(breakdown).isNotNull();
        assertThat(breakdown.getBaseFare()).isEqualByComparingTo("5000.00");
        // 10% demand surge on 5000 = +500
        assertThat(breakdown.getDemandAdjustment()).isEqualByComparingTo("500.00");
        assertThat(breakdown.getDemandAdjustmentPercent()).isEqualTo(10.0);
        // Adjusted base = 5500. GST 12% on 5500 = 660
        assertThat(breakdown.getTaxes()).isEqualByComparingTo("660.00");
        // Fees = 150
        assertThat(breakdown.getFees()).isEqualByComparingTo("150.00");
        // Total per pax = 5500 + 660 + 150 = 6310
        assertThat(breakdown.getTotalPerPassenger()).isEqualByComparingTo("6310.00");
        assertThat(breakdown.getGrandTotal()).isEqualByComparingTo("6310.00");
    }

    @Test
    @DisplayName("calculateDynamicPrice applies DB-configured dynamic rules with priority")
    void testCalculateDynamicPrice_CustomRules() {
        DynamicPricingRule customDemand = DynamicPricingRule.builder()
                .name("Super High Demand")
                .type(DynamicPricingRuleType.DEMAND)
                .percentageAdjustment(25.0)
                .minOccupancyThreshold(0.70)
                .maxOccupancyThreshold(0.90)
                .description("Super Surge")
                .build();

        when(ruleRepository.findByTypeAndEnabledTrue(DynamicPricingRuleType.DEMAND))
                .thenReturn(List.of(customDemand));

        DynamicPriceBreakdown breakdown = pricingService.calculateDynamicPrice(flight, economyInventory, 2);

        assertThat(breakdown.getDemandAdjustmentPercent()).isEqualTo(25.0);
        // Base 5000 * 25% = 1250. Adjusted = 6250. Tax 12% = 750. Fee = 150. TotalPerPax = 7150.
        assertThat(breakdown.getTotalPerPassenger()).isEqualByComparingTo("7150.00");
        // 2 passengers = 14300
        assertThat(breakdown.getGrandTotal()).isEqualByComparingTo("14300.00");
    }

    @Test
    @DisplayName("recordPriceSnapshot rate limits snapshots within 60 minutes")
    void testRecordPriceSnapshot_RateLimiting() {
        when(historyRepository.countByFlightIdAndCabinClassAndCapturedAtAfter(
                eq("flight-01"), eq(CabinClass.ECONOMY), any(Instant.class)))
                .thenReturn(1L); // Already captured recently

        pricingService.recordPriceSnapshot(flight, economyInventory);

        verify(historyRepository, never()).save(any(FlightPriceHistory.class));
    }
}
