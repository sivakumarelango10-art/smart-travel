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
import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;
import com.smarttravel.modules.recommendation.repository.UserActivityRepository;
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
    private FlightRepository flightRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.smarttravel.modules.recommendation.service.CollaborativeFilteringService collaborativeFilteringService;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private Flight flightMumbai;
    private Flight flightGoa;
    private Hotel hotelMumbai;

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
    }

    @Test
    @DisplayName("getFlightRecommendations prioritizes destinations matching past search activity")
    void testGetFlightRecommendations_PrioritizesPastSearches() {
        // User previously searched for flights arriving in Mumbai (BOM)
        UserActivity activity = UserActivity.builder()
                .userId("user-1")
                .activityType(UserActivityType.SEARCH)
                .metadata(Map.of("arrivalAirport", "BOM"))
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
        // Mumbai flight should score higher due to destination search match (40 pts)
        assertThat(recs.get(0).getTargetId()).isEqualTo("fl-bom");
        assertThat(recs.get(0).getReasonCode()).isEqualTo("PAST_SEARCH");
    }

    @Test
    @DisplayName("getPopularDestinations returns top trending properties without requiring user login")
    void testGetPopularDestinations_Public() {
        when(hotelRepository.findByActiveTrueOrderByAverageRatingDesc(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(hotelMumbai)));

        List<RecommendationItem> dests = recommendationService.getPopularDestinations(5);

        assertThat(dests).hasSize(1);
        assertThat(dests.get(0).getTitle()).isEqualTo("Mumbai");
        assertThat(dests.get(0).getReasonCode()).isEqualTo("POPULAR");
    }
}
