package com.smarttravel.modules.flight.tracking.service;

import com.smarttravel.modules.flight.config.AviationstackProperties;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackFlightDataProvider;
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
 * In Aviationstack mode, periodically polls telemetry safely within the free-tier cache and quota rules.
 * Emits WebSocket events and browser push notifications ONLY when meaningful operational changes occur (deduplication).
 */
@Service
public class LiveFlightTrackingSyncService {

    private static final Logger log = LoggerFactory.getLogger(LiveFlightTrackingSyncService.class);

    private final AviationstackProperties properties;
    private final TrackedFlightRepository trackedFlightRepository;
    private final FlightRepository flightRepository;
    private final AviationstackFlightDataProvider aviationstackProvider;
    private final FlightStatusWebSocketPublisher webSocketPublisher;
    private final WebPushService webPushService;

    @Autowired
    public LiveFlightTrackingSyncService(AviationstackProperties properties,
                                        TrackedFlightRepository trackedFlightRepository,
                                        FlightRepository flightRepository,
                                        AviationstackFlightDataProvider aviationstackProvider,
                                        @Autowired(required = false) FlightStatusWebSocketPublisher webSocketPublisher,
                                        @Autowired(required = false) WebPushService webPushService) {
        this.properties = properties;
        this.trackedFlightRepository = trackedFlightRepository;
        this.flightRepository = flightRepository;
        this.aviationstackProvider = aviationstackProvider;
        this.webSocketPublisher = webSocketPublisher;
        this.webPushService = webPushService;
    }

    /**
     * Periodic sync runner. Runs every 60 seconds (aligned with flight cache TTL).
     */
    @Scheduled(fixedDelayString = "${smarttravel.flight.aviationstack.sync-interval-ms:60000}")
    public void syncTrackedFlights() {
        if (!properties.isAviationstackMode() || !properties.isEnabled()) {
            return;
        }

        List<TrackedFlight> activeTracked = trackedFlightRepository.findByActiveTrue();
        if (activeTracked.isEmpty()) {
            return;
        }

        log.debug("Synchronizing {} actively tracked flights with Aviationstack live telemetry", activeTracked.size());
        for (TrackedFlight tf : activeTracked) {
            syncSingleFlight(tf);
        }
    }

    public boolean syncSingleFlight(TrackedFlight tf) {
        if (tf == null || !tf.isActive() || tf.getFlightNumber() == null) {
            return false;
        }

        try {
            Optional<FlightStatusSnapshot> snapshotOpt = aviationstackProvider.fetchLatestStatus(tf.getFlightNumber(), null);
            if (snapshotOpt.isEmpty()) {
                return false;
            }

            FlightStatusSnapshot snapshot = snapshotOpt.get();
            FlightStatus oldStatus = tf.getLastKnownStatus();
            FlightStatus newStatus = snapshot.status();

            boolean statusChanged = oldStatus != newStatus;
            boolean etaChanged = !Objects.equals(tf.getLastKnownEta(), snapshot.revisedArrivalTime());
            boolean delayChanged = snapshot.delayMinutes() != null && snapshot.delayMinutes() > 0;

            if (statusChanged || etaChanged) {
                log.info("Tracked flight {} status transition detected: {} -> {} (ETA: {})",
                        tf.getFlightNumber(), oldStatus, newStatus, snapshot.revisedArrivalTime());

                // 1. Update TrackedFlight document
                tf.setLastKnownStatus(newStatus);
                if (snapshot.revisedArrivalTime() != null) {
                    tf.setLastKnownEta(snapshot.revisedArrivalTime());
                }
                trackedFlightRepository.save(tf);

                // 2. Update local MongoDB flight document if present
                Optional<Flight> flightOpt = flightRepository.findById(tf.getFlightId());
                if (flightOpt.isPresent()) {
                    Flight flight = flightOpt.get();
                    flight.setStatus(newStatus);
                    if (snapshot.delayMinutes() != null) {
                        flight.setDelayMinutes(snapshot.delayMinutes());
                    }
                    if (snapshot.delayReason() != null) {
                        flight.setDelayReason(snapshot.delayReason());
                    }
                    if (snapshot.revisedDepartureTime() != null) {
                        flight.setRevisedDepartureTime(snapshot.revisedDepartureTime());
                    }
                    if (snapshot.revisedArrivalTime() != null) {
                        flight.setEstimatedArrival(snapshot.revisedArrivalTime());
                    }
                    flightRepository.save(flight);
                }

                // 3. Broadcast WebSocket Live Event
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
                            .source("AVIATIONSTACK")
                            .build();

                    webSocketPublisher.publish(event);
                }

                // 4. Send Web Push Notification
                if (webPushService != null) {
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
