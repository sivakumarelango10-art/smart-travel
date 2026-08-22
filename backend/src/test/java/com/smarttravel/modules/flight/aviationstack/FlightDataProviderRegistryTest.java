package com.smarttravel.modules.flight.aviationstack;

import com.smarttravel.modules.flight.config.AviationstackProperties;
import com.smarttravel.modules.flight.provider.FlightDataProviderRegistry;
import com.smarttravel.modules.flight.provider.FlightStatusProvider;
import com.smarttravel.modules.flight.provider.MockFlightStatusProviderImpl;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackFlightDataProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FlightDataProviderRegistryTest {

    @Mock
    private MockFlightStatusProviderImpl mockProvider;

    @Mock
    private AviationstackFlightDataProvider aviationstackProvider;

    @Test
    @DisplayName("1. Returns Mock provider when FLIGHT_DATA_PROVIDER is MOCK (default)")
    void testDefaultMockProvider() {
        AviationstackProperties properties = new AviationstackProperties();
        properties.setProvider("MOCK");

        FlightDataProviderRegistry registry = new FlightDataProviderRegistry(properties, mockProvider, aviationstackProvider);
        FlightStatusProvider active = registry.getActiveProvider();

        assertSame(mockProvider, active);
        assertFalse(properties.isAviationstackMode());
    }

    @Test
    @DisplayName("2. Returns Aviationstack provider when FLIGHT_DATA_PROVIDER is AVIATIONSTACK and API key is present")
    void testAviationstackProviderSwitch() {
        AviationstackProperties properties = new AviationstackProperties();
        properties.setProvider("AVIATIONSTACK");
        properties.setApiKey("test_mock_key");

        FlightDataProviderRegistry registry = new FlightDataProviderRegistry(properties, mockProvider, aviationstackProvider);
        FlightStatusProvider active = registry.getActiveProvider();

        assertSame(aviationstackProvider, active);
        assertTrue(properties.isAviationstackMode());
    }

    @Test
    @DisplayName("3. Falls back to Mock provider when API key is missing or empty even if AVIATIONSTACK mode requested")
    void testMissingApiKeyFallsBackToMock() {
        AviationstackProperties properties = new AviationstackProperties();
        properties.setProvider("AVIATIONSTACK");
        properties.setApiKey("");

        FlightDataProviderRegistry registry = new FlightDataProviderRegistry(properties, mockProvider, aviationstackProvider);
        FlightStatusProvider active = registry.getActiveProvider();

        assertSame(mockProvider, active);
        assertFalse(properties.isAviationstackMode());
    }
}
