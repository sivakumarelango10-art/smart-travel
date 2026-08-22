package com.smarttravel.modules.flight.provider.aviationstack;

import com.smarttravel.modules.flight.config.AviationstackProperties;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackError;
import com.smarttravel.modules.flight.provider.aviationstack.dto.AviationstackFlightResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

/**
 * Dedicated server-side HTTP client for the Aviationstack API.
 * Encapsulates timeout handling, error masking, quota checks, and caching integration.
 */
@Component
public class AviationstackClient {

    private static final Logger log = LoggerFactory.getLogger(AviationstackClient.class);

    private final AviationstackProperties properties;
    private final AviationstackQuotaGuard quotaGuard;
    private final AviationstackCacheManager cacheManager;
    private final RestClient restClient;

    public AviationstackClient(AviationstackProperties properties,
                              AviationstackQuotaGuard quotaGuard,
                              AviationstackCacheManager cacheManager) {
        this.properties = properties;
        this.quotaGuard = quotaGuard;
        this.cacheManager = cacheManager;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(properties.getReadTimeoutMs());

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    /**
     * Searches flights by route criteria (origin, destination, optional date) with server-side caching and deduplication.
     */
    public Optional<AviationstackFlightResponse> searchFlights(String depIata, String arrIata, String flightDate) {
        if (!properties.isEnabled() || properties.getApiKey().isBlank()) {
            log.debug("Aviationstack search skipped: API integration disabled or API key not set.");
            return Optional.empty();
        }

        String cacheKey = buildSearchCacheKey(depIata, arrIata, flightDate);

        // 1. Check Cache Hit
        Optional<AviationstackCacheManager.CachedResponse<AviationstackFlightResponse>> cached = cacheManager.getCachedSearch(cacheKey);
        if (cached.isPresent()) {
            return Optional.of(cached.get().data());
        }

        // 2. Check Monthly Quota Guard
        if (!quotaGuard.isQuotaAvailable()) {
            log.warn("Aviationstack search rejected: Monthly quota reached. Returning empty response.");
            return Optional.empty();
        }

        // 3. Execute with In-Flight Request Deduplication / Coalescing
        AviationstackFlightResponse response = cacheManager.executeWithCoalescing(cacheKey, () -> {
            try {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                        .path("/v1/flights")
                        .queryParam("access_key", properties.getApiKey());

                if (depIata != null && !depIata.isBlank()) {
                    uriBuilder.queryParam("dep_iata", depIata.trim().toUpperCase());
                }
                if (arrIata != null && !arrIata.isBlank()) {
                    uriBuilder.queryParam("arr_iata", arrIata.trim().toUpperCase());
                }
                if (flightDate != null && !flightDate.isBlank()) {
                    uriBuilder.queryParam("flight_date", flightDate.trim());
                }
                uriBuilder.queryParam("limit", 15);

                URI uri = uriBuilder.build().toUri();
                log.info("Executing Aviationstack route search: dep={}, arr={}, date={}", depIata, arrIata, flightDate);

                quotaGuard.recordRequest();
                AviationstackFlightResponse result = restClient.get()
                        .uri(uri)
                        .retrieve()
                        .body(AviationstackFlightResponse.class);

                if (result != null && result.hasError()) {
                    log.warn("Aviationstack API returned business error: code={}, message={}",
                            result.getError().getCode(), result.getError().getMessage());
                    return null;
                }

                return result;
            } catch (HttpClientErrorException ex) {
                handleHttpError(ex.getStatusCode().value(), "Search flights");
                return null;
            } catch (HttpServerErrorException ex) {
                log.error("Aviationstack upstream 5xx server error (status: {}) during flight search", ex.getStatusCode());
                return null;
            } catch (ResourceAccessException ex) {
                log.error("Aviationstack connection timeout or network error during flight search: {}", ex.getMessage());
                return null;
            } catch (Exception ex) {
                log.error("Unexpected error querying Aviationstack API: {}", ex.getMessage());
                return null;
            }
        });

        if (response != null) {
            cacheManager.putCachedSearch(cacheKey, response);
            return Optional.of(response);
        }

        return Optional.empty();
    }

    /**
     * Queries single flight real-time status by IATA/ICAO flight number with caching and deduplication.
     */
    public Optional<AviationstackFlightResponse> getFlightStatus(String flightNumber) {
        if (!properties.isEnabled() || properties.getApiKey().isBlank() || flightNumber == null || flightNumber.isBlank()) {
            return Optional.empty();
        }

        String normalizedFlightNumber = flightNumber.trim().toUpperCase();
        if (!isRealIataFlightNumber(normalizedFlightNumber)) {
            log.debug("Skipping Aviationstack lookup for synthetic/test flight number: {}", normalizedFlightNumber);
            return Optional.empty();
        }

        // 1. Check Cache Hit
        Optional<AviationstackCacheManager.CachedResponse<AviationstackFlightResponse>> cached = cacheManager.getCachedFlight(normalizedFlightNumber);
        if (cached.isPresent()) {
            return Optional.of(cached.get().data());
        }

        // 2. Check Monthly Quota Guard
        if (!quotaGuard.isQuotaAvailable()) {
            log.debug("Aviationstack single flight status skipped: Monthly quota limit reached for flight {}.", normalizedFlightNumber);
            return Optional.empty();
        }

        // 3. Execute with In-Flight Request Deduplication / Coalescing
        AviationstackFlightResponse response = cacheManager.executeWithCoalescing("flight:" + normalizedFlightNumber, () -> {
            try {
                // Determine if IATA or ICAO code format
                String flightParam = normalizedFlightNumber.replace("-", "").replace(" ", "");
                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                        .path("/v1/flights")
                        .queryParam("access_key", properties.getApiKey())
                        .queryParam("flight_iata", flightParam)
                        .queryParam("limit", 5);

                URI uri = uriBuilder.build().toUri();
                log.info("Executing Aviationstack flight query for flight: {}", normalizedFlightNumber);

                quotaGuard.recordRequest();
                AviationstackFlightResponse result = restClient.get()
                        .uri(uri)
                        .retrieve()
                        .body(AviationstackFlightResponse.class);

                if (result != null && result.hasError()) {
                    log.warn("Aviationstack API error for flight {}: code={}, message={}",
                            normalizedFlightNumber, result.getError().getCode(), result.getError().getMessage());
                    return null;
                }

                // If flight_iata returned no rows, try flight_number query
                if (result != null && result.getData().isEmpty() && normalizedFlightNumber.contains("-")) {
                    String[] parts = normalizedFlightNumber.split("-");
                    if (parts.length == 2 && parts[1].matches("^[0-9]+$")) {
                        String flightNumOnly = parts[1];
                        UriComponentsBuilder numQuery = UriComponentsBuilder.fromUriString(properties.getBaseUrl())
                                .path("/v1/flights")
                                .queryParam("access_key", properties.getApiKey())
                                .queryParam("flight_number", flightNumOnly)
                                .queryParam("limit", 5);
                        quotaGuard.recordRequest();
                        result = restClient.get()
                                .uri(numQuery.build().toUri())
                                .retrieve()
                                .body(AviationstackFlightResponse.class);
                    }
                }

                return result;
            } catch (HttpClientErrorException ex) {
                handleHttpError(ex.getStatusCode().value(), "Get flight " + normalizedFlightNumber);
                return null;
            } catch (HttpServerErrorException ex) {
                log.warn("Aviationstack upstream 5xx server status ({}) for flight {}. Falling back.", ex.getStatusCode(), normalizedFlightNumber);
                return null;
            } catch (ResourceAccessException ex) {
                log.warn("Aviationstack connection timeout for flight {}: {}. Falling back.", normalizedFlightNumber, ex.getMessage());
                return null;
            } catch (Exception ex) {
                log.warn("Error querying Aviationstack for flight {}: {}. Falling back.", normalizedFlightNumber, ex.getMessage());
                return null;
            }
        });

        if (response != null && !response.getData().isEmpty()) {
            cacheManager.putCachedFlight(normalizedFlightNumber, response);
            return Optional.of(response);
        }

        return Optional.empty();
    }

    /**
     * Filters out internal synthetic/test flight numbers (e.g., CC-101-817F7B, TEST-1, SEC-12)
     * so that only authentic IATA flight numbers query the external API.
     */
    public boolean isRealIataFlightNumber(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            return false;
        }
        String clean = flightNumber.trim().toUpperCase();
        // Check for test/synthetic prefixes
        if (clean.startsWith("CC-") || clean.startsWith("TEST-") || clean.startsWith("SEC-") ||
                clean.startsWith("MOCK-") || clean.startsWith("SIM-") || clean.startsWith("ST-")) {
            return false;
        }
        // Valid IATA: 2-3 alphanumeric airline code + optional hyphen + 1-4 digits (e.g. AI-101, 6E204, LH-6396, BA112)
        return clean.matches("^[A-Z0-9]{2,3}-?[0-9]{1,4}[A-Z]?$");
    }

    private void handleHttpError(int statusCode, String action) {
        if (statusCode == 401) {
            log.warn("Aviationstack Authentication (401): Invalid or missing API key during {}. Using local fallback.", action);
        } else if (statusCode == 429) {
            log.warn("Aviationstack Rate Limit (429) during {}. Using cached/local flight fallback.", action);
        } else {
            log.warn("Aviationstack HTTP {} during {}. Using local fallback.", statusCode, action);
        }
    }

    public AviationstackCacheManager getCacheManager() {
        return cacheManager;
    }

    private String buildSearchCacheKey(String dep, String arr, String date) {
        return String.format("search:%s->%s:%s",
                dep != null ? dep.toUpperCase().trim() : "ANY",
                arr != null ? arr.toUpperCase().trim() : "ANY",
                date != null ? date.trim() : "ANY");
    }
}
