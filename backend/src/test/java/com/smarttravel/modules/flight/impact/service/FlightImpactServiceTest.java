package com.smarttravel.modules.flight.impact.service;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.repository.CheckInRepository;
import com.smarttravel.modules.flight.impact.dto.FlightImpactSummaryDto;
import com.smarttravel.modules.flight.model.Flight;
import com.smarttravel.modules.flight.repository.FlightRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightImpactServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CheckInRepository checkInRepository;

    @InjectMocks
    private FlightImpactServiceImpl impactService;

    @Test
    @DisplayName("Should correctly classify bookings into confirmed, checked-in, and pending impact categories")
    void shouldAssessImpactSummaryCorrectly() {
        Flight flight = Flight.builder()
                .id("fl-1")
                .flightNumber("ST-101")
                .build();

        Booking confirmedBooking = Booking.builder().id("bk-1").status(BookingStatus.CONFIRMED).build();
        Booking checkedInBooking = Booking.builder().id("bk-2").status(BookingStatus.CONFIRMED).build();
        Booking pendingBooking = Booking.builder().id("bk-3").status(BookingStatus.PENDING).build();
        Booking cancelledBooking = Booking.builder().id("bk-4").status(BookingStatus.CANCELLED).build();

        when(flightRepository.findById("fl-1")).thenReturn(Optional.of(flight));
        when(bookingRepository.findByFlightId("fl-1")).thenReturn(List.of(confirmedBooking, checkedInBooking, pendingBooking, cancelledBooking));
        when(checkInRepository.existsByBookingId("bk-1")).thenReturn(false);
        when(checkInRepository.existsByBookingId("bk-2")).thenReturn(true);

        FlightImpactSummaryDto summary = impactService.getDisruptionImpactSummary("fl-1");

        assertThat(summary.getTotalAffectedBookings()).isEqualTo(2); // Only CONFIRMED bookings
        assertThat(summary.getConfirmedBookingsCount()).isEqualTo(2);
        assertThat(summary.getCheckedInBookingsCount()).isEqualTo(1);
        assertThat(summary.getPendingBookingsCount()).isEqualTo(1);
        assertThat(summary.getAffectedBookingIds()).containsExactlyInAnyOrder("bk-1", "bk-2");
    }
}
