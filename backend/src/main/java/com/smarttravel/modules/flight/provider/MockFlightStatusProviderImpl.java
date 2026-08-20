package com.smarttravel.modules.flight.provider;

import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

/**
 * Standard Mock Flight Status Provider for development, internship simulation, and automated test environments.
 */
@Component
public class MockFlightStatusProviderImpl implements FlightStatusProvider {

    private final FlightRepository flightRepository;

    public MockFlightStatusProviderImpl(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public String getProviderName() {
        return "SMARTTRAVEL_MOCK_ENGINE";
    }

    @Override
    public boolean isLiveProvider() {
        return false;
    }

    @Override
    public Optional<FlightStatusSnapshot> fetchLatestStatus(String flightNumber, Instant scheduledDeparture) {
        if (flightNumber == null || flightNumber.trim().isEmpty()) {
            return Optional.empty();
        }

        return flightRepository.findByFlightNumber(flightNumber).map(f -> {
            String term = (f.getDepartureAirport() != null && f.getDepartureAirport().getTerminal() != null)
                    ? f.getDepartureAirport().getTerminal()
                    : "T3";
            String gate = "Gate " + ((Math.abs(f.getFlightNumber().hashCode()) % 15) + 1);

            return new FlightStatusSnapshot(
                    f.getFlightNumber(),
                    f.getStatus() != null ? f.getStatus() : FlightStatus.SCHEDULED,
                    f.getDelayMinutes(),
                    f.getDelayReason(),
                    f.getRevisedDepartureTime(),
                    f.getEstimatedArrival(),
                    gate,
                    term,
                    "MOCK_INTERNAL_SIMULATION"
            );
        });
    }
}
