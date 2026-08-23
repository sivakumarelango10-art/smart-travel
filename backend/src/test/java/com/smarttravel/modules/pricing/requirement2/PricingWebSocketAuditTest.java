package com.smarttravel.modules.pricing.requirement2;

import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.pricing.event.DynamicPricingEvent;
import com.smarttravel.modules.pricing.repository.DynamicPricingRuleRepository;
import com.smarttravel.modules.pricing.repository.FlightPriceHistoryRepository;
import com.smarttravel.modules.pricing.service.DynamicPricingServiceImpl;
import com.smarttravel.modules.pricing.websocket.PricingWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Requirement #2 - Test Group 5: Real-Time Dynamic Pricing WebSockets
 * Verifies that dynamic pricing events are properly formed and broadcast to /topic/pricing/{flightId}.
 */
@ExtendWith(MockitoExtension.class)
class PricingWebSocketAuditTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private DynamicPricingRuleRepository ruleRepository;

    @Mock
    private FlightPriceHistoryRepository historyRepository;

    private PricingWebSocketPublisher pricingWebSocketPublisher;
    private DynamicPricingServiceImpl pricingService;

    private Flight flight;
    private CabinInventory cabinInventory;

    @BeforeEach
    void setUp() {
        pricingWebSocketPublisher = new PricingWebSocketPublisher(messagingTemplate);
        pricingService = new DynamicPricingServiceImpl(ruleRepository, historyRepository, pricingWebSocketPublisher);

        cabinInventory = CabinInventory.builder()
                .cabinClass(CabinClass.ECONOMY)
                .totalSeats(100)
                .availableSeats(20) // 80% occupancy
                .basePrice(new BigDecimal("5000.00"))
                .feeAmount(new BigDecimal("150.00"))
                .build();

        flight = Flight.builder()
                .id("fl-ws-01")
                .flightNumber("6E-551")
                .cabinInventories(List.of(cabinInventory))
                .build();
    }

    @Test
    @DisplayName("37. Pricing event contains all required operational and calculation fields")
    void testDynamicPricingEventFields() {
        DynamicPricingEvent event = DynamicPricingEvent.builder()
                .flightId("fl-ws-01")
                .flightNumber("6E-551")
                .cabinClass(CabinClass.ECONOMY)
                .oldPrice(new BigDecimal("5750.00"))
                .newPrice(new BigDecimal("6870.00"))
                .demandAdjustmentPercent(20.0)
                .availableSeats(15)
                .occupancyRatio(0.85)
                .reason("High demand (85% seats filled)")
                .build();

        assertThat(event.getFlightId()).isEqualTo("fl-ws-01");
        assertThat(event.getFlightNumber()).isEqualTo("6E-551");
        assertThat(event.getCabinClass()).isEqualTo(CabinClass.ECONOMY);
        assertThat(event.getOldPrice()).isEqualByComparingTo("5750.00");
        assertThat(event.getNewPrice()).isEqualByComparingTo("6870.00");
        assertThat(event.getDemandAdjustmentPercent()).isEqualTo(20.0);
        assertThat(event.getAvailableSeats()).isEqualTo(15);
        assertThat(event.getReason()).contains("High demand");
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("38. PricingWebSocketPublisher broadcasts to /topic/pricing/{flightId}")
    void testWebSocketBroadcastingToTopic() {
        DynamicPricingEvent event = DynamicPricingEvent.builder()
                .flightId("fl-ws-01")
                .flightNumber("6E-551")
                .cabinClass(CabinClass.ECONOMY)
                .newPrice(new BigDecimal("6870.00"))
                .build();

        pricingWebSocketPublisher.publish(event);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/pricing/fl-ws-01"), eq(event));
    }

    @Test
    @DisplayName("39. publishPriceUpdate in service calculates current fare and publishes event")
    void testPublishPriceUpdateEndToEnd() {
        when(ruleRepository.findByTypeAndEnabledTrue(any())).thenReturn(Collections.emptyList());

        pricingService.publishPriceUpdate(flight, cabinInventory, new BigDecimal("5750.00"));

        ArgumentCaptor<DynamicPricingEvent> captor = ArgumentCaptor.forClass(DynamicPricingEvent.class);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/pricing/fl-ws-01"), captor.capture());

        DynamicPricingEvent published = captor.getValue();
        assertThat(published.getFlightId()).isEqualTo("fl-ws-01");
        assertThat(published.getFlightNumber()).isEqualTo("6E-551");
        assertThat(published.getOldPrice()).isEqualByComparingTo("5750.00");
        assertThat(published.getNewPrice()).isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("40. Null or incomplete event is safely ignored without throwing")
    void testNullEventHandledGracefully() {
        pricingWebSocketPublisher.publish(null);
        pricingWebSocketPublisher.publish(new DynamicPricingEvent());

        verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    @DisplayName("41. WebSocket publish failure does not break the business flow")
    void testWebSocketFailureResilience() {
        doThrow(new RuntimeException("STOMP broker disconnect"))
                .when(messagingTemplate).convertAndSend(anyString(), any(Object.class));

        DynamicPricingEvent event = DynamicPricingEvent.builder()
                .flightId("fl-ws-01")
                .build();

        // Must not throw uncaught exception
        pricingWebSocketPublisher.publish(event);
    }

    @Test
    @DisplayName("42. Topic isolation ensures updates only broadcast to specific flight channel")
    void testTopicIsolation() {
        DynamicPricingEvent event1 = DynamicPricingEvent.builder().flightId("flight-AAA").build();
        DynamicPricingEvent event2 = DynamicPricingEvent.builder().flightId("flight-BBB").build();

        pricingWebSocketPublisher.publish(event1);
        pricingWebSocketPublisher.publish(event2);

        verify(messagingTemplate).convertAndSend(eq("/topic/pricing/flight-AAA"), eq(event1));
        verify(messagingTemplate).convertAndSend(eq("/topic/pricing/flight-BBB"), eq(event2));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/pricing/flight-CCC"), any(Object.class));
    }
}
