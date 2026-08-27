package com.smarttravel.modules.hotel;

import com.smarttravel.common.exception.BadRequestException;
import com.smarttravel.common.exception.ForbiddenException;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.hotel.dto.HotelBookingDto;
import com.smarttravel.modules.hotel.model.Hotel;
import com.smarttravel.modules.hotel.model.HotelAddress;
import com.smarttravel.modules.hotel.model.HotelBooking;
import com.smarttravel.modules.hotel.model.RoomCategory;
import com.smarttravel.modules.hotel.model.RoomType;
import com.smarttravel.modules.hotel.repository.HotelBookingRepository;
import com.smarttravel.modules.hotel.service.HotelBookingServiceImpl;
import com.smarttravel.modules.hotel.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelBookingServiceTest {

    @Mock
    private HotelBookingRepository hotelBookingRepository;

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private HotelBookingServiceImpl hotelBookingService;

    private Hotel sampleHotel;
    private RoomType sampleRoom;

    @BeforeEach
    void setUp() {
        sampleRoom = RoomType.builder()
                .id("rm-01")
                .name("Deluxe Palm Suite")
                .category(RoomCategory.SUITE)
                .nightlyRate(new BigDecimal("10000.00"))
                .availableRooms(5)
                .totalRooms(10)
                .maxOccupancy(2)
                .build();

        sampleHotel = Hotel.builder()
                .id("htl-dxb-01")
                .name("Burj Al Arab Jumeirah")
                .address(HotelAddress.builder().city("Dubai").line1("Jumeirah Beach Road").country("UAE").build())
                .baseNightlyRate(new BigDecimal("10000.00"))
                .currency("INR")
                .roomTypes(List.of(sampleRoom))
                .imageUrls(List.of("https://images.unsplash.com/photo-1"))
                .active(true)
                .build();
    }

    @Test
    @DisplayName("calculatePrice calculates authoritative stay amount with 12% tax")
    void testCalculatePriceSuccess() {
        when(hotelService.getHotelById("htl-dxb-01")).thenReturn(sampleHotel);

        LocalDate in = LocalDate.now().plusDays(2);
        LocalDate out = LocalDate.now().plusDays(5); // 3 nights

        HotelBookingDto.PriceCalculateRequest req = new HotelBookingDto.PriceCalculateRequest(
                "htl-dxb-01", "rm-01", in, out, 2, 1, null
        );

        HotelBookingDto.PriceCalculateResponse res = hotelBookingService.calculatePrice(req);

        assertThat(res.nights()).isEqualTo(3);
        assertThat(res.baseAmount()).isEqualByComparingTo("30000.00");
        assertThat(res.taxAmount()).isEqualByComparingTo("3600.00");
        assertThat(res.totalAmount()).isEqualByComparingTo("33600.00");
        assertThat(res.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("calculatePrice throws BadRequestException if checkout is not after check-in")
    void testCalculatePriceInvalidDates() {
        LocalDate in = LocalDate.now().plusDays(3);
        LocalDate out = in;

        HotelBookingDto.PriceCalculateRequest req = new HotelBookingDto.PriceCalculateRequest(
                "htl-dxb-01", "rm-01", in, out, 2, 1, null
        );

        assertThatThrownBy(() -> hotelBookingService.calculatePrice(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Check-out date must be at least 1 day after check-in date");
    }

    @Test
    @DisplayName("createBooking successfully creates confirmed reservation and saves in repository")
    void testCreateBookingSuccess() {
        when(hotelService.getHotelById("htl-dxb-01")).thenReturn(sampleHotel);
        when(hotelBookingRepository.existsByBookingReference(any())).thenReturn(false);
        when(hotelBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDate in = LocalDate.now().plusDays(3);
        LocalDate out = LocalDate.now().plusDays(5);

        HotelBookingDto.CreateHotelBookingRequest req = new HotelBookingDto.CreateHotelBookingRequest(
                "htl-dxb-01", "rm-01", in, out, 2, 1,
                "Alice Smith", "alice@example.com", "+91-9876543210",
                "High floor please", null, "CARD"
        );

        HotelBookingDto.HotelBookingResponse res = hotelBookingService.createBooking(req, "usr-alice", "alice@example.com");

        assertThat(res).isNotNull();
        assertThat(res.bookingReference()).startsWith("HTL-");
        assertThat(res.totalAmount()).isEqualByComparingTo("22400.00");
        assertThat(res.status()).isEqualTo(BookingStatus.CONFIRMED);
        verify(hotelService).holdRoom("htl-dxb-01", "rm-01", 1);
        verify(hotelBookingRepository).save(any(HotelBooking.class));
    }

    @Test
    @DisplayName("createBooking throws BadRequestException when rooms are sold out")
    void testCreateBookingSoldOut() {
        sampleRoom.setAvailableRooms(0);
        when(hotelService.getHotelById("htl-dxb-01")).thenReturn(sampleHotel);

        LocalDate in = LocalDate.now().plusDays(3);
        LocalDate out = LocalDate.now().plusDays(5);

        HotelBookingDto.CreateHotelBookingRequest req = new HotelBookingDto.CreateHotelBookingRequest(
                "htl-dxb-01", "rm-01", in, out, 2, 1,
                "Alice Smith", "alice@example.com", "+91-9876543210",
                null, null, "CARD"
        );

        assertThatThrownBy(() -> hotelBookingService.createBooking(req, "usr-alice", "alice@example.com"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("sold out");
    }

    @Test
    @DisplayName("cancelBooking provides 100% refund when cancelled > 7 days before check-in")
    void testCancelBookingFullRefund() {
        HotelBooking booking = HotelBooking.builder()
                .id("hbk-123")
                .bookingReference("HTL-778899")
                .userId("usr-alice")
                .hotelId("htl-dxb-01")
                .roomTypeId("rm-01")
                .roomCount(1)
                .checkInDate(LocalDate.now().plusDays(10))
                .checkOutDate(LocalDate.now().plusDays(12))
                .totalAmount(new BigDecimal("22400.00"))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(hotelBookingRepository.findById("hbk-123")).thenReturn(Optional.of(booking));
        when(hotelBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HotelBookingDto.HotelBookingResponse res = hotelBookingService.cancelBooking("hbk-123", "usr-alice", "Schedule changed");

        assertThat(res.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(res.refundAmount()).isEqualByComparingTo("22400.00");
        verify(hotelService).releaseRoom("htl-dxb-01", "rm-01", 1);
    }

    @Test
    @DisplayName("cancelBooking provides 50% refund when cancelled 2 days before check-in")
    void testCancelBookingPartialRefund() {
        HotelBooking booking = HotelBooking.builder()
                .id("hbk-123")
                .bookingReference("HTL-778899")
                .userId("usr-alice")
                .hotelId("htl-dxb-01")
                .roomTypeId("rm-01")
                .roomCount(1)
                .checkInDate(LocalDate.now().plusDays(2))
                .checkOutDate(LocalDate.now().plusDays(4))
                .totalAmount(new BigDecimal("20000.00"))
                .status(BookingStatus.CONFIRMED)
                .build();

        when(hotelBookingRepository.findById("hbk-123")).thenReturn(Optional.of(booking));
        when(hotelBookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        HotelBookingDto.HotelBookingResponse res = hotelBookingService.cancelBooking("hbk-123", "usr-alice", "Personal reason");

        assertThat(res.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(res.refundAmount()).isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("getBookingById throws ForbiddenException if accessed by different user (IDOR protection)")
    void testGetBookingByIdForbidden() {
        HotelBooking booking = HotelBooking.builder()
                .id("hbk-123")
                .userId("usr-alice")
                .build();

        when(hotelBookingRepository.findById("hbk-123")).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> hotelBookingService.getBookingById("hbk-123", "usr-mallory"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Access denied");
    }
}
