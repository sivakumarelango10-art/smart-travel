package com.smarttravel.modules.recommendation;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;
import com.smarttravel.modules.flight.model.CabinInventory;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.model.FlightStatus;
import com.smarttravel.modules.flight.repository.FlightRepository;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.HotelAddress;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import com.smarttravel.modules.recommendation.dto.RecommendationItem;
import com.smarttravel.modules.recommendation.dto.UserPreferenceProfileDto;
import com.smarttravel.modules.recommendation.model.RecommendationFeedback;
import com.smarttravel.modules.recommendation.model.RecommendationFeedbackType;
import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;
import com.smarttravel.modules.recommendation.repository.RecommendationFeedbackRepository;
import com.smarttravel.modules.recommendation.repository.UserActivityRepository;
import com.smarttravel.modules.recommendation.service.CollaborativeFilteringService;
import com.smarttravel.modules.recommendation.service.RecommendationServiceImpl;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.model.UserPreferences;
import com.smarttravel.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UserActivityRepository activityRepository;

    @Mock
    private RecommendationFeedbackRepository feedbackRepository;

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollaborativeFilteringService collaborativeFilteringService;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private Flight flightMumbai;
    private Flight flightGoa;
    private Hotel hotelMumbai;
    private Hotel hotelGoa;

    @BeforeEach
    void setUp() {
        flightMumbai = Flight.builder()
                .id("fl-bom")
                .flightNumber("AI-101")
                .airline("Air India")
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").city("Mumbai").build())
                .departureTime(Instant.now().plusSeconds(86400))
                .arrivalTime(Instant.now().plusSeconds(86400 + 7200))
                .status(FlightStatus.SCHEDULED)
                .basePrice(new BigDecimal("4500.00"))
                .cabinInventories(List.of(
                        CabinInventory.builder().cabinClass(CabinClass.ECONOMY).totalPrice(new BigDecimal("4500.00")).build()
                ))
                .totalSeats(100)
                .availableSeats(50)
                .active(true)
                .build();

        flightGoa = Flight.builder()
                .id("fl-goi")
                .flightNumber("6E-202")
                .airline("IndiGo")
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("GOI").city("Goa").build())
                .departureTime(Instant.now().plusSeconds(86400))
                .arrivalTime(Instant.now().plusSeconds(86400 + 9000))
                .status(FlightStatus.SCHEDULED)
                .basePrice(new BigDecimal("3800.00"))
                .cabinInventories(List.of(
                        CabinInventory.builder().cabinClass(CabinClass.ECONOMY).totalPrice(new BigDecimal("3800.00")).build()
                ))
                .totalSeats(100)
                .availableSeats(80)
                .active(true)
                .build();

        hotelMumbai = Hotel.builder()
                .id("ht-bom")
                .name("The Oberoi Mumbai")
                .address(HotelAddress.builder().city("Mumbai").state("Maharashtra").build())
                .starRating(5)
                .averageRating(4.8)
                .baseNightlyRate(new BigDecimal("18000.00"))
                .active(true)
                .build();

        hotelGoa = Hotel.builder()
                .id("ht-goi")
                .name("Taj Exotica Resort & Spa Goa")
                .address(HotelAddress.builder().city("Goa").state("Goa").build())
                .starRating(5)
                .averageRating(4.9)
                .baseNightlyRate(new BigDecimal("22000.00"))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("getFlightRecommendations prioritizes destinations matching past search activity with explainable reasoning")
    void testGetFlightRecommendations_PrioritizesPastSearches() {
        UserActivity activity = UserActivity.builder()
                .userId("user-1")
                .activityType(UserActivityType.SEARCH)
                .metadata(Map.of("arrivalAirport", "BOM", "city", "Mumbai"))
                .build();

        when(activityRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(eq("user-1"), any(Instant.class)))
                .thenReturn(List.of(activity));

        UserPreferences prefs = new UserPreferences();
        prefs.setHomeAirport("DEL");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(
                User.builder().id("user-1").preferences(prefs).build()
        ));

        when(flightRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(flightGoa, flightMumbai)));

        List<RecommendationItem> recs = recommendationService.getFlightRecommendations("user-1", 5);

        assertThat(recs).isNotEmpty();
        assertThat(recs.get(0).getTargetId()).isEqualTo("fl-bom");
        assertThat(recs.get(0).getExplanation()).isNotNull();
        assertThat(recs.get(0).getExplanation().getHeadline()).contains("Based on your destination");
    }

    @Test
    @DisplayName("Negative feedback (NOT_RELEVANT or DISMISS) excludes the item from future recommendations")
    void testFeedbackExcludesDismissedItem() {
        RecommendationFeedback feedback = RecommendationFeedback.builder()
                .userId("user-1")
                .targetId("fl-bom")
                .feedbackType(RecommendationFeedbackType.NOT_RELEVANT)
                .build();

        when(feedbackRepository.findByUserId("user-1")).thenReturn(List.of(feedback));
        when(flightRepository.findAll(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(flightGoa, flightMumbai)));

        List<RecommendationItem> recs = recommendationService.getFlightRecommendations("user-1", 5);

        assertThat(recs).extracting(RecommendationItem::getTargetId).doesNotContain("fl-bom");
    }

    @Test
    @DisplayName("recordFeedback persists user feedback and triggers activity tracking")
    void testRecordFeedback() {
        when(feedbackRepository.save(any(RecommendationFeedback.class))).thenAnswer(inv -> inv.getArgument(0));

        RecommendationFeedback result = recommendationService.recordFeedback(
                "user-1", "dest-bali", "DESTINATION",
                RecommendationFeedbackType.HELPFUL, "CATEGORY_AFFINITY", "BEACH"
        );

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.getTargetId()).isEqualTo("dest-bali");
        assertThat(result.getFeedbackType()).isEqualTo(RecommendationFeedbackType.HELPFUL);
        assertThat(result.getCategory()).isEqualTo("BEACH");

        verify(activityRepository, times(1)).save(any(UserActivity.class));
    }

    @Test
    @DisplayName("getUserPreferenceProfile synthesizes top categories and travel style from history")
    void testGetUserPreferenceProfile() {
        UserActivity act1 = UserActivity.builder()
                .userId("user-1")
                .activityType(UserActivityType.BOOK)
                .metadata(Map.of("city", "Goa", "airline", "Air India"))
                .build();

        UserActivity act2 = UserActivity.builder()
                .userId("user-1")
                .activityType(UserActivityType.VIEW)
                .metadata(Map.of("city", "Bali"))
                .build();

        when(activityRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(act1, act2));
        when(feedbackRepository.countByUserIdAndFeedbackType("user-1", RecommendationFeedbackType.HELPFUL)).thenReturn(3L);

        UserPreferences prefs = new UserPreferences();
        prefs.setHomeAirport("BOM");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(User.builder().id("user-1").preferences(prefs).build()));

        UserPreferenceProfileDto profile = recommendationService.getUserPreferenceProfile("user-1");

        assertThat(profile).isNotNull();
        assertThat(profile.getTopCategories()).contains("BEACH");
        assertThat(profile.getInferredTravelStyle()).contains("BEACH");
        assertThat(profile.getHelpfulFeedbackCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getDestinationRecommendations returns personalized destinations with 'You liked beaches! Try Bali' reasoning")
    void testGetDestinationRecommendations_BeachCategory() {
        UserActivity activity = UserActivity.builder()
                .userId("user-1")
                .activityType(UserActivityType.BOOK)
                .metadata(Map.of("city", "Goa"))
                .build();

        when(activityRepository.findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(eq("user-1"), any(Instant.class)))
                .thenReturn(List.of(activity));

        List<RecommendationItem> dests = recommendationService.getDestinationRecommendations("user-1", 5);

        assertThat(dests).isNotEmpty();
        RecommendationItem bali = dests.stream().filter(d -> "Bali".equalsIgnoreCase(d.getTitle())).findFirst().orElse(null);
        assertThat(bali).isNotNull();
        assertThat(bali.getExplanation()).isNotNull();
        assertThat(bali.getExplanation().getHeadline()).contains("You liked beach destinations! Try Bali.");
    }
}
