package com.smarttravel.modules.review;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewReply;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.repository.ReviewReplyRepository;
import com.smarttravel.modules.review.repository.ReviewRepository;
import com.smarttravel.modules.review.service.ReviewReplyService;
import com.smarttravel.modules.review.service.ReviewReplyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewReplyServiceTest {

    @Mock
    private ReviewReplyRepository replyRepository;

    @Mock
    private ReviewRepository reviewRepository;

    private ReviewReplyService replyService;

    private Review sampleReview;
    private ReviewReply sampleReply;

    @BeforeEach
    void setUp() {
        replyService = new ReviewReplyServiceImpl(replyRepository, reviewRepository);

        sampleReview = Review.builder()
                .id("rev-1")
                .userId("user-1")
                .targetType(ReviewTargetType.HOTEL)
                .targetId("hotel-taj")
                .rating(5.0)
                .title("Superb stay")
                .body("Loved every moment of our vacation.")
                .status(ReviewStatus.PUBLISHED)
                .build();

        sampleReply = ReviewReply.builder()
                .id("reply-1")
                .reviewId("rev-1")
                .userId("hotel-mgr")
                .userName("Taj General Manager")
                .content("Thank you for staying with us!")
                .status(ReviewStatus.PUBLISHED)
                .build();
    }

    @Test
    @DisplayName("Should create review reply successfully")
    void createReply_Success() {
        when(reviewRepository.findById("rev-1")).thenReturn(Optional.of(sampleReview));
        when(replyRepository.save(any(ReviewReply.class))).thenAnswer(i -> {
            ReviewReply r = i.getArgument(0);
            r.setId("reply-new");
            return r;
        });

        ReviewReply created = replyService.createReply("rev-1", "user-2", "Bob", "Glad to hear that!");

        assertThat(created.getId()).isEqualTo("reply-new");
        assertThat(created.getContent()).isEqualTo("Glad to hear that!");
        assertThat(created.getStatus()).isEqualTo(ReviewStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Should reject reply with blank or too short content")
    void createReply_BlankContent_ThrowsBadRequest() {
        assertThatThrownBy(() -> replyService.createReply("rev-1", "user-2", "Bob", " "))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 2 characters");
    }

    @Test
    @DisplayName("Should retrieve published replies for a review")
    void getRepliesForReview_Success() {
        when(replyRepository.findByReviewIdAndStatusOrderByCreatedAtAsc("rev-1", ReviewStatus.PUBLISHED))
                .thenReturn(List.of(sampleReply));

        List<ReviewReply> replies = replyService.getRepliesForReview("rev-1");
        assertThat(replies).hasSize(1);
        assertThat(replies.get(0).getContent()).isEqualTo("Thank you for staying with us!");
    }

    @Test
    @DisplayName("Should edit own reply successfully")
    void updateReply_OwnReply_Success() {
        when(replyRepository.findById("reply-1")).thenReturn(Optional.of(sampleReply));
        when(replyRepository.save(any(ReviewReply.class))).thenAnswer(i -> i.getArgument(0));

        ReviewReply updated = replyService.updateReply("reply-1", "hotel-mgr", "Updated thank you message!", false);
        assertThat(updated.getContent()).isEqualTo("Updated thank you message!");
    }

    @Test
    @DisplayName("Should reject editing someone else's reply")
    void updateReply_OtherUser_ThrowsBadRequest() {
        when(replyRepository.findById("reply-1")).thenReturn(Optional.of(sampleReply));

        assertThatThrownBy(() -> replyService.updateReply("reply-1", "attacker-user", "Hacked!", false))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("You can only edit your own replies");
    }

    @Test
    @DisplayName("Should delete own reply successfully")
    void deleteReply_OwnReply_Success() {
        when(replyRepository.findById("reply-1")).thenReturn(Optional.of(sampleReply));

        replyService.deleteReply("reply-1", "hotel-mgr", false);
        verify(replyRepository, times(1)).deleteById("reply-1");
    }
}
