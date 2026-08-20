package com.smarttravel.modules.recommendation;

import com.smarttravel.modules.recommendation.model.UserActivity;
import com.smarttravel.modules.recommendation.model.UserActivityType;
import com.smarttravel.modules.recommendation.repository.UserActivityRepository;
import com.smarttravel.modules.recommendation.service.CollaborativeFilteringService;
import com.smarttravel.modules.recommendation.service.CollaborativeFilteringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollaborativeFilteringServiceTest {

    @Mock
    private UserActivityRepository activityRepository;

    private CollaborativeFilteringService collaborativeFilteringService;

    @BeforeEach
    void setUp() {
        collaborativeFilteringService = new CollaborativeFilteringServiceImpl(activityRepository);
    }

    @Test
    @DisplayName("Should compute positive co-occurrence cosine similarity between two correlated destinations")
    void computeItemSimilarity_CorrelatedDestinations() {
        // User 1 booked GOA and BALI
        UserActivity u1Goa = UserActivity.builder().userId("user1").activityType(UserActivityType.BOOK).targetId("GOI").build();
        UserActivity u1Bali = UserActivity.builder().userId("user1").activityType(UserActivityType.BOOK).targetId("DPS").build();

        // User 2 booked GOA and BALI
        UserActivity u2Goa = UserActivity.builder().userId("user2").activityType(UserActivityType.BOOK).targetId("GOI").build();
        UserActivity u2Bali = UserActivity.builder().userId("user2").activityType(UserActivityType.BOOK).targetId("DPS").build();

        // User 3 booked GOA and PHUKET
        UserActivity u3Goa = UserActivity.builder().userId("user3").activityType(UserActivityType.BOOK).targetId("GOI").build();
        UserActivity u3Phuket = UserActivity.builder().userId("user3").activityType(UserActivityType.BOOK).targetId("HKT").build();

        when(activityRepository.findAll()).thenReturn(List.of(u1Goa, u1Bali, u2Goa, u2Bali, u3Goa, u3Phuket));

        double simGoaBali = collaborativeFilteringService.computeItemSimilarity("GOI", "DPS");
        double simGoaPhuket = collaborativeFilteringService.computeItemSimilarity("GOI", "HKT");
        double simBaliPhuket = collaborativeFilteringService.computeItemSimilarity("DPS", "HKT");

        assertThat(simGoaBali).isGreaterThan(0.5);
        assertThat(simGoaPhuket).isGreaterThan(0.0);
        assertThat(simBaliPhuket).isEqualTo(0.0); // No user booked both Bali and Phuket
    }

    @Test
    @DisplayName("Should score candidate items based on collaborative affinity for target user")
    void computeCollaborativeScores_TargetUser() {
        // User A (target user) booked GOA
        UserActivity uAGoa = UserActivity.builder().userId("userA").activityType(UserActivityType.BOOK).targetId("GOI").build();

        // Other users booked both GOA and BALI
        UserActivity u1Goa = UserActivity.builder().userId("user1").activityType(UserActivityType.BOOK).targetId("GOI").build();
        UserActivity u1Bali = UserActivity.builder().userId("user1").activityType(UserActivityType.BOOK).targetId("DPS").build();

        UserActivity u2Goa = UserActivity.builder().userId("user2").activityType(UserActivityType.BOOK).targetId("GOI").build();
        UserActivity u2Bali = UserActivity.builder().userId("user2").activityType(UserActivityType.BOOK).targetId("DPS").build();

        when(activityRepository.findAll()).thenReturn(List.of(uAGoa, u1Goa, u1Bali, u2Goa, u2Bali));

        Map<String, Double> scores = collaborativeFilteringService.computeCollaborativeScores("userA", Set.of("DPS", "DEL"));

        assertThat(scores).containsKey("DPS");
        assertThat(scores.get("DPS")).isGreaterThan(50.0);
        assertThat(scores.get("DEL")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should return empty scores for anonymous or new user with zero prior activity (cold start)")
    void computeCollaborativeScores_NewUserColdStart() {
        Map<String, Double> scores = collaborativeFilteringService.computeCollaborativeScores("new-user-123", Set.of("DPS"));
        assertThat(scores).isEmpty();
    }
}
