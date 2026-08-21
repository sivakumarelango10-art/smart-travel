package com.smarttravel.modules.flight.provider;

import com.smarttravel.modules.flight.config.AviationstackProperties;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackFlightDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Registry and dynamic router for FlightStatusProviders.
 * Automatically chooses between MockFlightStatusProvider and AviationstackFlightDataProvider based on configuration.
 */
@Component
public class FlightDataProviderRegistry {

    private static final Logger log = LoggerFactory.getLogger(FlightDataProviderRegistry.class);

    private final AviationstackProperties properties;
    private final MockFlightStatusProviderImpl mockProvider;
    private final AviationstackFlightDataProvider aviationstackProvider;

    public FlightDataProviderRegistry(AviationstackProperties properties,
                                     MockFlightStatusProviderImpl mockProvider,
                                     AviationstackFlightDataProvider aviationstackProvider) {
        this.properties = properties;
        this.mockProvider = mockProvider;
        this.aviationstackProvider = aviationstackProvider;
    }

    /**
     * Resolves the active FlightStatusProvider according to the configured provider mode.
     */
    public FlightStatusProvider getActiveProvider() {
        if (properties.isAviationstackMode()) {
            log.debug("Active flight status provider: AviationstackFlightDataProvider");
            return aviationstackProvider;
        }
        log.debug("Active flight status provider: MockFlightStatusProviderImpl");
        return mockProvider;
    }

    public MockFlightStatusProviderImpl getMockProvider() {
        return mockProvider;
    }

    public AviationstackFlightDataProvider getAviationstackProvider() {
        return aviationstackProvider;
    }
}
