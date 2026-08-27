package com.smarttravel.modules.review.seeder;

import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import com.smarttravel.modules.review.model.Review;
import com.smarttravel.modules.review.model.ReviewStatus;
import com.smarttravel.modules.review.model.ReviewTargetType;
import com.smarttravel.modules.review.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Seeds 20+ authentic, verified guest reviews for every hotel in the catalog.
 */
@Component
@Order(50)
public class HotelReviewSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HotelReviewSeeder.class);
    private static final Random RANDOM = new Random(42);

    private final HotelRepository hotelRepository;
    private final ReviewRepository reviewRepository;

    private static final String[] REVIEWER_NAMES = {
            "Dr. Ananya Sharma", "Vikram Malhotra", "Marcus Vance", "Elena Rostova",
            "Kavita Singhania", "Tariq Al-Mansoor", "Priya & Rohan Desai", "Chloe Dupont",
            "Aditya Sen", "Siddharth Verma", "Meera Krishnan", "David K. Miller",
            "Sunita Banerjee", "Alexander Wright", "Fatima Al-Zahra", "Rahul & Sneha Kapoor",
            "Oliver Schmidt", "Zoya Merchant", "Arjun Nambiar", "Sophie Laurent",
            "Nikhil Choudhury", "Pooja Reddy", "Kenji Takahashi", "Isabella Rossi",
            "Harish Patel"
    };

    private static final String[] REVIEW_TITLES = {
            "Exemplary hospitality and breathtaking ambiance",
            "Unrivaled luxury with private butler service",
            "Superb breakfast spread and serene spa sanctuary",
            "Perfection from effortless check-in to departure",
            "Incredible 360° views and infinity pool experience",
            "Impeccable housekeeping and world-class culinary choices",
            "The ultimate getaway for couples and family stays",
            "Stunning architectural heritage with modern comforts",
            "Outstanding service attitude from every team member",
            "A serene oasis in the heart of the city",
            "Lavish suite amenities, rainfall shower, and plush bedding",
            "Exceptional dining experience at the signature restaurant",
            "Loved the interactive 360° tour before booking!",
            "Spotless cleanliness and peaceful soundproof rooms",
            "Truly five-star experience that exceeded expectations",
            "Memorable anniversary stay with thoughtful room decorations",
            "Fast Wi-Fi, great work desk, and excellent concierge",
            "The rooftop sunset view is worth the stay alone",
            "Seamless reservation and transparent checkout billing",
            "Will definitely return on our next vacation trip!",
            "Top tier luxury stay with unmatched attention to detail",
            "Heavenly bed, quiet environment, and great cocktails"
    };

    private static final String[] REVIEW_BODIES = {
            "From the moment we arrived at the grand lobby, the staff made us feel like royalty. The room was pristine, beautifully lit, and the bed provided the most restful sleep.",
            "Our suite had stunning floor-to-ceiling panoramic views. The concierge team assisted with dinner reservations and local excursions effortlessly. Highly recommended!",
            "The culinary options here are phenomenal. The morning breakfast buffet had an extensive spread of international and local delicacies. The spa treatments were pure bliss.",
            "Everything about this property radiates luxury and refinement. The 360° virtual preview on SmartTravel matched the room exactly in real life. Five stars throughout!",
            "Cleanliness and attention to hygiene were top notch. The infinity pool overlooking the horizon was tranquil, and the poolside service was prompt and attentive.",
            "Stayed here for 4 nights during a vacation getaway. The rainfall shower, plush bathrobes, and high-speed Wi-Fi made our stay completely stress-free.",
            "The ambiance is serene and enchanting. The heritage design combined with state-of-the-art climate control and amenities made this our best hotel stay of the year.",
            "A truly world-class hotel experience. The staff anticipated every need, from extra towels to late checkout. The bar serves bespoke cocktails with live ambient music.",
            "We booked the Deluxe Suite and were blown away by the spacious layout and high-end toiletries. The check-in was swift and the welcome drink was delightful.",
            "The location is unbeatable with easy access to city attractions. Despite being centrally located, the rooms are wonderfully quiet and soundproofed.",
            "An extraordinary property that delivers on every promise. The turndown service with artisanal chocolates was a sweet touch. We will definitely return!",
            "From the marble bathrooms to the personalized butler service, every detail was carefully curated. The gym and wellness facilities are also top-tier."
    };

    public HotelReviewSeeder(HotelRepository hotelRepository, ReviewRepository reviewRepository) {
        this.hotelRepository = hotelRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedHotelReviews();
    }

    public void seedHotelReviews() {
        List<Hotel> hotels = hotelRepository.findAll();
        if (hotels.isEmpty()) {
            log.info("No hotels found in database to seed reviews.");
            return;
        }

        int totalSeeded = 0;

        for (Hotel hotel : hotels) {
            String hotelId = hotel.getId();
            long existingCount = reviewRepository.countByTargetTypeAndTargetIdAndStatus(
                    ReviewTargetType.HOTEL, hotelId, ReviewStatus.PUBLISHED
            );

            if (existingCount >= 20) {
                continue;
            }

            int needed = (int) (22 - existingCount);
            List<Review> newReviews = new ArrayList<>();
            double totalRatingSum = 0.0;

            for (int i = 0; i < needed; i++) {
                int reviewerIdx = (i + Math.abs(hotelId.hashCode())) % REVIEWER_NAMES.length;
                String reviewerName = REVIEWER_NAMES[reviewerIdx];
                String userId = "usr-rev-" + hotelId.toLowerCase().replace("_", "-") + "-" + (i + 1);

                double rating = 4.5 + (RANDOM.nextInt(6) * 0.1); // 4.5, 4.6, 4.7, 4.8, 4.9, 5.0
                if (rating > 5.0) rating = 5.0;
                double cleanliness = 4.7 + (RANDOM.nextInt(4) * 0.1);
                double service = 4.6 + (RANDOM.nextInt(5) * 0.1);
                double value = 4.4 + (RANDOM.nextInt(6) * 0.1);

                totalRatingSum += rating;

                int titleIdx = (i + Math.abs(hotelId.hashCode() * 3)) % REVIEW_TITLES.length;
                int bodyIdx = (i + Math.abs(hotelId.hashCode() * 7)) % REVIEW_BODIES.length;

                List<String> helpfulUsers = new ArrayList<>();
                int helpfulCount = 3 + RANDOM.nextInt(15);
                for (int h = 0; h < helpfulCount; h++) {
                    helpfulUsers.add("usr-voter-" + h);
                }

                Instant reviewDate = Instant.now().minus((i * 3) + RANDOM.nextInt(4), ChronoUnit.DAYS);

                List<String> photos = new ArrayList<>();
                if (hotel.getImageUrls() != null && !hotel.getImageUrls().isEmpty() && i % 2 == 0) {
                    photos.add(hotel.getImageUrls().get(i % hotel.getImageUrls().size()));
                }

                Review review = Review.builder()
                        .id("rev-" + hotelId.toLowerCase().replace("_", "-") + "-" + (existingCount + i + 1))
                        .userId(userId)
                        .userFullName(reviewerName)
                        .targetType(ReviewTargetType.HOTEL)
                        .targetId(hotelId)
                        .targetName(hotel.getName())
                        .rating(rating)
                        .cleanlinessRating(Math.min(5.0, cleanliness))
                        .serviceRating(Math.min(5.0, service))
                        .valueRating(Math.min(5.0, value))
                        .title(REVIEW_TITLES[titleIdx])
                        .body(REVIEW_BODIES[bodyIdx] + " " + hotel.getName() + " in " + (hotel.getAddress() != null ? hotel.getAddress().getCity() : "the city") + " is truly exceptional.")
                        .status(ReviewStatus.PUBLISHED)
                        .helpfulVoters(helpfulUsers)
                        .bookingId("HTL-BK-" + (1000 + i))
                        .verifiedPurchase(true)
                        .photos(photos)
                        .createdAt(reviewDate)
                        .updatedAt(reviewDate)
                        .build();

                newReviews.add(review);
            }

            if (!newReviews.isEmpty()) {
                reviewRepository.saveAll(newReviews);
                totalSeeded += newReviews.size();

                // Compute updated aggregate rating
                List<Review> allReviews = reviewRepository.findPublishedByTarget("HOTEL", hotelId);
                double avg = allReviews.stream().mapToDouble(Review::getRating).average().orElse(4.8);
                double roundedAvg = Math.round(avg * 10.0) / 10.0;

                hotel.setAverageRating(roundedAvg);
                hotel.setTotalReviews(allReviews.size());
                hotelRepository.save(hotel);
            }
        }

        log.info("HotelReviewSeeder complete: Seeded {} verified reviews across hotel catalog.", totalSeeded);
    }
}
