package com.smarttravel.modules.flight.provider;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.provider.FlightStatusProvider.FlightStatusSnapshot;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalFlightDataProviderTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private MockFlightStatusProviderImpl internalProvider;

    private Flight sampleFlight;

    @BeforeEach
    void setUp() {
        sampleFlight = new Flight();
        sampleFlight.setId("fl-test-01");
        sampleFlight.setFlightNumber("AI-101");
        sampleFlight.setAirline("Air India");
        sampleFlight.setAirlineCode("AI");
        sampleFlight.setDepartureAirport(new AirportInfo("DEL", "Indira Gandhi International Airport", "New Delhi", "India", "T3", "Gate 12"));
        sampleFlight.setArrivalAirport(new AirportInfo("BOM", "Chhatrapati Shivaji Maharaj International Airport", "Mumbai", "India", "T2", "Gate 04"));
        sampleFlight.setDepartureTime(Instant.now().plus(2, ChronoUnit.HOURS));
        sampleFlight.setArrivalTime(Instant.now().plus(4, ChronoUnit.HOURS));
        sampleFlight.setStatus(FlightStatus.DELAYED);
        sampleFlight.setDelayMinutes(45);
        sampleFlight.setDelayReason("Late incoming aircraft from London");
        sampleFlight.setAircraftModel("Boeing 787-8");
        sampleFlight.setActive(true);
    }

    @Test
    @DisplayName("1. Returns provider metadata correctly as internal self-contained provider")
    void testProviderMetadata() {
        assertThat(internalProvider.getProviderName()).isEqualTo("SMARTTRAVEL_INTERNAL_SIMULATION");
        assertThat(internalProvider.isLiveProvider()).isFalse();
    }

    @Test
    @DisplayName("2. Fetches flight snapshot accurately from MongoDB flight document")
    void testFetchLatestStatusFromDatabase() {
        when(flightRepository.findByFlightNumber("AI-101")).thenReturn(Optional.of(sampleFlight));

        Optional<FlightStatusSnapshot> result = internalProvider.fetchLatestStatus("AI-101", null);

        assertThat(result).isPresent();
        FlightStatusSnapshot snapshot = result.get();
        assertThat(snapshot.flightNumber()).isEqualTo("AI-101");
        assertThat(snapshot.airline()).isEqualTo("Air India");
        assertThat(snapshot.airlineCode()).isEqualTo("AI");
        assertThat(snapshot.status()).isEqualTo(FlightStatus.DELAYED);
        assertThat(snapshot.delayMinutes()).isEqualTo(45);
        assertThat(snapshot.delayReason()).isEqualTo("Late incoming aircraft from London");
        assertThat(snapshot.originCode()).isEqualTo("DEL");
        assertThat(snapshot.destCode()).isEqualTo("BOM");
        assertThat(snapshot.updatedSource()).isEqualTo("MOCK_INTERNAL_SIMULATION");
        assertThat(snapshot.aircraftModel()).isEqualTo("Boeing 787-8");
    }

    @Test
    @DisplayName("3. Returns empty optional when flight number does not exist in database")
    void testFetchLatestStatusNotFound() {
        when(flightRepository.findByFlightNumber("NON-EXISTENT")).thenReturn(Optional.empty());

        Optional<FlightStatusSnapshot> result = internalProvider.fetchLatestStatus("NON-EXISTENT", null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("4. Gracefully handles null or empty flight numbers")
    void testFetchLatestStatusNullOrBlank() {
        assertThat(internalProvider.fetchLatestStatus(null, null)).isEmpty();
        assertThat(internalProvider.fetchLatestStatus("   ", null)).isEmpty();
    }
}
