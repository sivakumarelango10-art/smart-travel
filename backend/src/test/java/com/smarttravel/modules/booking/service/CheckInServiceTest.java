package com.smarttravel.modules.booking.service;

import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.modules.booking.config.CheckInProperties;

import com.smarttravel.modules.booking.dto.CheckInResponse;

import com.smarttravel.modules.booking.model.Booking;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.model.CheckIn;
import com.smarttravel.modules.booking.model.CheckInStatus;
import com.smarttravel.modules.booking.model.Passenger;
import com.smarttravel.modules.booking.model.PassengerCheckInInfo;
import com.smarttravel.modules.booking.repository.BoardingPassRepository;
import com.smarttravel.modules.booking.repository.BookingRepository;
import com.smarttravel.modules.booking.repository.CheckInRepository;

import com.smarttravel.modules.flight.model.AirportInfo;
import com.smarttravel.modules.flight.model.CabinClass;

import com.smarttravel.modules.flight.service.SeatMapService;
import com.smarttravel.modules.ticket.model.PassengerTicketInfo;
import com.smarttravel.modules.ticket.model.Ticket;
import com.smarttravel.modules.ticket.repository.TicketRepository;
import com.smarttravel.modules.ticket.service.TicketNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInServiceTest {

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private BoardingPassRepository boardingPassRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private SeatMapService seatMapService;

    @Mock
    private BoardingPassPdfService boardingPassPdfService;

    private CheckInProperties checkInProperties;
    private TicketNumberGenerator numberGenerator;
    private CheckInService checkInService;

    private Booking sampleBooking;
    private Ticket sampleTicket;

    @BeforeEach
    void setUp() {
        checkInProperties = new CheckInProperties();
        numberGenerator = new TicketNumberGenerator();
        checkInService = new CheckInServiceImpl(
                checkInRepository,
                boardingPassRepository,
                bookingRepository,
                ticketRepository,
                seatMapService,
                boardingPassPdfService,
                checkInProperties,
                numberGenerator
        );

        sampleBooking = Booking.builder()
                .id("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-sarah")
                .flightId("fl-100")
                .flightNumber("ST-101")
                .airline("SmartTravel Express")
                .cabinClass(CabinClass.ECONOMY)
                .departureAirport(AirportInfo.builder().code("DEL").city("Delhi").build())
                .arrivalAirport(AirportInfo.builder().code("BOM").city("Mumbai").build())
                .departureTime(Instant.now().plusSeconds(3600 * 12)) // 12 hours away (valid window)
                .status(BookingStatus.CONFIRMED)
                .passengers(List.of(
                        Passenger.builder().passengerId("p-1").title("Ms").firstName("Sarah").lastName("Connor").seatNumber("12A").build()
                ))
                .build();

        sampleTicket = Ticket.builder()
                .id("tkt-100")
                .ticketNumber("ST-MW827QQJRL45")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .userId("user-sarah")
                .passengers(List.of(
                        PassengerTicketInfo.builder().firstName("Sarah").lastName("Connor").seatNumber("12A").eTicketNumber("ST-MW827QQJRL45-01").build()
                ))
                .build();
    }

    @Test
    @DisplayName("performCheckIn succeeds and creates CheckIn and BoardingPass")
    void testPerformCheckInSuccess() {
        when(bookingRepository.findByIdAndUserId("bk-100", "user-sarah")).thenReturn(Optional.of(sampleBooking));
        when(ticketRepository.findFirstByBookingId("bk-100")).thenReturn(Optional.of(sampleTicket));
        when(checkInRepository.findByBookingId("bk-100")).thenReturn(Optional.empty());
        when(seatMapService.getSeatsForFlight("fl-100", CabinClass.ECONOMY)).thenReturn(List.of());
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(inv -> {
            CheckIn c = inv.getArgument(0);
            c.setId("ci-100");
            return c;
        });

        CheckInResponse response = checkInService.performCheckIn("bk-100", null, "user-sarah", false);

        assertThat(response).isNotNull();
        assertThat(response.getBookingReference()).isEqualTo("ST8K4P2Q");
        assertThat(response.getStatus()).isEqualTo(CheckInStatus.COMPLETED);
        assertThat(response.getPassengers()).hasSize(1);
        assertThat(response.getPassengers().get(0).getSeatNumber()).isEqualTo("12A");
        assertThat(response.getPassengers().get(0).getETicketNumber()).isEqualTo("ST-MW827QQJRL45-01");
        assertThat(response.getPassengers().get(0).getBoardingPassNumber()).startsWith("BP-");

        verify(boardingPassRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("performCheckIn is idempotent and returns existing check-in")
    void testPerformCheckInIdempotent() {
        when(bookingRepository.findByIdAndUserId("bk-100", "user-sarah")).thenReturn(Optional.of(sampleBooking));
        when(ticketRepository.findFirstByBookingId("bk-100")).thenReturn(Optional.of(sampleTicket));

        CheckIn existing = CheckIn.builder()
                .id("ci-100")
                .checkInNumber("CI-EXISTING123")
                .bookingId("bk-100")
                .bookingReference("ST8K4P2Q")
                .flightNumber("ST-101")
                .status(CheckInStatus.COMPLETED)
                .checkedInAt(Instant.now())
                .passengers(List.of(
                        PassengerCheckInInfo.builder().title("Ms").firstName("Sarah").lastName("Connor").seatNumber("12A").eTicketNumber("ST-MW827QQJRL45-01").boardingPassNumber("BP-EXISTING123").build()
                ))
                .build();
        when(checkInRepository.findByBookingId("bk-100")).thenReturn(Optional.of(existing));

        CheckInResponse response = checkInService.performCheckIn("bk-100", null, "user-sarah", false);

        assertThat(response).isNotNull();
        assertThat(response.getCheckInNumber()).isEqualTo("CI-EXISTING123");
        assertThat(response.getPassengers().get(0).getBoardingPassNumber()).isEqualTo("BP-EXISTING123");
    }

    @Test
    @DisplayName("performCheckIn throws ConflictException if before check-in window (e.g. 48h before)")
    void testPerformCheckInTooEarly() {
        sampleBooking.setDepartureTime(Instant.now().plusSeconds(3600 * 48)); // 48 hours away
        when(bookingRepository.findByIdAndUserId("bk-100", "user-sarah")).thenReturn(Optional.of(sampleBooking));
        when(ticketRepository.findFirstByBookingId("bk-100")).thenReturn(Optional.of(sampleTicket));

        assertThatThrownBy(() -> checkInService.performCheckIn("bk-100", null, "user-sarah", false))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Online check-in opens 24 hours before flight departure");
    }

    @Test
    @DisplayName("performCheckIn throws ConflictException if after check-in window (e.g. 30 mins before)")
    void testPerformCheckInTooLate() {
        sampleBooking.setDepartureTime(Instant.now().plusSeconds(1800)); // 30 mins away
        when(bookingRepository.findByIdAndUserId("bk-100", "user-sarah")).thenReturn(Optional.of(sampleBooking));
        when(ticketRepository.findFirstByBookingId("bk-100")).thenReturn(Optional.of(sampleTicket));

        assertThatThrownBy(() -> checkInService.performCheckIn("bk-100", null, "user-sarah", false))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Online check-in closed 60 minutes before flight departure");
    }

    @Test
    @DisplayName("performCheckIn throws ResourceNotFoundException for unauthorized user (IDOR)")
    void testPerformCheckInIdor() {
        when(bookingRepository.findByIdAndUserId("bk-100", "user-hacker")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> checkInService.performCheckIn("bk-100", null, "user-hacker", false))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("performCheckIn auto-issues missing ticket for confirmed booking without throwing conflict")
    void testPerformCheckIn_AutoIssuesMissingTicket() {
        com.smarttravel.modules.ticket.service.TicketService mockTicketService = org.mockito.Mockito.mock(com.smarttravel.modules.ticket.service.TicketService.class);
        CheckInService checkInServiceWithTicket = new CheckInServiceImpl(
                checkInRepository,
                boardingPassRepository,
                bookingRepository,
                ticketRepository,
                seatMapService,
                boardingPassPdfService,
                checkInProperties,
                numberGenerator,
                mockTicketService
        );

        when(bookingRepository.findByIdAndUserId("bk-100", "user-sarah")).thenReturn(Optional.of(sampleBooking));
        // First lookup fails, after issueTicket it returns sampleTicket
        when(ticketRepository.findFirstByBookingId("bk-100")).thenReturn(Optional.empty()).thenReturn(Optional.of(sampleTicket));
        when(checkInRepository.findByBookingId("bk-100")).thenReturn(Optional.empty());
        when(seatMapService.getSeatsForFlight("fl-100", CabinClass.ECONOMY)).thenReturn(List.of());
        when(checkInRepository.save(any(CheckIn.class))).thenAnswer(inv -> {
            CheckIn c = inv.getArgument(0);
            c.setId("ci-100");
            return c;
        });

        CheckInResponse response = checkInServiceWithTicket.performCheckIn("bk-100", null, "user-sarah", false);

        org.mockito.Mockito.verify(mockTicketService, org.mockito.Mockito.times(1)).issueTicket("bk-100");
        assertThat(response).isNotNull();
        assertThat(response.getBookingReference()).isEqualTo("ST8K4P2Q");
    }
}
