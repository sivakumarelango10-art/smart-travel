package com.smarttravel.modules.flight.tracking.service;

import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.flight.tracking.model.TrackedFlight;
import com.smarttravel.modules.flight.tracking.repository.TrackedFlightRepository;
import com.smarttravel.modules.flight.websocket.FlightStatusEvent;
import com.smarttravel.modules.flight.websocket.FlightStatusWebSocketPublisher;
import com.smarttravel.modules.notification.service.WebPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Background synchronization service for actively tracked flights.
 * Periodically syncs tracked flights with MongoDB operational status transitions and simulation updates.
 * Emits WebSocket events and browser push notifications when meaningful operational changes occur.
 */
@Service
public class LiveFlightTrackingSyncService {

    private static final Logger log = LoggerFactory.getLogger(LiveFlightTrackingSyncService.class);

    private final TrackedFlightRepository trackedFlightRepository;
    private final FlightRepository flightRepository;
    private final FlightStatusProvider flightStatusProvider;
    private final FlightStatusWebSocketPublisher webSocketPublisher;
    private final WebPushService webPushService;

    @Autowired
    public LiveFlightTrackingSyncService(TrackedFlightRepository trackedFlightRepository,
                                        FlightRepository flightRepository,
                                        FlightStatusProvider flightStatusProvider,
                                        @Autowired(required = false) FlightStatusWebSocketPublisher webSocketPublisher,
                                        @Autowired(required = false) WebPushService webPushService) {
        this.trackedFlightRepository = trackedFlightRepository;
        this.flightRepository = flightRepository;
        this.flightStatusProvider = flightStatusProvider;
        this.webSocketPublisher = webSocketPublisher;
        this.webPushService = webPushService;
    }

    /**
     * Periodic sync runner for active tracked flights.
     */
    @Scheduled(fixedDelayString = "${smarttravel.flight.sync-interval-ms:30000}")
    public void syncTrackedFlights() {
        List<TrackedFlight> activeTracked = trackedFlightRepository.findByActiveTrue();
        if (activeTracked.isEmpty()) {
            return;
        }

        log.debug("Synchronizing {} actively tracked flights with database telemetry", activeTracked.size());
        for (TrackedFlight tf : activeTracked) {
            syncSingleFlight(tf);
        }
    }

    public boolean syncSingleFlight(TrackedFlight tf) {
        if (tf == null || !tf.isActive() || tf.getFlightNumber() == null) {
            return false;
        }

        try {
            Optional<FlightStatusSnapshot> snapshotOpt = flightStatusProvider.fetchLatestStatus(tf.getFlightNumber(), null);
            if (snapshotOpt.isEmpty()) {
                return false;
            }

            FlightStatusSnapshot snapshot = snapshotOpt.get();
            FlightStatus oldStatus = tf.getLastKnownStatus();
            FlightStatus newStatus = snapshot.status();

            boolean statusChanged = oldStatus != newStatus;
            boolean etaChanged = !Objects.equals(tf.getLastKnownEta(), snapshot.revisedArrivalTime());

            if (statusChanged || etaChanged) {
                log.info("Tracked flight {} status transition detected: {} -> {} (ETA: {})",
                        tf.getFlightNumber(), oldStatus, newStatus, snapshot.revisedArrivalTime());

                // 1. Update TrackedFlight document
                tf.setLastKnownStatus(newStatus);
                if (snapshot.revisedArrivalTime() != null) {
                    tf.setLastKnownEta(snapshot.revisedArrivalTime());
                }
                trackedFlightRepository.save(tf);

                // 2. Broadcast WebSocket Live Event
                if (webSocketPublisher != null) {
                    String eventId = String.format("event-%s-%s-%d", tf.getFlightNumber(), newStatus, System.currentTimeMillis());
                    FlightStatusEvent event = FlightStatusEvent.builder()
                            .eventId(eventId)
                            .flightId(tf.getFlightId())
                            .flightNumber(tf.getFlightNumber())
                            .previousStatus(oldStatus)
                            .status(newStatus)
                            .delayMinutes(snapshot.delayMinutes())
                            .delayReason(snapshot.delayReason())
                            .revisedDeparture(snapshot.revisedDepartureTime())
                            .estimatedArrival(snapshot.revisedArrivalTime())
                            .gate(snapshot.gate())
                            .terminal(snapshot.terminal())
                            .source("SIMULATED")
                            .build();

                    webSocketPublisher.publish(event);
                }

                // 4. Send Web Push Notification
                if (webPushService != null && tf.getFlightId() != null) {
                    String title = String.format("Flight %s: Status Update", tf.getFlightNumber());
                    String body = buildNotificationBody(tf.getFlightNumber(), newStatus, snapshot);
                    String url = "/tracked-flights";
                    webPushService.sendPushForFlight(tf.getFlightId(), title, body, url, "FLIGHT_STATUS_UPDATE");
                }

                return true;
            }
        } catch (Exception ex) {
            log.warn("Error syncing tracked flight {}: {}", tf.getFlightNumber(), ex.getMessage());
        }

        return false;
    }

    private String buildNotificationBody(String flightNumber, FlightStatus status, FlightStatusSnapshot snapshot) {
        if (status == FlightStatus.DELAYED && snapshot.delayMinutes() != null) {
            return String.format("Flight %s is delayed by %d minutes. %s",
                    flightNumber, snapshot.delayMinutes(), snapshot.delayReason() != null ? snapshot.delayReason() : "");
        }
        if (status == FlightStatus.BOARDING) {
            return String.format("Flight %s is now BOARDING at Terminal %s, Gate %s.",
                    flightNumber, snapshot.terminal(), snapshot.gate());
        }
        if (status == FlightStatus.DEPARTED) {
            return String.format("Flight %s has departed and is currently in flight.", flightNumber);
        }
        if (status == FlightStatus.ARRIVED) {
            return String.format("Flight %s has landed safely.", flightNumber);
        }
        if (status == FlightStatus.CANCELLED) {
            return String.format("Flight %s has been cancelled.", flightNumber);
        }
        return String.format("Flight %s status changed to %s.", flightNumber, status);
    }
}
