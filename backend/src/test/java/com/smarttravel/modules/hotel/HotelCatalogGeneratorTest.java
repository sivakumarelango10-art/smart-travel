package com.smarttravel.modules.hotel;

import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.model.VirtualTour;
import com.smarttravel.modules.hotel.seeder.HotelCatalogGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HotelCatalogGeneratorTest {

    @Test
    @DisplayName("HotelCatalogGenerator generates at least 100 unique realistic hotels")
    void testCatalogVolumeAndUniqueness() {
        List<Hotel> hotels = HotelCatalogGenerator.generateAllHotels();

        assertThat(hotels).isNotNull();
        assertThat(hotels.size()).isGreaterThanOrEqualTo(100);

        // Verify ID uniqueness
        Set<String> ids = new HashSet<>();
        Set<String> names = new HashSet<>();

        for (Hotel hotel : hotels) {
            assertThat(hotel.getId()).isNotBlank();
            assertThat(hotel.getName()).isNotBlank();
            assertThat(hotel.getAddress()).isNotNull();
            assertThat(hotel.getAddress().getCity()).isNotBlank();
            assertThat(hotel.getAddress().getCountry()).isNotBlank();
            assertThat(hotel.getBaseNightlyRate()).isNotNull();
            assertThat(hotel.getStarRating()).isBetween(3, 5);
            assertThat(hotel.getAverageRating()).isBetween(4.0, 5.0);
            assertThat(hotel.getTotalReviews()).isGreaterThan(0);
            assertThat(hotel.getImageUrls()).isNotEmpty();
            assertThat(hotel.getRoomTypes()).isNotEmpty();

            ids.add(hotel.getId());
            names.add(hotel.getName());
        }

        assertThat(ids).hasSameSizeAs(hotels);
    }

    @Test
    @DisplayName("Catalog contains comprehensive 360° virtual tours for hotels and rooms")
    void testVirtualTourCoverage() {
        List<Hotel> hotels = HotelCatalogGenerator.generateAllHotels();

        long hotelsWith360 = hotels.stream()
                .filter(h -> h.getVirtualTour() != null && h.getVirtualTour().isEnabled() && h.getVirtualTour().getPanoramaUrl() != null)
                .count();

        assertThat(hotelsWith360).isGreaterThanOrEqualTo(100);

        // Verify room-level 360 tours
        long roomsWith360 = hotels.stream()
                .flatMap(h -> h.getRoomTypes().stream())
                .filter(r -> r.getVirtualTour() != null && r.getVirtualTour().isEnabled() && r.getVirtualTour().getPanoramaUrl() != null)
                .count();

        assertThat(roomsWith360).isGreaterThan(150);
    }

    @Test
    @DisplayName("Catalog covers key Indian and International destinations")
    void testDestinationCoverage() {
        List<Hotel> hotels = HotelCatalogGenerator.generateAllHotels();

        Set<String> cities = new HashSet<>();
        for (Hotel h : hotels) {
            cities.add(h.getAddress().getCity().toLowerCase());
        }

        assertThat(cities).contains(
                "delhi", "mumbai", "bangalore", "chennai", "hyderabad",
                "goa", "kochi", "jaipur", "udaipur", "coimbatore",
                "madurai", "ahmedabad", "pune", "kolkata", "ooty",
                "mysore", "pondicherry", "tirupati", "varanasi", "rishikesh",
                "dubai", "singapore", "bangkok", "london", "paris",
                "new york", "tokyo", "bali", "maldives"
        );
    }

    @Test
    @DisplayName("Rooms have valid pricing, capacities, and bed types")
    void testRoomTypeIntegrity() {
        List<Hotel> hotels = HotelCatalogGenerator.generateAllHotels();

        for (Hotel hotel : hotels) {
            for (RoomType room : hotel.getRoomTypes()) {
                assertThat(room.getId()).isNotBlank();
                assertThat(room.getName()).isNotBlank();
                assertThat(room.getCategory()).isNotNull();
                assertThat(room.getMaxOccupancy()).isGreaterThanOrEqualTo(1);
                assertThat(room.getNightlyRate()).isNotNull();
                assertThat(room.getTotalNightlyRate()).isGreaterThan(room.getNightlyRate());
                assertThat(room.getAvailableRooms()).isLessThanOrEqualTo(room.getTotalRooms());
            }
        }
    }
}
