package com.smarttravel.modules.flight.provider.aviationstack;

import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.provider.FlightStatusProvider;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackFlightItem;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackFlightResponse;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * Production Aviationstack Flight Status Provider.
 * Integrates real external aviation telemetry data with automatic fallback to local database data.
 */
@Component
public class AviationstackFlightDataProvider implements FlightStatusProvider {

    private static final Logger log = LoggerFactory.getLogger(AviationstackFlightDataProvider.class);

    private final AviationstackClient aviationstackClient;
    private final AviationstackDataNormalizer normalizer;
    private final FlightRepository flightRepository;

    public AviationstackFlightDataProvider(AviationstackClient aviationstackClient,
                                          AviationstackDataNormalizer normalizer,
                                          FlightRepository flightRepository) {
        this.aviationstackClient = aviationstackClient;
        this.normalizer = normalizer;
        this.flightRepository = flightRepository;
    }

    @Override
    public String getProviderName() {
        return "AVIATIONSTACK";
    }

    @Override
    public boolean isLiveProvider() {
        return true;
    }

    @Override
    public Optional<FlightStatusSnapshot> fetchLatestStatus(String flightNumber, Instant scheduledDeparture) {
        if (flightNumber == null || flightNumber.isBlank()) {
            return Optional.empty();
        }

        try {
            boolean wasCached = aviationstackClient.getCacheManager() != null &&
                    aviationstackClient.getCacheManager().getCachedFlight(flightNumber).isPresent();

            Optional<AviationstackFlightResponse> liveResponse = aviationstackClient.getFlightStatus(flightNumber);
            if (liveResponse.isPresent() && !liveResponse.get().getData().isEmpty()) {
                AviationstackFlightItem item = liveResponse.get().getData().get(0);
                String source = wasCached ? "CACHED_AVIATIONSTACK" : "AVIATIONSTACK";
                FlightStatusSnapshot snapshot = normalizer.toFlightStatusSnapshot(item, flightNumber, source);
                if (snapshot != null) {
                    log.info("Aviationstack real-time telemetry snapshot retrieved for flight {}: status={}, source={}",
                            flightNumber, snapshot.status(), source);
                    return Optional.of(snapshot);
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to fetch live status from Aviationstack for flight {}. Falling back to local data. Reason: {}",
                    flightNumber, ex.getMessage());
        }

        // Graceful Fallback to Local Flight Document if Aviationstack is unavailable or quota is reached
        return flightRepository.findByFlightNumber(flightNumber).map(f -> {
            String term = (f.getDepartureAirport() != null && f.getDepartureAirport().getTerminal() != null)
                    ? f.getDepartureAirport().getTerminal()
                    : "T3";
            String gate = "Gate " + ((Math.abs(f.getFlightNumber().hashCode()) % 15) + 1);

            return new FlightStatusSnapshot(
                    f.getFlightNumber(),
                    f.getStatus(),
                    f.getDelayMinutes(),
                    f.getDelayReason(),
                    f.getRevisedDepartureTime(),
                    f.getEstimatedArrival(),
                    gate,
                    term,
                    "SIMULATED"
            );
        });
    }
}
