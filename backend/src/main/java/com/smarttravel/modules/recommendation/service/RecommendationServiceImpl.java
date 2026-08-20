package com.smarttravel.modules.recommendation.service;

import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import com.smarttravel.modules.recommendation.dto.RecommendationItem;
import com.smarttravel.modules.recommendation.dto.RecommendationItemType;
import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;
import com.smarttravel.modules.recommendation.repository.UserActivityRepository;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid recommendation engine implementation.
 *
 * Scoring breakdown:
 *   - Content-based (40%): flight/hotel attributes matching user preferences
 *   - Activity-based (35%): weight sum of user's own activity on similar targets
 *   - Popularity (15%): recent booking/view count across all users
 *   - Preference match (10%): home airport, preferred cabin class alignment
 *
 * Falls back to popularity-based recommendations for anonymous users.
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private static final int ACTIVITY_LOOKBACK_DAYS = 30;

    private final UserActivityRepository activityRepository;
    private final FlightRepository flightRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final CollaborativeFilteringService collaborativeFilteringService;

    public RecommendationServiceImpl(UserActivityRepository activityRepository,
                                     FlightRepository flightRepository,
                                     HotelRepository hotelRepository,
                                     UserRepository userRepository,
                                     CollaborativeFilteringService collaborativeFilteringService) {
        this.activityRepository = activityRepository;
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
                .metadata(metadata)
                .build();

        activityRepository.save(activity);
        log.debug("Tracked {} activity for user {} on {} {}", type, userId, targetType, targetId);
    }

    @Override
    public List<RecommendationItem> getRecommendations(String userId, int limit) {
        int half = limit / 2;
        List<RecommendationItem> flights = getFlightRecommendations(userId, half + (limit % 2));
        List<RecommendationItem> hotels = getHotelRecommendations(userId, half);

        List<RecommendationItem> combined = new ArrayList<>();
        combined.addAll(flights);
        combined.addAll(hotels);

        // Interleave by score
        combined.sort(Comparator.comparingDouble(RecommendationItem::getScore).reversed());
        return combined.stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public List<RecommendationItem> getFlightRecommendations(String userId, int limit) {
        Instant since = Instant.now().minus(ACTIVITY_LOOKBACK_DAYS, ChronoUnit.DAYS);

        // Get user's preferred destinations from activity
        Set<String> preferredArrivalCodes = new HashSet<>();
        String homeAirport = null;

        if (userId != null) {
            List<UserActivity> recentActivity = activityRepository
                    .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);

            for (UserActivity a : recentActivity) {
                if (a.getMetadata() != null) {
                    Object arrival = a.getMetadata().get("arrivalAirport");
                    if (arrival != null) preferredArrivalCodes.add(arrival.toString());
                }
            }

            // Get user preferences
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent() && userOpt.get().getPreferences() != null) {
                homeAirport = userOpt.get().getPreferences().getHomeAirport();
            }
        }

        // Fetch bookable flights
        List<Flight> flights = flightRepository.findAll(PageRequest.of(0, 200)).getContent().stream()
                .filter(f -> f.isActive() &&
                        f.getStatus() == FlightStatus.SCHEDULED &&
                        f.getDepartureTime().isAfter(Instant.now()))
                .collect(Collectors.toList());

        // Extract candidate destination / flight targets for collaborative filtering
        Set<String> candidateTargets = flights.stream()
                .map(f -> f.getArrivalAirport() != null ? f.getArrivalAirport().getCode() : f.getId())
                .collect(Collectors.toSet());

        Map<String, Double> collabScores = userId != null
                ? collaborativeFilteringService.computeCollaborativeScores(userId, candidateTargets)
                : Collections.emptyMap();

        // Score each flight
        String finalHomeAirport = homeAirport;
        List<RecommendationItem> items = flights.stream()
                .map(f -> scoreAndBuildFlight(f, userId, preferredArrivalCodes, finalHomeAirport, collabScores, since))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(RecommendationItem::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        return items;
    }

    @Override
    public List<RecommendationItem> getHotelRecommendations(String userId, int limit) {
        Set<String> preferredCities = new HashSet<>();

        if (userId != null) {
            Instant since = Instant.now().minus(ACTIVITY_LOOKBACK_DAYS, ChronoUnit.DAYS);
            List<UserActivity> hotelActivity = activityRepository
                    .findByUserIdAndTargetTypeAndCreatedAtAfterOrderByCreatedAtDesc(userId, "HOTEL", since);

            for (UserActivity a : hotelActivity) {
                if (a.getMetadata() != null) {
                    Object city = a.getMetadata().get("city");
                    if (city != null) preferredCities.add(city.toString().toLowerCase());
                }
            }
        }

        List<Hotel> hotels = hotelRepository.findByActiveTrueOrderByAverageRatingDesc(PageRequest.of(0, 50))
                .getContent();

        Set<String> candidateHotelCities = hotels.stream()
                .filter(h -> h.getAddress() != null && h.getAddress().getCity() != null)
                .map(h -> h.getAddress().getCity().toLowerCase())
                .collect(Collectors.toSet());

        Map<String, Double> hotelCollabScores = userId != null
                ? collaborativeFilteringService.computeCollaborativeScores(userId, candidateHotelCities)
                : Collections.emptyMap();

        return hotels.stream()
                .map(h -> buildHotelRecommendation(h, preferredCities, hotelCollabScores))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(RecommendationItem::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecommendationItem> getPopularDestinations(int limit) {
        // Return top destination recommendations based on hotel cities
        List<Hotel> topHotels = hotelRepository
                .findByActiveTrueOrderByAverageRatingDesc(PageRequest.of(0, limit))
                .getContent();

        return topHotels.stream()
                .map(h -> RecommendationItem.builder()
                        .id("dest-" + h.getId())
                        .type(RecommendationItemType.DESTINATION)
                        .targetId(h.getId())
                        .title(h.getAddress() != null ? h.getAddress().getCity() : "India")
                        .subtitle("Popular destination")
                        .price(h.getBaseNightlyRate())
                        .priceLabel("From ₹" + formatPrice(h.getBaseNightlyRate()) + "/night")
                        .currency("INR")
                        .score(h.getAverageRating() * 20)
                        .reasonCode("POPULAR")
                        .reasonLabel("Trending destination")
                        .city(h.getAddress() != null ? h.getAddress().getCity() : null)
                        .avgRating(h.getAverageRating())
                        .build())
                .collect(Collectors.toList());
    }

    // ── Private Scoring Helpers ───────────────────────────────────────────────

    private RecommendationItem scoreAndBuildFlight(Flight flight, String userId,
                                                    Set<String> preferredArrivalCodes,
                                                    String homeAirport,
                                                    Map<String, Double> collabScores,
                                                    Instant since) {
        if (flight.getArrivalAirport() == null || flight.getDepartureAirport() == null) return null;

        double score = 0.0;
        String reasonCode = "POPULAR";
        String reasonLabel = "Popular flight";

        double contentScore = 0.0;
        double activityScore = 0.0;
        double collabScore = 0.0;
        double popularityScore = 0.0;
        double preferenceScore = 0.0;

        // Content-based: matching preferred destinations (30% weight)
        String arrivalCode = flight.getArrivalAirport().getCode();
        if (preferredArrivalCodes.contains(arrivalCode)) {
            contentScore = 100.0;
        }

        // Activity-based score (25% weight): has user viewed/searched for this flight or destination?
        if (userId != null) {
            boolean viewed = activityRepository.existsByUserIdAndTargetIdAndActivityType(
                    userId, flight.getId(), UserActivityType.VIEW);
            if (viewed) {
                activityScore = 100.0;
            }
        }

        // Collaborative filtering score (25% weight)
        if (collabScores != null && collabScores.containsKey(arrivalCode)) {
            collabScore = collabScores.get(arrivalCode);
        }

        // Popularity component (10% weight)
        int totalSeats = flight.getTotalSeats();
        int availableSeats = flight.getAvailableSeats();
        if (totalSeats > 0) {
            double occupancy = (double)(totalSeats - availableSeats) / totalSeats;
            popularityScore = occupancy * 100.0;
        }

        // Preference match (10% weight): home airport alignment
        if (homeAirport != null && homeAirport.equalsIgnoreCase(flight.getDepartureAirport().getCode())) {
            preferenceScore = 100.0;
        }

        // Multi-factor hybrid weighted score:
        score = (0.30 * contentScore)
                + (0.25 * activityScore)
                + (0.25 * collabScore)
                + (0.10 * popularityScore)
                + (0.10 * preferenceScore);

        // Determine truthful primary explanation
        if (collabScore >= 50.0 && collabScore >= contentScore && collabScore >= activityScore) {
            reasonCode = "COLLABORATIVE";
            reasonLabel = "Travelers with similar booking patterns also liked this";
        } else if (activityScore > 0 && activityScore >= contentScore) {
            reasonCode = "PREVIOUSLY_VIEWED";
            reasonLabel = "You recently viewed this";
        } else if (contentScore > 0) {
            reasonCode = "PAST_SEARCH";
            reasonLabel = "Based on your destination searches";
        } else if (score > 40.0) {
            reasonCode = "POPULAR";
            reasonLabel = "Trending flight route";
        } else {
            reasonCode = "RECOMMENDED";
            reasonLabel = "Recommended for you";
        }

        BigDecimal lowestPrice = flight.getCabinInventories().stream()
                .filter(ci -> ci.getTotalPrice() != null)
                .map(CabinInventory::getTotalPrice)
                .min(Comparator.naturalOrder())
                .orElse(flight.getBasePrice());

        return RecommendationItem.builder()
                .id("flight-" + flight.getId())
                .type(RecommendationItemType.FLIGHT)
                .targetId(flight.getId())
                .title(flight.getDepartureAirport().getCity() + " → " + flight.getArrivalAirport().getCity())
                .subtitle(flight.getFlightNumber() + " · " + flight.getAirline())
                .description("Scheduled departure from " + flight.getDepartureAirport().getCode())
                .price(lowestPrice)
                .priceLabel("From ₹" + formatPrice(lowestPrice))
                .currency("INR")
                .score(Math.min(100.0, Math.max(10.0, score)))
                .reasonCode(reasonCode)
                .reasonLabel(reasonLabel)
                .fromCity(flight.getDepartureAirport().getCity())
                .toCity(flight.getArrivalAirport().getCity())
                .fromCode(flight.getDepartureAirport().getCode())
                .toCode(flight.getArrivalAirport().getCode())
                .airline(flight.getAirline())
                .build();
    }

    private RecommendationItem buildHotelRecommendation(Hotel hotel,
                                                         Set<String> preferredCities,
                                                         Map<String, Double> hotelCollabScores) {
        if (hotel.getAddress() == null) return null;

        String city = hotel.getAddress().getCity();
        String cityLower = city != null ? city.toLowerCase() : "";

        double contentScore = 0.0;
        double activityScore = 0.0;
        double collabScore = 0.0;
        double popularityScore = (hotel.getAverageRating() / 5.0) * 100.0;
        double preferenceScore = hotel.getStarRating() * 20.0;

        if (city != null && preferredCities.contains(cityLower)) {
            contentScore = 100.0;
            activityScore = 80.0;
        }

        if (hotelCollabScores != null && hotelCollabScores.containsKey(cityLower)) {
            collabScore = hotelCollabScores.get(cityLower);
        }

        double score = (0.30 * contentScore)
                + (0.25 * activityScore)
                + (0.25 * collabScore)
                + (0.10 * popularityScore)
                + (0.10 * preferenceScore);

        String reasonCode = "HIGH_RATED";
        String reasonLabel = "Highly rated hotel";

        if (collabScore >= 50.0 && collabScore >= contentScore) {
            reasonCode = "COLLABORATIVE";
            reasonLabel = "Travelers with similar booking patterns also booked this";
        } else if (contentScore > 0) {
            reasonCode = "PREFERRED_CITY";
            reasonLabel = "Based on your destination searches";
        }

        return RecommendationItem.builder()
                .id("hotel-" + hotel.getId())
                .type(RecommendationItemType.HOTEL)
                .targetId(hotel.getId())
                .title(hotel.getName())
                .subtitle(city + " · " + hotel.getStarRating() + " Star")
                .description(hotel.getDescription())
                .price(hotel.getBaseNightlyRate())
                .priceLabel("From ₹" + formatPrice(hotel.getBaseNightlyRate()) + "/night")
                .currency("INR")
                .score(Math.min(100.0, Math.max(10.0, score)))
                .reasonCode(reasonCode)
                .reasonLabel(reasonLabel)
                .city(city)
                .starRating(hotel.getStarRating())
                .avgRating(hotel.getAverageRating())
                .build();
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "0";
        long val = price.longValue();
        if (val >= 1000) return String.format("%,d", val);
        return String.valueOf(val);
    }
}
