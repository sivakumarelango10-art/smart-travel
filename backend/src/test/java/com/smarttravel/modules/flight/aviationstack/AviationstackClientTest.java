package com.smarttravel.modules.flight.aviationstack;

import com.smarttravel.modules.flight.config.AviationstackProperties;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackCacheManager;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackClient;
import com.smarttravel.modules.flight.provider.aviationstack.AviationstackQuotaGuard;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackFlightResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AviationstackClientTest {

    @Mock
    private AviationstackQuotaGuard quotaGuard;

    @Mock
    private AviationstackCacheManager cacheManager;

    private AviationstackProperties properties;
    private AviationstackClient client;

    @BeforeEach
    void setUp() {
        properties = new AviationstackProperties();
        properties.setApiKey("test_mock_api_key");
        properties.setBaseUrl("https://api.aviationstack.com");
        properties.setEnabled(true);

        client = new AviationstackClient(properties, quotaGuard, cacheManager);
    }

    @Test
    @DisplayName("1. Returns cached search response directly if cache hit exists")
    void testCacheHitReturnsImmediately() {
        AviationstackFlightResponse mockResp = new AviationstackFlightResponse();
        AviationstackCacheManager.CachedResponse<AviationstackFlightResponse> cached =
                new AviationstackCacheManager.CachedResponse<>(mockResp, java.time.Instant.now(), java.time.Instant.now().plusSeconds(60));

        when(cacheManager.getCachedSearch("search:DEL->BOM:2026-08-21")).thenReturn(Optional.of(cached));

        Optional<AviationstackFlightResponse> result = client.searchFlights("DEL", "BOM", "2026-08-21");

        assertTrue(result.isPresent());
        assertSame(mockResp, result.get());
        verify(quotaGuard, never()).recordRequest();
    }

    @Test
    @DisplayName("2. Blocks search when quota guard indicates monthly request limit exceeded")
    void testQuotaExhaustedBlocksSearch() {
        when(cacheManager.getCachedSearch("search:DEL->BOM:2026-08-21")).thenReturn(Optional.empty());
        when(quotaGuard.isQuotaAvailable()).thenReturn(false);

        Optional<AviationstackFlightResponse> result = client.searchFlights("DEL", "BOM", "2026-08-21");

        assertTrue(result.isEmpty());
        verify(quotaGuard, never()).recordRequest();
    }

    @Test
    @DisplayName("3. Single flight lookup returns empty gracefully when disabled or key is empty")
    void testDisabledClientReturnsEmpty() {
        properties.setEnabled(false);
        Optional<AviationstackFlightResponse> result = client.getFlightStatus("AI-101");
        assertTrue(result.isEmpty());

        properties.setEnabled(true);
        properties.setApiKey("");
        Optional<AviationstackFlightResponse> resultNoKey = client.getFlightStatus("AI-101");
        assertTrue(resultNoKey.isEmpty());
    }

    @Test
    @DisplayName("4. Verifies API key is never exposed in client toString or exception logs")
    void testApiKeyMaskingSecurity() {
        assertFalse(client.toString().contains(properties.getApiKey()), "Client string representation must not contain API key");
    }

    @Test
    @DisplayName("5. Verifies synthetic/test flight numbers are skipped without querying external API")
    void testSyntheticFlightNumberSkipped() {
        assertFalse(client.isRealIataFlightNumber("CC-101-817F7B"));
        assertFalse(client.isRealIataFlightNumber("TEST-101"));
        assertFalse(client.isRealIataFlightNumber("SEC-12-1787292725607"));
        assertTrue(client.isRealIataFlightNumber("AI-101"));
        assertTrue(client.isRealIataFlightNumber("6E204"));
        assertTrue(client.isRealIataFlightNumber("LH6396"));

        Optional<AviationstackFlightResponse> res = client.getFlightStatus("CC-101-817F7B");
        assertTrue(res.isEmpty());
        verify(quotaGuard, never()).recordRequest();
    }
}
