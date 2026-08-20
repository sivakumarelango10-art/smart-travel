package com.smarttravel.modules.review;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.repository.ReviewRepository;
import com.smarttravel.modules.review.service.ReviewServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private com.smarttravel.modules.review.service.storage.ReviewMediaStorageService mediaStorageService;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    @Test
    @DisplayName("createReview persists new verified review when valid")
    void testCreateReview_Success() {
        when(reviewRepository.findByUserIdAndTargetId("user-1", "hotel-01")).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            r.setId("rev-01");
            return r;
        });

        Review result = reviewService.createReview(
                "user-1", "Aarav Sharma", ReviewTargetType.HOTEL,
                "hotel-01", "The Grand Palace", 4.5,
                5.0, 4.0, 4.5,
                "Stunning palace stay", "Everything was top-notch from breakfast to checkout.",
                "booking-123"
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("rev-01");
        assertThat(result.getRating()).isEqualTo(4.5);
        assertThat(result.isVerifiedPurchase()).isTrue();
        assertThat(result.getStatus()).isEqualTo(ReviewStatus.PUBLISHED);
    }

    @Test
    @DisplayName("createReview throws ConflictException if user already reviewed target")
    void testCreateReview_Duplicate() {
        Review existing = Review.builder().id("rev-old").userId("user-1").targetId("hotel-01").build();
        when(reviewRepository.findByUserIdAndTargetId("user-1", "hotel-01")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> reviewService.createReview(
                "user-1", "Aarav Sharma", ReviewTargetType.HOTEL,
                "hotel-01", "The Grand Palace", 5.0,
                5.0, 5.0, 5.0,
                "Title", "Body body body body body",
                null
        )).isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("voteHelpful toggles vote on/off and updates voter list")
    void testVoteHelpful_Toggle() {
        Review review = Review.builder()
                .id("rev-01")
                .userId("other-user")
                .helpfulVoters(new ArrayList<>())
                .build();

        when(reviewRepository.findById("rev-01")).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // First vote: adds user
        Review afterVote1 = reviewService.voteHelpful("rev-01", "user-1");
        assertThat(afterVote1.getHelpfulVoters()).contains("user-1");

        // Second vote: removes user (toggle)
        Review afterVote2 = reviewService.voteHelpful("rev-01", "user-1");
        assertThat(afterVote2.getHelpfulVoters()).doesNotContain("user-1");
    }

    @Test
    @DisplayName("flagReview auto-promotes status to FLAGGED when reaching 3 flags threshold")
    void testFlagReview_AutoFlagThreshold() {
        List<String> flags = new ArrayList<>(List.of("user-A", "user-B"));
        Review review = Review.builder()
                .id("rev-01")
                .userId("author-user")
                .status(ReviewStatus.PUBLISHED)
                .flaggedBy(flags)
                .build();

        when(reviewRepository.findById("rev-01")).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // 3rd flag from user-C
        Review result = reviewService.flagReview("rev-01", "user-C");

        assertThat(result.getFlaggedBy()).hasSize(3);
        assertThat(result.getStatus()).isEqualTo(ReviewStatus.FLAGGED);
    }
}
