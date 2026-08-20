package com.smarttravel.modules.hotel;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.HotelAddress;
import com.smarttravel.modules.hotel.model.RoomCategory;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.repository.HotelRepository;
import com.smarttravel.modules.hotel.service.HotelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Hotel sampleHotel;
    private RoomType sampleRoom;

    @BeforeEach
    void setUp() {
        sampleRoom = RoomType.builder()
                .id("room-01")
                .name("Deluxe Room")
                .category(RoomCategory.DELUXE)
                .totalRooms(10)
                .availableRooms(5)
                .nightlyRate(new BigDecimal("6000.00"))
                .build();

        sampleHotel = Hotel.builder()
                .id("hotel-01")
                .name("The Grand Palace")
                .address(HotelAddress.builder().city("Mumbai").state("Maharashtra").build())
                .starRating(5)
                .baseNightlyRate(new BigDecimal("6000.00"))
                .roomTypes(List.of(sampleRoom))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("searchHotels filters by city and star rating")
    void testSearchHotels() {
        Page<Hotel> page = new PageImpl<>(List.of(sampleHotel));
        when(hotelRepository.searchByCityAndStars("Mumbai", 5, PageRequest.of(0, 10)))
                .thenReturn(page);

        Page<Hotel> result = hotelService.searchHotels("Mumbai", null, 5, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("The Grand Palace");
    }

    @Test
    @DisplayName("holdRoom atomically decrements availableRooms via findAndModify")
    void testHoldRoom_AtomicSuccess() {
        RoomType updatedRoom = RoomType.builder()
                .id("room-01")
                .name("Deluxe Room")
                .category(RoomCategory.DELUXE)
                .totalRooms(10)
                .availableRooms(4) // decremented by 1
                .build();

        Hotel updatedHotel = Hotel.builder()
                .id("hotel-01")
                .roomTypes(List.of(updatedRoom))
                .build();

        when(mongoTemplate.findAndModify(
                any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Hotel.class)))
                .thenReturn(updatedHotel);

        RoomType held = hotelService.holdRoom("hotel-01", "room-01", 1);

        assertThat(held).isNotNull();
        assertThat(held.getAvailableRooms()).isEqualTo(4);
        verify(mongoTemplate).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Hotel.class));
    }

    @Test
    @DisplayName("holdRoom throws BadRequestException when insufficient rooms available")
    void testHoldRoom_InsufficientRooms() {
        when(mongoTemplate.findAndModify(
                any(Query.class), any(Update.class), any(FindAndModifyOptions.class), eq(Hotel.class)))
                .thenReturn(null); // Concurrency guard: condition failed

        assertThatThrownBy(() -> hotelService.holdRoom("hotel-01", "room-01", 1))
                .isInstanceOf(BadRequestException.class);
    }
}
