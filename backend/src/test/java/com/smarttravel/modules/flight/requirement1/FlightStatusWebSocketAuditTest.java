package com.smarttravel.modules.flight.requirement1;

import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.websocket.FlightStatusEvent;
import com.smarttravel.modules.flight.websocket.FlightStatusWebSocketPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Requirement #1 - Test Group C: Live WebSocket & STOMP Broadcast Audit
 * Verifies real-time event broadcasting, topic isolation, payload completeness,
 * and subscription resilience.
 */
@ExtendWith(MockitoExtension.class)
class FlightStatusWebSocketAuditTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private FlightStatusWebSocketPublisher webSocketPublisher;

    private Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.parse("2026-10-20T08:00:00Z");
    }

    @Test
    @DisplayName("19-20. WebSocket STOMP topic structure conforms to /topic/flight-status/{flightId}")
    void testStompTopicPrefixAndPublish() {
        FlightStatusEvent event = FlightStatusEvent.builder()
                .flightId("flight-del-bom-101")
                .flightNumber("6E-2041")
                .status(FlightStatus.DELAYED)
                .delayMinutes(45)
                .build();

        webSocketPublisher.publish(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/flight-status/flight-del-bom-101"), eq(event));
    }

    @Test
    @DisplayName("21-27. Published event contains full telemetry: ID, status, ETA, reason, revised times, gate/terminal")
    void testCompleteEventPayloadStructure() {
        Instant schedDep = baseTime;
        Instant revDep = baseTime.plus(60, ChronoUnit.MINUTES);
        Instant schedArr = baseTime.plus(120, ChronoUnit.MINUTES);
        Instant estArr = baseTime.plus(180, ChronoUnit.MINUTES);

        FlightStatusEvent event = FlightStatusEvent.builder()
                .flightId("flight-ai-902")
                .flightNumber("AI-902")
                .previousStatus(FlightStatus.SCHEDULED)
                .status(FlightStatus.DELAYED)
                .delayMinutes(60)
                .delayReason("Technical inspection and baggage load balance")
                .scheduledDeparture(schedDep)
                .revisedDeparture(revDep)
                .scheduledArrival(schedArr)
                .estimatedArrival(estArr)
                .gate("Gate 12B")
                .terminal("T3")
                .source("SIMULATION:cfg-902")
                .build();

        webSocketPublisher.publish(event);

        ArgumentCaptor<FlightStatusEvent> captor = ArgumentCaptor.forClass(FlightStatusEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/flight-status/flight-ai-902"), captor.capture());

        FlightStatusEvent captured = captor.getValue();
        assertThat(captured.getFlightId()).isEqualTo("flight-ai-902");
        assertThat(captured.getFlightNumber()).isEqualTo("AI-902");
        assertThat(captured.getPreviousStatus()).isEqualTo(FlightStatus.SCHEDULED);
        assertThat(captured.getStatus()).isEqualTo(FlightStatus.DELAYED);
        assertThat(captured.getDelayMinutes()).isEqualTo(60);
        assertThat(captured.getDelayReason()).isEqualTo("Technical inspection and baggage load balance");
        assertThat(captured.getScheduledDeparture()).isEqualTo(schedDep);
        assertThat(captured.getRevisedDeparture()).isEqualTo(revDep);
        assertThat(captured.getScheduledArrival()).isEqualTo(schedArr);
        assertThat(captured.getEstimatedArrival()).isEqualTo(estArr);
        assertThat(captured.getGate()).isEqualTo("Gate 12B");
        assertThat(captured.getTerminal()).isEqualTo("T3");
    }

    @Test
    @DisplayName("28-29. Topic isolation: Flight A event is published strictly to Flight A topic, NOT Flight B")
    void testTopicIsolationBetweenFlights() {
        FlightStatusEvent eventFlightA = FlightStatusEvent.builder()
                .flightId("flight-AAA")
                .flightNumber("6E-111")
                .status(FlightStatus.DELAYED)
                .build();

        FlightStatusEvent eventFlightB = FlightStatusEvent.builder()
                .flightId("flight-BBB")
                .flightNumber("UK-222")
                .status(FlightStatus.ON_TIME)
                .build();

        webSocketPublisher.publish(eventFlightA);
        webSocketPublisher.publish(eventFlightB);

        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/flight-status/flight-AAA"), eq(eventFlightA));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/flight-status/flight-BBB"), eq(eventFlightB));
        verify(messagingTemplate, never()).convertAndSend(eq("/topic/flight-status/flight-AAA"), eq(eventFlightB));
    }

    @Test
    @DisplayName("30-31. Simultaneous multi-flight publication handles concurrent topics cleanly")
    void testMultipleFlightConcurrentBroadcasts() {
        for (int i = 1; i <= 5; i++) {
            FlightStatusEvent e = FlightStatusEvent.builder()
                    .flightId("fl-" + i)
                    .flightNumber("FL-00" + i)
                    .status(FlightStatus.BOARDING)
                    .build();
            webSocketPublisher.publish(e);
        }

        verify(messagingTemplate, times(5)).convertAndSend(any(String.class), any(FlightStatusEvent.class));
    }

    @Test
    @DisplayName("32-33. Publisher handles null or invalid events gracefully without exception")
    void testNullOrIncompleteEventHandling() {
        webSocketPublisher.publish(null);

        FlightStatusEvent incomplete = new FlightStatusEvent();
        webSocketPublisher.publish(incomplete);

        verifyNoInteractions(messagingTemplate);
    }
}
