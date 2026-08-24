package com.smarttravel.modules.booking.requirement4;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.RoomCategory;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import com.smarttravel.modules.hotel.service.HotelServiceImpl;
import com.smarttravel.modules.hotel.websocket.HotelRoomWebSocketPublisher;
import com.smarttravel.modules.hotel.websocket.RoomAvailabilityEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Requirement #4 — Hotel Room Grid, Pricing Upgrades, and Real-time Broadcast Audit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Requirement #4: Hotel Room Grid & Inventory Audit")
class RoomTypeGridAuditTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private HotelRoomWebSocketPublisher roomWebSocketPublisher;

    private HotelServiceImpl hotelService;
    private Hotel testHotel;
    private RoomType standardRoom;
    private RoomType deluxeRoom;
    private RoomType suiteRoom;

    @BeforeEach
    void setUp() {
        hotelService = new HotelServiceImpl(hotelRepository, mongoTemplate, roomWebSocketPublisher);

        standardRoom = RoomType.builder()
                .id("rt-std-01")
                .name("Standard King Room")
                .category(RoomCategory.STANDARD)
                .totalRooms(10)
                .availableRooms(5)
                .maxOccupancy(2)
                .bedType("King")
                .sizeInSqFt(320)
                .nightlyRate(new BigDecimal("3000.00"))
                .taxAmount(new BigDecimal("360.00"))
                .totalNightlyRate(new BigDecimal("3360.00"))
                .amenities(List.of("Free Wi-Fi", "Air Conditioning"))
                .imageUrls(List.of("https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=800&q=80"))
                .breakfastIncluded(false)
                .refundable(true)
                .build();

        deluxeRoom = RoomType.builder()
                .id("rt-dlx-01")
                .name("Deluxe Ocean Suite")
                .category(RoomCategory.DELUXE)
                .totalRooms(8)
                .availableRooms(3)
                .maxOccupancy(3)
                .bedType("King + Daybed")
                .sizeInSqFt(480)
                .nightlyRate(new BigDecimal("4500.00"))
                .taxAmount(new BigDecimal("540.00"))
                .totalNightlyRate(new BigDecimal("5040.00"))
                .amenities(List.of("Ocean View", "Free Wi-Fi", "Espresso Machine"))
                .imageUrls(List.of("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80"))
                .breakfastIncluded(true)
                .refundable(true)
                .build();

        suiteRoom = RoomType.builder()
                .id("rt-ste-01")
                .name("Presidential Luxury Suite")
                .category(RoomCategory.SUITE)
                .totalRooms(2)
                .availableRooms(1)
                .maxOccupancy(4)
                .bedType("Super King + Double")
                .sizeInSqFt(850)
                .nightlyRate(new BigDecimal("8000.00"))
                .taxAmount(new BigDecimal("960.00"))
                .totalNightlyRate(new BigDecimal("8960.00"))
                .amenities(List.of("Private Balcony", "Jacuzzi", "Butler Service", "Free Breakfast"))
                .imageUrls(List.of("https://images.unsplash.com/photo-1631049307264-da0ec9d70304?auto=format&fit=crop&w=800&q=80"))
                .breakfastIncluded(true)
                .refundable(true)
                .build();

        testHotel = Hotel.builder()
                .id("ht-test-01")
                .name("The Grand Palace Resort")
                .starRating(5)
                .baseNightlyRate(new BigDecimal("3000.00"))
                .roomTypes(List.of(standardRoom, deluxeRoom, suiteRoom))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("[RG-1] Room grid returns detailed specifications, amenities, and accurate upgrade pricing")
    void testRoomTypeGridSpecifications() {
        when(hotelRepository.findById("ht-test-01")).thenReturn(Optional.of(testHotel));

        List<RoomType> rooms = hotelService.getRoomTypes("ht-test-01");

        assertThat(rooms).hasSize(3);
        assertThat(rooms).extracting(RoomType::getCategory)
                .containsExactly(RoomCategory.STANDARD, RoomCategory.DELUXE, RoomCategory.SUITE);

        // Verify upgrade price difference (Suite ₹8000 vs Standard ₹3000 = +₹5000 delta)
        BigDecimal standardRate = standardRoom.getNightlyRate();
        BigDecimal suiteRate = suiteRoom.getNightlyRate();
        BigDecimal upgradeDelta = suiteRate.subtract(standardRate);

        assertThat(upgradeDelta).isEqualByComparingTo(new BigDecimal("5000.00"));
    }

    @Test
    @DisplayName("[RG-2] Atomic room hold decrements availableRooms and broadcasts WebSocket event")
    void testHoldRoomAtomicAndBroadcast() {
        RoomType updatedDeluxe = RoomType.builder()
                .id("rt-dlx-01")
                .name("Deluxe Ocean Suite")
                .category(RoomCategory.DELUXE)
                .totalRooms(8)
                .availableRooms(2) // decremented from 3 to 2
                .nightlyRate(new BigDecimal("4500.00"))
                .build();

        Hotel updatedHotel = Hotel.builder()
                .id("ht-test-01")
                .roomTypes(List.of(standardRoom, updatedDeluxe, suiteRoom))
                .build();

        when(mongoTemplate.findAndModify(any(), any(), any(), eq(Hotel.class))).thenReturn(updatedHotel);

        RoomType held = hotelService.holdRoom("ht-test-01", "rt-dlx-01", 1);

        assertThat(held).isNotNull();
        assertThat(held.getAvailableRooms()).isEqualTo(2);

        // Verify WebSocket event broadcast
        ArgumentCaptor<RoomAvailabilityEvent> captor = ArgumentCaptor.forClass(RoomAvailabilityEvent.class);
        verify(roomWebSocketPublisher).publishRoomUpdate(captor.capture());

        RoomAvailabilityEvent event = captor.getValue();
        assertThat(event.getHotelId()).isEqualTo("ht-test-01");
        assertThat(event.getRoomTypeId()).isEqualTo("rt-dlx-01");
        assertThat(event.getAvailableRooms()).isEqualTo(2);
        assertThat(event.getAction()).isEqualTo("HELD");
    }

    @Test
    @DisplayName("[RG-3] Attempting to hold more rooms than available throws BadRequestException")
    void testHoldRoomInsufficientInventoryThrowsException() {
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(Hotel.class))).thenReturn(null);

        assertThatThrownBy(() -> hotelService.holdRoom("ht-test-01", "rt-ste-01", 5))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient available rooms");
    }
}
