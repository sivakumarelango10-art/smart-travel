package com.smarttravel.modules.recommendation.service;

import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import com.smarttravel.modules.recommendation.dto.RecommendationExplanation;
import com.smarttravel.modules.recommendation.dto.RecommendationItem;
import com.smarttravel.modules.recommendation.dto.RecommendationItemType;
import com.smarttravel.modules.recommendation.dto.UserPreferenceProfileDto;
import com.smarttravel.modules.recommendation.model.RecommendationFeedback;
import com.smarttravel.modules.recommendation.model.RecommendationFeedbackType;
import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;
import com.smarttravel.modules.recommendation.repository.RecommendationFeedbackRepository;
import com.smarttravel.modules.recommendation.repository.UserActivityRepository;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Production-grade Hybrid Recommendation Engine with Explainable AI & Feedback Loop.
 *
 * Implements:
 *   - Content-Based Filtering (destination, category, travel style match)
 *   - Collaborative Filtering (user-item cosine similarity)
 *   - User Activity Signals (searches, views, bookings, reviews)
 *   - Cold-Start / Popularity Fallback
 *   - Explainable "Why this recommendation?" Transparency Metadata
 *   - Feedback Loop (Helpful, Not Relevant, Dismissal adjustments)
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private static final int ACTIVITY_LOOKBACK_DAYS = 60;

    private final UserActivityRepository activityRepository;
    private final RecommendationFeedbackRepository feedbackRepository;
    private final FlightRepository flightRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final CollaborativeFilteringService collaborativeFilteringService;

    // Destination category mappings
    private static final Map<String, String> CITY_CATEGORY_MAP = new HashMap<>();
    private static final Map<String, List<String>> CITY_TAGS_MAP = new HashMap<>();

    static {
        // Beach & Tropical
        registerDestination("goa", "BEACH", List.of("Beaches", "Nightlife", "Water Sports", "Coastal"));
        registerDestination("bali", "BEACH", List.of("Tropical Beaches", "Surfing", "Villas", "Temples"));
        registerDestination("maldives", "BEACH", List.of("Overwater Villas", "Turquoise Waters", "Luxury", "Snorkeling"));
        registerDestination("phuket", "BEACH", List.of("Island Hopping", "Beaches", "Nightlife"));
        registerDestination("port blair", "BEACH", List.of("Scuba Diving", "Pristine Beaches", "Island"));
        registerDestination("pattaya", "BEACH", List.of("Coastal", "Water Sports", "Cabaret"));

        // Heritage & Culture
        registerDestination("jaipur", "HERITAGE", List.of("Royal Palaces", "Forts", "Rajasthani Culture", "Heritage"));
        registerDestination("udaipur", "HERITAGE", List.of("City of Lakes", "Palaces", "Romantic", "Architecture"));
        registerDestination("varanasi", "HERITAGE", List.of("Spiritual Ghats", "Ganga Aarti", "Ancient Temples"));
        registerDestination("agra", "HERITAGE", List.of("Taj Mahal", "Mughal History", "Monuments"));
        registerDestination("rome", "HERITAGE", List.of("Colosseum", "Vatican", "Ancient History"));
        registerDestination("kyoto", "HERITAGE", List.of("Shrines", "Zen Gardens", "Traditional Culture"));

        // Mountains & Scenic
        registerDestination("manali", "MOUNTAIN", List.of("Snow Peaks", "Solang Valley", "Trekking", "Adventure"));
        registerDestination("shimla", "MOUNTAIN", List.of("Mall Road", "Pine Forests", "Colonial Heritage"));
        registerDestination("leh", "MOUNTAIN", List.of("Pangong Lake", "High Passes", "Monasteries", "Biking"));
        registerDestination("srinagar", "MOUNTAIN", List.of("Dal Lake", "Houseboats", "Mughal Gardens"));
        registerDestination("munnar", "MOUNTAIN", List.of("Tea Plantations", "Misty Hills", "Waterfalls"));
        registerDestination("zurich", "MOUNTAIN", List.of("Swiss Alps", "Lakeside", "Scenic Trains"));

        // Luxury & Metropolitan
        registerDestination("dubai", "LUXURY", List.of("Burj Khalifa", "Luxury Shopping", "Desert Safari", "Skyline"));
        registerDestination("paris", "LUXURY", List.of("Eiffel Tower", "Haute Couture", "Fine Dining", "Romantic"));
        registerDestination("singapore", "LUXURY", List.of("Marina Bay", "Futuristic Gardens", "Clean City", "Shopping"));
        registerDestination("london", "LUXURY", List.of("Historic Landmarks", "West End", "Museums"));
        registerDestination("new york", "LUXURY", List.of("Broadway", "Times Square", "Metropolitan", "Arts"));
        registerDestination("tokyo", "LUXURY", List.of("High Tech", "Gastronomy", "Neon Skylines"));

        // Metropolitan Hubs
        registerDestination("mumbai", "METROPOLITAN", List.of("Bollywood", "Marine Drive", "Business Hub", "Street Food"));
        registerDestination("delhi", "METROPOLITAN", List.of("Historical Monuments", "Culinary Capital", "Shopping"));
        registerDestination("bengaluru", "METROPOLITAN", List.of("Tech Parks", "Craft Breweries", "Gardens"));
        registerDestination("hyderabad", "METROPOLITAN", List.of("Biryani", "Charminar", "IT Corridor"));
        registerDestination("chennai", "METROPOLITAN", List.of("Marina Beach", "Classical Arts", "Temples"));
        registerDestination("kolkata", "METROPOLITAN", List.of("Colonial Architecture", "Literature", "Sweets"));

        // Nature & Leisure
        registerDestination("kochi", "NATURE", List.of("Backwaters", "Fort Kochi", "Ayurveda", "Spice Markets"));
        registerDestination("shillong", "NATURE", List.of("Living Root Bridges", "Waterfalls", "Scotland of the East"));
        registerDestination("coorg", "NATURE", List.of("Coffee Plantations", "Lush Valleys", "Trekking"));
    }

    private static void registerDestination(String city, String category, List<String> tags) {
        CITY_CATEGORY_MAP.put(city.toLowerCase(), category);
        CITY_TAGS_MAP.put(city.toLowerCase(), tags);
    }

    public RecommendationServiceImpl(UserActivityRepository activityRepository,
                                     RecommendationFeedbackRepository feedbackRepository,
                                     FlightRepository flightRepository,
                                     HotelRepository hotelRepository,
                                     UserRepository userRepository,
                                     CollaborativeFilteringService collaborativeFilteringService) {
        this.activityRepository = activityRepository;
        this.feedbackRepository = feedbackRepository;
        this.flightRepository = flightRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.collaborativeFilteringService = collaborativeFilteringService;
    }

    @Override
    public void trackActivity(String userId, UserActivityType type, String targetId,
                               String targetType, Map<String, Object> metadata) {
        if (userId == null || targetId == null) return;

        UserActivity activity = UserActivity.builder()
                .userId(userId)
                .activityType(type)
                .targetId(targetId)
                .targetType(targetType)
                .metadata(metadata != null ? metadata : new HashMap<>())
                .build();

        activityRepository.save(activity);
        log.debug("Tracked {} activity for user {} on {} {}", type, userId, targetType, targetId);
    }

    @Override
    @Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_RECOMMENDATIONS, key = "(#userId != null ? #userId : 'anon') + '_' + #limit")
    public List<RecommendationItem> getRecommendations(String userId, int limit) {
        return getRecommendations(userId, null, null, limit);
    }

    @Override
    @Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_RECOMMENDATIONS,
            key = "(#userId != null ? #userId : 'anon') + '_' + (#context != null ? #context : 'none') + '_' + (#destination != null ? #destination : 'all') + '_' + #limit")
    public List<RecommendationItem> getRecommendations(String userId, String context, String destination, int limit) {
        int portion = Math.max(2, limit / 3);
        List<RecommendationItem> flights = getFlightRecommendations(userId, portion);
        List<RecommendationItem> hotels = getHotelRecommendations(userId, portion);
        List<RecommendationItem> destinations = getDestinationRecommendations(userId, portion);

        List<RecommendationItem> combined = new ArrayList<>();
        combined.addAll(flights);
        combined.addAll(hotels);
        combined.addAll(destinations);

        // Filter out dismissed / irrelevant items if user is logged in
        if (userId != null) {
            Set<String> excludedIds = getExcludedTargetIds(userId);
            combined = combined.stream()
                    .filter(item -> !excludedIds.contains(item.getTargetId()) && !excludedIds.contains(item.getId()))
                    .collect(Collectors.toList());
        }

        // Contextual boost (e.g. if user is browsing a specific destination or hotel)
        if (destination != null && !destination.isBlank()) {
            String destLower = destination.toLowerCase();
            combined.forEach(item -> {
                if (item.getCity() != null && item.getCity().toLowerCase().contains(destLower) ||
                    item.getTitle() != null && item.getTitle().toLowerCase().contains(destLower) ||
                    item.getToCity() != null && item.getToCity().toLowerCase().contains(destLower)) {
                    item.setScore(Math.min(100.0, item.getScore() + 25.0));
                    item.setBadgeText("Contextual Match");
                }
            });
        }

        // Sort by final score
        combined.sort(Comparator.comparingDouble(RecommendationItem::getScore).reversed());
        return combined.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_RECOMMENDATIONS, key = "'flight_' + (#userId != null ? #userId : 'anon') + '_' + #limit")
    public List<RecommendationItem> getFlightRecommendations(String userId, int limit) {
        Instant since = Instant.now().minus(ACTIVITY_LOOKBACK_DAYS, ChronoUnit.DAYS);

        Set<String> preferredArrivalCodes = new HashSet<>();
        Set<String> preferredCategories = new HashSet<>();
        String homeAirport = null;

        // User feedback and exclusions
        Set<String> excludedIds = userId != null ? getExcludedTargetIds(userId) : Collections.emptySet();
        Map<String, String> userFeedbackMap = userId != null ? getUserFeedbackMap(userId) : Collections.emptyMap();
        Map<String, Double> categoryFeedbackBoosts = userId != null ? getCategoryFeedbackBoosts(userId) : Collections.emptyMap();

        if (userId != null) {
            List<UserActivity> recentActivity = activityRepository
                    .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);

            for (UserActivity a : recentActivity) {
                if (a.getMetadata() != null) {
                    Object arrival = a.getMetadata().get("arrivalAirport");
                    if (arrival != null) preferredArrivalCodes.add(arrival.toString().toUpperCase());
                    Object city = a.getMetadata().get("city");
                    if (city != null) {
                        String cat = getCategoryForCity(city.toString());
                        if (cat != null) preferredCategories.add(cat);
                    }
                }
            }

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent() && userOpt.get().getPreferences() != null) {
                homeAirport = userOpt.get().getPreferences().getHomeAirport();
            }
        }

        int candidatePoolSize = Math.max(limit * 4, 30);
        var flightPage = flightRepository.findAll(PageRequest.of(0, candidatePoolSize));
        List<Flight> flights = (flightPage != null ? flightPage.getContent() : Collections.<Flight>emptyList()).stream()
                .filter(f -> f != null && f.isActive() &&
                        f.getStatus() == FlightStatus.SCHEDULED &&
                        f.getDepartureTime() != null &&
                        f.getDepartureTime().isAfter(Instant.now()) &&
                        !excludedIds.contains(f.getId()))
                .collect(Collectors.toList());

        Set<String> candidateTargets = flights.stream()
                .map(f -> f.getArrivalAirport() != null ? f.getArrivalAirport().getCode() : f.getId())
                .collect(Collectors.toSet());

        Set<String> viewedFlightIds = userId != null
                ? activityRepository.findByUserIdAndActivityTypeOrderByCreatedAtDesc(userId, UserActivityType.VIEW)
                        .stream()
                        .map(UserActivity::getTargetId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
                : Collections.emptySet();

        Map<String, Double> collabScores = userId != null
                ? collaborativeFilteringService.computeCollaborativeScores(userId, candidateTargets)
                : Collections.emptyMap();

        String finalHomeAirport = homeAirport;
        return flights.stream()
                .map(f -> scoreAndBuildFlight(f, viewedFlightIds, preferredArrivalCodes, preferredCategories,
                        finalHomeAirport, collabScores, userFeedbackMap, categoryFeedbackBoosts))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(RecommendationItem::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_RECOMMENDATIONS, key = "'hotel_' + (#userId != null ? #userId : 'anon') + '_' + #limit")
    public List<RecommendationItem> getHotelRecommendations(String userId, int limit) {
        Set<String> preferredCities = new HashSet<>();
        Set<String> preferredCategories = new HashSet<>();
        Set<String> excludedIds = userId != null ? getExcludedTargetIds(userId) : Collections.emptySet();
        Map<String, String> userFeedbackMap = userId != null ? getUserFeedbackMap(userId) : Collections.emptyMap();
        Map<String, Double> categoryFeedbackBoosts = userId != null ? getCategoryFeedbackBoosts(userId) : Collections.emptyMap();

        if (userId != null) {
            Instant since = Instant.now().minus(ACTIVITY_LOOKBACK_DAYS, ChronoUnit.DAYS);
            List<UserActivity> hotelActivity = activityRepository
                    .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);

            for (UserActivity a : hotelActivity) {
                if (a.getMetadata() != null) {
                    Object city = a.getMetadata().get("city");
                    if (city != null) {
                        String c = city.toString().toLowerCase();
                        preferredCities.add(c);
                        String cat = getCategoryForCity(c);
                        if (cat != null) preferredCategories.add(cat);
                    }
                }
            }
        }

        int candidatePoolSize = Math.max(limit * 4, 30);
        var hotelPage = hotelRepository.findByActiveTrueOrderByAverageRatingDesc(PageRequest.of(0, candidatePoolSize));
        if (hotelPage == null) {
            hotelPage = hotelRepository.findAll(PageRequest.of(0, candidatePoolSize));
        }
        List<Hotel> hotels = (hotelPage != null ? hotelPage.getContent() : Collections.<Hotel>emptyList()).stream()
                .filter(h -> h != null && !excludedIds.contains(h.getId()))
                .collect(Collectors.toList());

        Set<String> candidateHotelCities = hotels.stream()
                .filter(h -> h.getAddress() != null && h.getAddress().getCity() != null)
                .map(h -> h.getAddress().getCity().toLowerCase())
                .collect(Collectors.toSet());

        Map<String, Double> hotelCollabScores = userId != null
                ? collaborativeFilteringService.computeCollaborativeScores(userId, candidateHotelCities)
                : Collections.emptyMap();

        return hotels.stream()
                .map(h -> buildHotelRecommendation(h, preferredCities, preferredCategories, hotelCollabScores,
                        userFeedbackMap, categoryFeedbackBoosts))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(RecommendationItem::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_RECOMMENDATIONS, key = "'dest_pop_' + #limit")
    public List<RecommendationItem> getPopularDestinations(int limit) {
        return getDestinationRecommendations(null, limit);
    }

    @Override
    @Cacheable(value = com.smarttravel.common.config.CacheConfig.CACHE_RECOMMENDATIONS, key = "'dest_' + (#userId != null ? #userId : 'anon') + '_' + #limit")
    public List<RecommendationItem> getDestinationRecommendations(String userId, int limit) {
        Set<String> preferredCategories = new HashSet<>();
        Set<String> searchedCities = new HashSet<>();
        Set<String> excludedIds = userId != null ? getExcludedTargetIds(userId) : Collections.emptySet();
        Map<String, String> userFeedbackMap = userId != null ? getUserFeedbackMap(userId) : Collections.emptyMap();

        if (userId != null) {
            Instant since = Instant.now().minus(ACTIVITY_LOOKBACK_DAYS, ChronoUnit.DAYS);
            List<UserActivity> activities = activityRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);
            for (UserActivity a : activities) {
                if (a.getMetadata() != null) {
                    Object city = a.getMetadata().get("city");
                    if (city != null) {
                        String c = city.toString().toLowerCase();
                        searchedCities.add(c);
                        String cat = getCategoryForCity(c);
                        if (cat != null) preferredCategories.add(cat);
                    }
                }
            }
        }

        // List of candidate destination keys
        List<String> destinationKeys = List.of(
                "bali", "goa", "maldives", "dubai", "paris", "singapore",
                "jaipur", "manali", "kochi", "udaipur", "phuket", "tokyo"
        );

        List<RecommendationItem> items = new ArrayList<>();

        for (String destKey : destinationKeys) {
            if (excludedIds.contains("dest-" + destKey) || excludedIds.contains(destKey)) continue;

            String category = getCategoryForCity(destKey);
            List<String> tags = CITY_TAGS_MAP.getOrDefault(destKey, List.of("Scenic", "Top Rated"));
            String displayCity = capitalize(destKey);

            double score = 65.0; // Base score
            String reasonCode = "TRENDING_DESTINATION";
            String headline = "Trending destination for travelers";
            String details = "Popular among travel enthusiasts for picturesque views, authentic culture, and world-class hospitality.";

            if (preferredCategories.contains(category)) {
                score += 25.0;
                reasonCode = "CATEGORY_AFFINITY";
                if ("BEACH".equals(category)) {
                    headline = "You liked beach destinations! Try " + displayCity + ".";
                    details = "Based on your interest in coastal retreats and sun-drenched beaches, " + displayCity + " is your next dream getaway.";
                } else if ("LUXURY".equals(category)) {
                    headline = "Curated luxury escape in " + displayCity;
                    details = "Matches your preference for 5-star comfort, metropolitan skyline views, and premium travel.";
                } else if ("MOUNTAIN".equals(category)) {
                    headline = "Scenic mountain retreat in " + displayCity;
                    details = "Because you explored mountain getaways, enjoy serene valleys and alpine freshness.";
                } else if ("HERITAGE".equals(category)) {
                    headline = "Immerse in royal heritage in " + displayCity;
                    details = "Explore historic palaces, architecture, and cultural richness matching your travel style.";
                }
            } else if (searchedCities.contains(destKey)) {
                score += 20.0;
                reasonCode = "RECENT_SEARCH";
                headline = "Continue planning your trip to " + displayCity;
                details = "You recently showed interest in " + displayCity + ". Compare scheduled flights and luxury hotels now.";
            }

            RecommendationExplanation explanation = RecommendationExplanation.builder()
                    .reasonCode(reasonCode)
                    .headline(headline)
                    .details(details)
                    .category(category)
                    .confidence(Math.min(0.98, score / 100.0))
                    .tags(tags)
                    .isAiGenerated(false)
                    .build();

            RecommendationItem item = RecommendationItem.builder()
                    .id("dest-" + destKey)
                    .type(RecommendationItemType.DESTINATION)
                    .targetId(destKey)
                    .title(displayCity)
                    .subtitle(category + " · " + String.join(" • ", tags.subList(0, Math.min(2, tags.size()))))
                    .description(details)
                    .price(BigDecimal.valueOf(14999))
                    .priceLabel("Packages from ₹14,999")
                    .currency("INR")
                    .score(Math.min(100.0, score))
                    .reasonCode(reasonCode)
                    .reasonLabel(headline)
                    .explanation(explanation)
                    .category(category)
                    .tags(tags)
                    .badgeText(score >= 85.0 ? "Top Recommendation" : "Trending Destination")
                    .userFeedback(userFeedbackMap.get("dest-" + destKey))
                    .city(displayCity)
                    .avgRating(4.8)
                    .build();

            items.add(item);
        }

        items.sort(Comparator.comparingDouble(RecommendationItem::getScore).reversed());
        return items.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = com.smarttravel.common.config.CacheConfig.CACHE_RECOMMENDATIONS, allEntries = true)
    public RecommendationFeedback recordFeedback(String userId, String targetId, String targetType,
                                                  RecommendationFeedbackType feedbackType, String reasonCode, String category) {
        if (userId == null || targetId == null || feedbackType == null) {
            throw new IllegalArgumentException("userId, targetId, and feedbackType are required");
        }

        // Delete existing feedback if any for idempotency
        feedbackRepository.deleteByUserIdAndTargetId(userId, targetId);

        RecommendationFeedback feedback = RecommendationFeedback.builder()
                .userId(userId)
                .targetId(targetId)
                .targetType(targetType != null ? targetType : "ITEM")
                .feedbackType(feedbackType)
                .reasonCode(reasonCode)
                .category(category)
                .createdAt(Instant.now())
                .build();

        RecommendationFeedback saved = feedbackRepository.save(feedback);

        // Also track as activity signal
        UserActivityType actType = switch (feedbackType) {
            case HELPFUL -> UserActivityType.RECOMMENDATION_HELPFUL;
            case NOT_RELEVANT -> UserActivityType.RECOMMENDATION_IRRELEVANT;
            case DISMISS -> UserActivityType.RECOMMENDATION_DISMISS;
        };

        Map<String, Object> meta = new HashMap<>();
        meta.put("feedbackType", feedbackType.name());
        if (category != null) meta.put("category", category);
        trackActivity(userId, actType, targetId, targetType, meta);

        log.info("Recorded feedback {} for user {} on target {} (category: {})", feedbackType, userId, targetId, category);
        return saved;
    }

    @Override
    public UserPreferenceProfileDto getUserPreferenceProfile(String userId) {
        if (userId == null) {
            return UserPreferenceProfileDto.builder()
                    .inferredTravelStyle("EXPLORER & TRENDING")
                    .topCategories(List.of("BEACH", "LUXURY", "HERITAGE"))
                    .confidenceScore(0.5)
                    .build();
        }

        List<UserActivity> activities = activityRepository.findByUserIdOrderByCreatedAtDesc(userId);
        long helpfulCount = feedbackRepository.countByUserIdAndFeedbackType(userId, RecommendationFeedbackType.HELPFUL);

        Map<String, Double> categoryWeights = new HashMap<>();
        Set<String> preferredAirlines = new LinkedHashSet<>();
        Set<String> preferredDestinations = new LinkedHashSet<>();

        for (UserActivity act : activities) {
            double weight = act.getActivityType() != null ? act.getActivityType().getWeight() : 1.0;
            if (act.getMetadata() != null) {
                Object city = act.getMetadata().get("city");
                if (city != null) {
                    String c = city.toString();
                    preferredDestinations.add(capitalize(c));
                    String cat = getCategoryForCity(c);
                    if (cat != null) {
                        categoryWeights.merge(cat, weight, Double::sum);
                    }
                }
                Object airline = act.getMetadata().get("airline");
                if (airline != null) preferredAirlines.add(airline.toString());
            }
        }

        // Sort top categories by weight
        List<String> topCategories = categoryWeights.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(3)
                .collect(Collectors.toList());

        if (topCategories.isEmpty()) {
            topCategories = List.of("BEACH", "LUXURY");
        }

        String inferredStyle = String.join(" & ", topCategories) + " TRAVELER";

        Optional<User> userOpt = userRepository.findById(userId);
        String homeAirport = userOpt.flatMap(u -> Optional.ofNullable(u.getPreferences())).map(p -> p.getHomeAirport()).orElse("DEL");

        return UserPreferenceProfileDto.builder()
                .userId(userId)
                .topCategories(topCategories)
                .categoryAffinities(categoryWeights)
                .preferredDestinations(new ArrayList<>(preferredDestinations).subList(0, Math.min(5, preferredDestinations.size())))
                .preferredAirlines(new ArrayList<>(preferredAirlines).subList(0, Math.min(3, preferredAirlines.size())))
                .homeAirport(homeAirport)
                .inferredTravelStyle(inferredStyle)
                .totalActivities(activities.size())
                .helpfulFeedbackCount(helpfulCount)
                .confidenceScore(Math.min(0.98, Math.max(0.40, activities.size() * 0.08)))
                .build();
    }

    @Override
    public List<UserActivity> getUserActivityHistory(String userId, int limit) {
        if (userId == null) return Collections.emptyList();
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ── Internal Scoring & Explainability Helpers ──────────────────────────────

    private RecommendationItem scoreAndBuildFlight(Flight flight,
                                                    Set<String> viewedFlightIds,
                                                    Set<String> preferredArrivalCodes,
                                                    Set<String> preferredCategories,
                                                    String homeAirport,
                                                    Map<String, Double> collabScores,
                                                    Map<String, String> userFeedbackMap,
                                                    Map<String, Double> categoryFeedbackBoosts) {
        if (flight.getArrivalAirport() == null || flight.getDepartureAirport() == null) return null;

        String arrivalCode = flight.getArrivalAirport().getCode().toUpperCase();
        String arrivalCity = flight.getArrivalAirport().getCity();
        String category = getCategoryForCity(arrivalCity);
        List<String> tags = CITY_TAGS_MAP.getOrDefault(arrivalCity.toLowerCase(), List.of("Non-stop", "Comfort"));

        double contentScore = preferredArrivalCodes.contains(arrivalCode) ? 100.0 : 0.0;
        double activityScore = viewedFlightIds.contains(flight.getId()) ? 100.0 : 0.0;
        double collabScore = (collabScores != null && collabScores.containsKey(arrivalCode))
                ? collabScores.get(arrivalCode) : 0.0;

        int totalSeats = flight.getTotalSeats();
        int availableSeats = flight.getAvailableSeats();
        double popularityScore = totalSeats > 0 ? ((double)(totalSeats - availableSeats) / totalSeats) * 100.0 : 50.0;

        double preferenceScore = 0.0;
        if (homeAirport != null && homeAirport.equalsIgnoreCase(flight.getDepartureAirport().getCode())) {
            preferenceScore += 60.0;
        }
        if (preferredCategories.contains(category)) {
            preferenceScore += 40.0;
        }

        double categoryBoost = categoryFeedbackBoosts.getOrDefault(category, 0.0);

        double score = (0.28 * contentScore)
                + (0.22 * activityScore)
                + (0.20 * collabScore)
                + (0.15 * preferenceScore)
                + (0.15 * popularityScore)
                + categoryBoost;

        String reasonCode;
        String headline;
        String details;

        if (collabScore >= 50.0 && collabScore >= contentScore && collabScore >= activityScore) {
            reasonCode = "COLLABORATIVE";
            headline = "Travelers like you also booked this route";
            details = "Travelers with similar booking preferences and travel dates frequently chose " + flight.getAirline() + " to " + arrivalCity + ".";
        } else if (activityScore > 0 && activityScore >= contentScore) {
            reasonCode = "RECENT_ACTIVITY";
            headline = "You recently viewed this flight";
            details = "Pick up where you left off for your flight from " + flight.getDepartureAirport().getCity() + " to " + arrivalCity + ".";
        } else if (contentScore > 0 || preferredCategories.contains(category)) {
            reasonCode = "DESTINATION_PREFERENCE";
            if ("BEACH".equals(category)) {
                headline = "You liked beach getaways! Fly to " + arrivalCity;
                details = "Direct scheduled flights to your favorite coastal paradise on " + flight.getAirline() + ".";
            } else {
                headline = "Based on your destination interests";
                details = "Convenient schedule matching your searches for " + arrivalCity + ".";
            }
        } else if (preferenceScore >= 60.0) {
            reasonCode = "HOME_AIRPORT_MATCH";
            headline = "Direct departure from your home airport (" + homeAirport + ")";
            details = "Seamless non-stop connection from your preferred departure hub.";
        } else {
            reasonCode = "POPULAR_ROUTE";
            headline = "Top scheduled flight to " + arrivalCity;
            details = "Consistently high on-time reliability and competitive fares.";
        }

        BigDecimal lowestPrice = flight.getCabinInventories().stream()
                .filter(ci -> ci.getTotalPrice() != null)
                .map(CabinInventory::getTotalPrice)
                .min(Comparator.naturalOrder())
                .orElse(flight.getBasePrice());

        RecommendationExplanation explanation = RecommendationExplanation.builder()
                .reasonCode(reasonCode)
                .headline(headline)
                .details(details)
                .category(category)
                .confidence(Math.min(0.98, Math.max(0.60, score / 100.0)))
                .tags(tags)
                .isAiGenerated(false)
                .build();

        return RecommendationItem.builder()
                .id("flight-" + flight.getId())
                .type(RecommendationItemType.FLIGHT)
                .targetId(flight.getId())
                .title(flight.getDepartureAirport().getCity() + " → " + flight.getArrivalAirport().getCity())
                .subtitle(flight.getFlightNumber() + " · " + flight.getAirline())
                .description(details)
                .price(lowestPrice)
                .priceLabel("From ₹" + formatPrice(lowestPrice))
                .currency("INR")
                .score(Math.min(100.0, Math.max(10.0, score)))
                .reasonCode(reasonCode)
                .reasonLabel(headline)
                .explanation(explanation)
                .category(category)
                .tags(tags)
                .badgeText(score >= 80.0 ? "Top Pick for You" : "Trending Flight")
                .userFeedback(userFeedbackMap.get(flight.getId()))
                .fromCity(flight.getDepartureAirport().getCity())
                .toCity(flight.getArrivalAirport().getCity())
                .fromCode(flight.getDepartureAirport().getCode())
                .toCode(flight.getArrivalAirport().getCode())
                .airline(flight.getAirline())
                .build();
    }

    private RecommendationItem buildHotelRecommendation(Hotel hotel,
                                                         Set<String> preferredCities,
                                                         Set<String> preferredCategories,
                                                         Map<String, Double> hotelCollabScores,
                                                         Map<String, String> userFeedbackMap,
                                                         Map<String, Double> categoryFeedbackBoosts) {
        if (hotel.getAddress() == null) return null;

        String city = hotel.getAddress().getCity();
        String cityLower = city != null ? city.toLowerCase() : "";
        String category = getCategoryForCity(cityLower);
        List<String> tags = CITY_TAGS_MAP.getOrDefault(cityLower, List.of("Luxury", "Pool", "Spa"));

        double contentScore = (city != null && preferredCities.contains(cityLower)) ? 100.0 : 0.0;
        double activityScore = (city != null && preferredCities.contains(cityLower)) ? 80.0 : 0.0;
        double collabScore = (hotelCollabScores != null && hotelCollabScores.containsKey(cityLower))
                ? hotelCollabScores.get(cityLower) : 0.0;
        double popularityScore = (hotel.getAverageRating() / 5.0) * 100.0;
        double preferenceScore = preferredCategories.contains(category) ? 100.0 : (hotel.getStarRating() * 20.0);

        double categoryBoost = categoryFeedbackBoosts.getOrDefault(category, 0.0);

        double score = (0.28 * contentScore)
                + (0.22 * activityScore)
                + (0.20 * collabScore)
                + (0.15 * preferenceScore)
                + (0.15 * popularityScore)
                + categoryBoost;

        String reasonCode;
        String headline;
        String details;

        if (collabScore >= 50.0 && collabScore >= contentScore) {
            reasonCode = "COLLABORATIVE";
            headline = "Travelers with similar tastes loved this stay";
            details = "Guests who booked similar properties rated " + hotel.getName() + " highly for service and luxury.";
        } else if (contentScore > 0 || preferredCategories.contains(category)) {
            reasonCode = "CATEGORY_AFFINITY";
            if ("BEACH".equals(category)) {
                headline = "You liked beach stays! Discover " + hotel.getName();
                details = "Coastal serenity and beachfront luxury matching your verified stay history.";
            } else if ("LUXURY".equals(category)) {
                headline = "Premium 5-Star Stay in " + city;
                details = "Matches your preference for bespoke service and top-rated luxury hospitality.";
            } else {
                headline = "Recommended based on your search for " + city;
                details = "Top guest-rated stay with great location and verified positive reviews.";
            }
        } else if (hotel.getAverageRating() >= 4.7) {
            reasonCode = "HIGHLY_RATED";
            headline = "Exceptional " + hotel.getAverageRating() + "★ Guest Favorite";
            details = "Consistently recognized as one of the finest properties in " + city + ".";
        } else {
            reasonCode = "POPULAR_HOTEL";
            headline = "Popular hotel in " + city;
            details = "High guest satisfaction with transparent pricing and verified reviews.";
        }

        RecommendationExplanation explanation = RecommendationExplanation.builder()
                .reasonCode(reasonCode)
                .headline(headline)
                .details(details)
                .category(category)
                .confidence(Math.min(0.98, Math.max(0.60, score / 100.0)))
                .tags(tags)
                .isAiGenerated(false)
                .build();

        return RecommendationItem.builder()
                .id("hotel-" + hotel.getId())
                .type(RecommendationItemType.HOTEL)
                .targetId(hotel.getId())
                .title(hotel.getName())
                .subtitle(city + " · " + hotel.getStarRating() + " Star Luxury")
                .description(hotel.getDescription())
                .price(hotel.getBaseNightlyRate())
                .priceLabel("From ₹" + formatPrice(hotel.getBaseNightlyRate()) + "/night")
                .currency("INR")
                .score(Math.min(100.0, Math.max(10.0, score)))
                .reasonCode(reasonCode)
                .reasonLabel(headline)
                .explanation(explanation)
                .category(category)
                .tags(tags)
                .badgeText(score >= 80.0 ? "Guest Favorite" : "Recommended Stay")
                .userFeedback(userFeedbackMap.get(hotel.getId()))
                .city(city)
                .starRating(hotel.getStarRating())
                .avgRating(hotel.getAverageRating())
                .build();
    }

    private Set<String> getExcludedTargetIds(String userId) {
        if (userId == null) return Collections.emptySet();
        List<RecommendationFeedback> negativeFeedback = feedbackRepository.findByUserId(userId).stream()
                .filter(f -> f.getFeedbackType() == RecommendationFeedbackType.NOT_RELEVANT ||
                             f.getFeedbackType() == RecommendationFeedbackType.DISMISS)
                .toList();

        return negativeFeedback.stream()
                .map(RecommendationFeedback::getTargetId)
                .collect(Collectors.toSet());
    }

    private Map<String, String> getUserFeedbackMap(String userId) {
        if (userId == null) return Collections.emptyMap();
        List<RecommendationFeedback> allFeedback = feedbackRepository.findByUserId(userId);
        Map<String, String> map = new HashMap<>();
        for (RecommendationFeedback f : allFeedback) {
            map.put(f.getTargetId(), f.getFeedbackType().name());
        }
        return map;
    }

    private Map<String, Double> getCategoryFeedbackBoosts(String userId) {
        if (userId == null) return Collections.emptyMap();
        List<RecommendationFeedback> allFeedback = feedbackRepository.findByUserId(userId);
        Map<String, Double> boosts = new HashMap<>();

        for (RecommendationFeedback f : allFeedback) {
            if (f.getCategory() == null) continue;
            if (f.getFeedbackType() == RecommendationFeedbackType.HELPFUL) {
                boosts.merge(f.getCategory(), 20.0, Double::sum);
            } else if (f.getFeedbackType() == RecommendationFeedbackType.NOT_RELEVANT) {
                boosts.merge(f.getCategory(), -25.0, Double::sum);
            }
        }
        return boosts;
    }

    private String getCategoryForCity(String city) {
        if (city == null) return "POPULAR";
        return CITY_CATEGORY_MAP.getOrDefault(city.toLowerCase().trim(), "LEISURE");
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0";
        long val = price.longValue();
        if (val >= 1000) return String.format("%,d", val);
        return String.valueOf(val);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
