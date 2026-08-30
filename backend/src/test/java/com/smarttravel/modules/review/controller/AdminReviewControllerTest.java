package com.smarttravel.modules.review.controller;

import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminReviewController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
class AdminReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    @DisplayName("GET /api/v1/admin/reviews/flagged returns 200 OK with flagged reviews list")
    void testGetFlaggedReviews() throws Exception {
        Review flagged = Review.builder()
                .id("rev-flag-1")
                .targetType(ReviewTargetType.HOTEL)
                .targetId("hotel-101")
                .status(ReviewStatus.FLAGGED)
                .title("Spam title")
                .body("Spam body review content text")
                .build();

        when(reviewService.getReviewsForAdmin(eq(ReviewStatus.FLAGGED), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(flagged)));

        mockMvc.perform(get("/api/v1/admin/reviews/flagged")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value("rev-flag-1"))
                .andExpect(jsonPath("$.data.content[0].status").value("FLAGGED"));
    }

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/admin/reviews/{id}/approve approves flagged review")
    void testApproveReview() throws Exception {
        Review approved = Review.builder()
                .id("rev-flag-1")
                .status(ReviewStatus.PUBLISHED)
                .moderatedBy("admin-user")
                .build();

        when(reviewService.approveReview(eq("rev-flag-1"), any()))
                .thenReturn(approved);

        mockMvc.perform(post("/api/v1/admin/reviews/rev-flag-1/approve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PUBLISHED"));
    }

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/admin/reviews/{id}/remove removes review with custom reason")
    void testRemoveReview() throws Exception {
        Review removed = Review.builder()
                .id("rev-flag-2")
                .status(ReviewStatus.REMOVED)
                .moderationNote("Inappropriate language")
                .build();

        when(reviewService.removeReview(eq("rev-flag-2"), any(), eq("Inappropriate language")))
                .thenReturn(removed);

        mockMvc.perform(post("/api/v1/admin/reviews/rev-flag-2/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Inappropriate language\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REMOVED"));
    }
}
