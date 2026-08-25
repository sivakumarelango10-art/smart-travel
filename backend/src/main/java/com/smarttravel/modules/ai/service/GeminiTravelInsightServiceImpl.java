package com.smarttravel.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.modules.ai.dto.FlightDelayExplanationResponse;
import com.smarttravel.modules.ai.dto.TravelInsightResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Production-grade Gemini AI Service for SmartTravel.
 * Integrates Google Gemini 1.5 Flash via REST API with strict timeouts,
 * in-memory deduplication caching, and deterministic offline fallbacks.
 */
@Service
public class GeminiTravelInsightServiceImpl implements GeminiTravelInsightService {

    private static final Logger log = LoggerFactory.getLogger(GeminiTravelInsightServiceImpl.class);

    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models";

    @Value("${app.gemini.api-key:${GEMINI_API_KEY:}}")
    private String apiKey;

    @Value("${app.gemini.model:gemini-1.5-flash}")
    private String modelName;

    @Value("${app.gemini.timeout-seconds:6}")
    private int timeoutSeconds;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    // Fast in-memory cache for repeated insight queries
    private final Map<String, TravelInsightResponse> insightsCache = new ConcurrentHashMap<>();
    private final Map<String, FlightDelayExplanationResponse> delayCache = new ConcurrentHashMap<>();

    public GeminiTravelInsightServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Override
    public TravelInsightResponse generateTravelInsights(String destinationCity, String travelType) {
        String city = (destinationCity != null && !destinationCity.isBlank()) ? destinationCity.trim() : "Mumbai";
        String type = (travelType != null && !travelType.isBlank()) ? travelType.trim() : "LEISURE";
        String cacheKey = (city + "_" + type).toUpperCase();

        if (insightsCache.containsKey(cacheKey)) {
            TravelInsightResponse cached = insightsCache.get(cacheKey);
            return new TravelInsightResponse(
                    cached.destinationCity(),
                    cached.summary(),
                    cached.bestTimeToVisit(),
                    cached.topAttractions(),
                    cached.localTips(),
                    cached.weatherInsight(),
                    cached.aiModel(),
                    true,
                    cached.fallback()
            );
        }

        if (apiKey == null || apiKey.trim().isBlank()) {
            TravelInsightResponse fallback = buildDeterministicInsightFallback(city, type);
            insightsCache.put(cacheKey, fallback);
            return fallback;
        }

        try {
            String prompt = String.format(
                    "You are a travel assistant. Provide a JSON response with travel insights for destination '%s' for '%s' travelers. " +
                    "Return ONLY JSON with fields: summary (string), bestTimeToVisit (string), topAttractions (array of 3 strings), " +
                    "localTips (array of 2 strings), weatherInsight (string). Do not include markdown codeblocks.",
                    city, type
            );

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    ))
            ));

            URI uri = URI.create(String.format("%s/%s:generateContent?key=%s", GEMINI_API_BASE, modelName, apiKey.trim()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidateTextNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (!candidateTextNode.isMissingNode()) {
                    String rawText = candidateTextNode.asText().trim();
                    if (rawText.startsWith("```json")) {
                        rawText = rawText.substring(7);
                    }
                    if (rawText.startsWith("```")) {
                        rawText = rawText.substring(3);
                    }
                    if (rawText.endsWith("```")) {
                        rawText = rawText.substring(0, rawText.length() - 3);
                    }
                    rawText = rawText.trim();

                    JsonNode parsed = objectMapper.readTree(rawText);
                    List<String> attractions = new ArrayList<>();
                    parsed.path("topAttractions").forEach(n -> attractions.add(n.asText()));
                    List<String> tips = new ArrayList<>();
                    parsed.path("localTips").forEach(n -> tips.add(n.asText()));

                    TravelInsightResponse result = new TravelInsightResponse(
                            city,
                            parsed.path("summary").asText("A vibrant destination with rich cultural heritage."),
                            parsed.path("bestTimeToVisit").asText("October to March for pleasant temperatures."),
                            attractions.isEmpty() ? List.of("Historic City Center", "Cultural Museums", "Iconic Waterfront") : attractions,
                            tips.isEmpty() ? List.of("Book tickets online in advance", "Use metro transit during rush hours") : tips,
                            parsed.path("weatherInsight").asText("Pleasant seasonal climate suitable for sightseeing."),
                            modelName,
                            false,
                            false
                    );
                    insightsCache.put(cacheKey, result);
                    return result;
                }
            } else {
                log.warn("Gemini API returned status {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Gemini AI generation failed (using fallback): {}", e.getMessage());
        }

        TravelInsightResponse fallback = buildDeterministicInsightFallback(city, type);
        insightsCache.put(cacheKey, fallback);
        return fallback;
    }

    @Override
    public FlightDelayExplanationResponse generateDelayExplanation(String flightNumber, String origin, String destination, String standardReason) {
        String fNum = flightNumber != null ? flightNumber.toUpperCase().trim() : "FLIGHT";
        String orig = origin != null ? origin.toUpperCase().trim() : "ORIGIN";
        String dest = destination != null ? destination.toUpperCase().trim() : "DESTINATION";
        String reason = standardReason != null ? standardReason.trim() : "Operational constraint";
        String cacheKey = (fNum + "_" + reason).toUpperCase();

        if (delayCache.containsKey(cacheKey)) {
            return delayCache.get(cacheKey);
        }

        if (apiKey == null || apiKey.trim().isBlank()) {
            FlightDelayExplanationResponse fallback = buildDeterministicDelayFallback(fNum, orig, dest, reason);
            delayCache.put(cacheKey, fallback);
            return fallback;
        }

        try {
            String prompt = String.format(
                    "Explain a flight delay for flight %s from %s to %s due to '%s'. " +
                    "Return ONLY JSON with fields: primaryReason (string), detailedExplanation (string), " +
                    "passengerAdvice (array of 2 strings), estimatedImpact (string). No markdown.",
                    fNum, orig, dest, reason
            );

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "contents", List.of(Map.of(
                            "parts", List.of(Map.of("text", prompt))
                    ))
            ));

            URI uri = URI.create(String.format("%s/%s:generateContent?key=%s", GEMINI_API_BASE, modelName, apiKey.trim()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                JsonNode candidateTextNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (!candidateTextNode.isMissingNode()) {
                    String rawText = candidateTextNode.asText().trim();
                    if (rawText.startsWith("```json")) rawText = rawText.substring(7);
                    if (rawText.startsWith("```")) rawText = rawText.substring(3);
                    if (rawText.endsWith("```")) rawText = rawText.substring(0, rawText.length() - 3);

                    JsonNode parsed = objectMapper.readTree(rawText.trim());
                    List<String> advice = new ArrayList<>();
                    parsed.path("passengerAdvice").forEach(n -> advice.add(n.asText()));

                    FlightDelayExplanationResponse result = new FlightDelayExplanationResponse(
                            fNum,
                            orig,
                            dest,
                            parsed.path("primaryReason").asText(reason),
                            parsed.path("detailedExplanation").asText("Air traffic control flow management is in effect to ensure safe runway separation."),
                            advice.isEmpty() ? List.of("Remain in the departure lounge near your gate", "Check the SmartTravel live radar for aircraft repositioning") : advice,
                            parsed.path("estimatedImpact").asText("Expected turnaround within 30 to 45 minutes."),
                            false
                    );
                    delayCache.put(cacheKey, result);
                    return result;
                }
            }
        } catch (Exception e) {
            log.warn("Gemini delay explanation failed (using fallback): {}", e.getMessage());
        }

        FlightDelayExplanationResponse fallback = buildDeterministicDelayFallback(fNum, orig, dest, reason);
        delayCache.put(cacheKey, fallback);
        return fallback;
    }

    private TravelInsightResponse buildDeterministicInsightFallback(String city, String travelType) {
        String cleanCity = city.toUpperCase();
        return switch (cleanCity) {
            case "BOM", "MUMBAI" -> new TravelInsightResponse(
                    "Mumbai",
                    "The financial capital of India, renowned for colonial architecture, coastal promenades, and vibrant arts scene.",
                    "November to February (mild winter temperatures averaging 22°C to 28°C)",
                    List.of("Gateway of India & Colaba Causeway", "Marine Drive & Nariman Point", "Elephanta Caves UNESCO site"),
                    List.of("Use the Western/Central railway during non-peak hours (11am - 4pm)", "Try authentic local coastal seafood along Fort district"),
                    "Tropical coastal climate with pleasant winter breezes.",
                    "deterministic-fallback",
                    false,
                    true
            );
            case "DEL", "NEW DELHI", "DELHI" -> new TravelInsightResponse(
                    "New Delhi",
                    "India's historic capital offering a blend of ancient Mughal monuments, wide boulevards, and world-class dining.",
                    "October to March (cool, sunny days ideal for outdoor exploration)",
                    List.of("Qutub Minar Complex", "Humayun's Tomb", "India Gate & National War Memorial"),
                    List.of("Use Delhi Metro Airport Express for seamless airport transfers", "Pre-book tickets for ASI monuments using QR codes"),
                    "Crisp, clear winter conditions; pleasant mornings and evenings.",
                    "deterministic-fallback",
                    false,
                    true
            );
            case "BLR", "BENGALURU", "BANGALORE" -> new TravelInsightResponse(
                    "Bengaluru",
                    "The Garden City and Silicon Valley of India, famed for pleasant weather, craft breweries, and tech parks.",
                    "Year-round pleasant climate; September to March is ideal",
                    List.of("Lalbagh Botanical Garden", "Bangalore Palace", "Cubbon Park & Vidhana Soudha"),
                    List.of("Use Namma Metro to avoid peak-hour Outer Ring Road traffic", "Explore specialty coffee roasters across Indiranagar"),
                    "Moderate subtropical highland climate with cool evenings.",
                    "deterministic-fallback",
                    false,
                    true
            );
            case "DXB", "DUBAI" -> new TravelInsightResponse(
                    "Dubai",
                    "Global hub of luxury, futuristic skyscrapers, ultra-modern malls, and desert safari experiences.",
                    "November to April (comfortable temperatures between 20°C and 30°C)",
                    List.of("Burj Khalifa & Dubai Mall Fountain", "Museum of the Future", "Palm Jumeirah & Atlantis"),
                    List.of("Use the Dubai Metro Red Line for central skyscraper corridor transit", "Purchase the Dubai Pass for combined attraction discounts"),
                    "Sunny and warm; perfect for beach and desert excursions.",
                    "deterministic-fallback",
                    false,
                    true
            );
            default -> new TravelInsightResponse(
                    city,
                    "A premier travel destination offering rich culture, distinct culinary traditions, and top tourist attractions.",
                    "October to April for optimal sightseeing weather",
                    List.of("City Center & Historic Quarter", "Cultural Landmarks & Museums", "Panoramic City Viewpoint"),
                    List.of("Keep your digital boarding pass and booking QR ready", "Check local transit schedules in advance"),
                    "Comfortable seasonal travel conditions.",
                    "deterministic-fallback",
                    false,
                    true
            );
        };
    }

    private FlightDelayExplanationResponse buildDeterministicDelayFallback(String fNum, String orig, String dest, String reason) {
        return new FlightDelayExplanationResponse(
                fNum,
                orig,
                dest,
                reason,
                String.format("Flight %s departing from %s to %s is experiencing an operational delay (%s). Ground dispatch teams are coordinating expedited boarding once the inbound aircraft is cleared.", fNum, orig, dest, reason),
                List.of(
                        "Please remain within the departure gate area for updated boarding announcements.",
                        "Track live revised departure time directly on your SmartTravel digital boarding pass."
                ),
                "Expected departure delay of approximately 25–35 minutes.",
                true
        );
    }
}
