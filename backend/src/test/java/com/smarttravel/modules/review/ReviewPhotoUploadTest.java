package com.smarttravel.modules.review;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.repository.ReviewRepository;
import com.smarttravel.modules.review.service.LocalReviewMediaStorageServiceImpl;
import com.smarttravel.modules.review.service.ReviewMediaStorageService;
import com.smarttravel.modules.review.service.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewPhotoUploadTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    private ReviewMediaStorageService mediaStorageService;
    private ReviewServiceImpl reviewService;

    @TempDir
    Path tempDir;

    private Review sampleReview;

    @BeforeEach
    void setUp() {
        mediaStorageService = new LocalReviewMediaStorageServiceImpl(tempDir.toString());
        reviewService = new ReviewServiceImpl(reviewRepository, mediaStorageService, mongoTemplate);

        sampleReview = Review.builder()
                .id("rev-100")
                .userId("user-alice")
                .userFullName("Alice Johnson")
                .targetType(ReviewTargetType.FLIGHT)
                .targetId("fl-1")
                .targetName("AI-101")
                .rating(4.5)
                .cleanlinessRating(4.0)
                .serviceRating(5.0)
                .valueRating(4.5)
                .title("Great flight experience")
                .body("The crew was attentive and flight was perfectly on time.")
                .status(ReviewStatus.PUBLISHED)
                .photos(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should successfully upload valid JPEG image and attach photo URL to review")
    void uploadValidPhoto_Success() {
        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "cabin_view.jpg",
                "image/jpeg",
                "fake-jpeg-content".getBytes()
        );

        Review updated = reviewService.attachPhoto("rev-100", "user-alice", file, false);

        assertThat(updated.getPhotos()).hasSize(1);
        assertThat(updated.getPhotos().get(0)).startsWith("/api/v1/reviews/photos/rev_rev-100_");
        assertThat(updated.getPhotos().get(0)).endsWith(".jpg");
    }

    @Test
    @DisplayName("Should successfully upload PNG and WebP images")
    void uploadPngAndWebp_Success() {
        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile pngFile = new MockMultipartFile(
                "file",
                "seat.png",
                "image/png",
                "png-bytes".getBytes()
        );

        MockMultipartFile webpFile = new MockMultipartFile(
                "file",
                "meal.webp",
                "image/webp",
                "webp-bytes".getBytes()
        );

        reviewService.attachPhoto("rev-100", "user-alice", pngFile, false);
        Review updated = reviewService.attachPhoto("rev-100", "user-alice", webpFile, false);

        assertThat(updated.getPhotos()).hasSize(2);
    }

    @Test
    @DisplayName("Should reject invalid non-image MIME types")
    void uploadInvalidType_ThrowsBadRequest() {
        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(sampleReview));

        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "malicious.txt",
                "text/plain",
                "hello world".getBytes()
        );

        assertThatThrownBy(() -> reviewService.attachPhoto("rev-100", "user-alice", textFile, false))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid photo format");
    }

    @Test
    @DisplayName("Should reject oversized photos exceeding 5MB")
    void uploadOversizedPhoto_ThrowsBadRequest() {
        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(sampleReview));

        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeBytes
        );

        assertThatThrownBy(() -> reviewService.attachPhoto("rev-100", "user-alice", largeFile, false))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds maximum limit of 5 MB");
    }

    @Test
    @DisplayName("Should reject photo upload when caller is not the review author")
    void uploadByNonAuthor_ThrowsBadRequest() {
        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(sampleReview));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "data".getBytes()
        );

        assertThatThrownBy(() -> reviewService.attachPhoto("rev-100", "attacker-bob", file, false))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("You can only upload photos to your own reviews");
    }

    @Test
    @DisplayName("Should allow admin to upload photos on behalf of reviews")
    void uploadByAdmin_Success() {
        when(reviewRepository.findById("rev-100")).thenReturn(Optional.of(sampleReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "admin_audit.jpg",
                "image/jpeg",
                "admin-bytes".getBytes()
        );

        Review updated = reviewService.attachPhoto("rev-100", "admin-user", file, true);
        assertThat(updated.getPhotos()).hasSize(1);
    }
}
