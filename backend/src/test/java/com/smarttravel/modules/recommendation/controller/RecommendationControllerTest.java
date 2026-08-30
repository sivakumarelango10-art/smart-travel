package com.smarttravel.modules.recommendation.controller;

import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.recommendation.dto.RecommendationExplanation;
import com.smarttravel.modules.recommendation.dto.RecommendationItem;
import com.smarttravel.modules.recommendation.dto.RecommendationItemType;
import com.smarttravel.modules.recommendation.dto.UserPreferenceProfileDto;
import com.smarttravel.modules.recommendation.model.RecommendationFeedback;
import com.smarttravel.modules.recommendation.model.RecommendationFeedbackType;
import com.smarttravel.modules.recommendation.service.RecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecommendationController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/v1/recommendations returns 200 OK with explainable recommendation items")
    void testGetRecommendations() throws Exception {
        RecommendationExplanation explanation = RecommendationExplanation.builder()
                .reasonCode("BEACH_PREFERENCE")
                .headline("You liked beach destinations! Try Bali.")
                .details("Based on your interest in Goa & Maldives.")
                .category("BEACH")
                .confidence(0.95)
                .tags(List.of("Beaches", "Tropical"))
                .build();

        RecommendationItem item = RecommendationItem.builder()
                .id("dest-bali")
                .type(RecommendationItemType.DESTINATION)
                .targetId("bali")
                .title("Bali")
                .subtitle("Tropical Paradise")
                .price(BigDecimal.valueOf(24999))
                .score(92.0)
                .explanation(explanation)
                .build();

        when(recommendationService.getRecommendations(any(), any(), any(), anyInt()))
                .thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value("dest-bali"))
                .andExpect(jsonPath("$.data[0].explanation.headline").value("You liked beach destinations! Try Bali."))
                .andExpect(jsonPath("$.data[0].explanation.category").value("BEACH"));
    }

    @Test
    @DisplayName("POST /api/v1/recommendations/feedback records user feedback")
    @WithMockUser(username = "traveler-1")
    void testSubmitFeedback() throws Exception {
        RecommendationFeedback feedback = RecommendationFeedback.builder()
                .id("fb-1")
                .userId("traveler-1")
                .targetId("dest-bali")
                .targetType("DESTINATION")
                .feedbackType(RecommendationFeedbackType.HELPFUL)
                .category("BEACH")
                .build();

        when(recommendationService.recordFeedback(any(), eq("dest-bali"), any(), eq(RecommendationFeedbackType.HELPFUL), any(), any()))
                .thenReturn(feedback);

        mockMvc.perform(post("/api/v1/recommendations/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetId\": \"dest-bali\", \"targetType\": \"DESTINATION\", \"feedbackType\": \"HELPFUL\", \"category\": \"BEACH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.targetId").value("dest-bali"))
                .andExpect(jsonPath("$.data.feedbackType").value("HELPFUL"));
    }

    @Test
    @DisplayName("GET /api/v1/recommendations/preferences returns inferred travel preference profile")
    @WithMockUser(username = "traveler-1")
    void testGetUserPreferences() throws Exception {
        UserPreferenceProfileDto profile = UserPreferenceProfileDto.builder()
                .userId("traveler-1")
                .topCategories(List.of("BEACH", "LUXURY"))
                .inferredTravelStyle("BEACH & LUXURY TRAVELER")
                .confidenceScore(0.88)
                .build();

        when(recommendationService.getUserPreferenceProfile(any()))
                .thenReturn(profile);

        mockMvc.perform(get("/api/v1/recommendations/preferences")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.topCategories[0]").value("BEACH"))
                .andExpect(jsonPath("$.data.inferredTravelStyle").value("BEACH & LUXURY TRAVELER"));
    }
}
