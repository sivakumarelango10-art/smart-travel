package com.smarttravel.modules.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.ConflictException;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.exception.InvalidStateTransitionException;
import com.smarttravel.common.exception.ResourceNotFoundException;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingCreateRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
import com.smarttravel.modules.booking.dto.PassengerDto;
import com.smarttravel.modules.booking.model.BookingStatus;
import com.smarttravel.modules.booking.service.BookingService;
import com.smarttravel.modules.flight.model.CabinClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private com.smarttravel.common.security.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private com.smarttravel.common.security.CustomUserDetailsService customUserDetailsService;

    private BookingResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = BookingResponse.builder()
                .id("66c1e101f1a2b3c4d5e6f801")
                .bookingReference("ST8K4P2Q")
                .userId("user-1")
                .userEmail("john.doe@example.com")
                .flightId("66c1e101f1a2b3c4d5e6f702")
                .flightNumber("AI-101")
                .airline("Air India")
                .airlineCode("AI")
                .departureTime(Instant.parse("2026-08-25T10:00:00Z"))
                .arrivalTime(Instant.parse("2026-08-25T12:00:00Z"))
                .durationMinutes(120)
                .cabinClass(CabinClass.ECONOMY)
                .passengerCount(1)
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .status(BookingStatus.CONFIRMED)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/bookings creates booking and returns 201 Created")
    void testCreateBooking_Success() throws Exception {
        PassengerDto passenger = PassengerDto.builder()
                .title("Mr")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId("66c1e101f1a2b3c4d5e6f702")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(passenger))
                .build();

        when(bookingService.createBooking(any(BookingCreateRequest.class), any(), any()))
                .thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("66c1e101f1a2b3c4d5e6f801"))
                .andExpect(jsonPath("$.data.bookingReference").value("ST8K4P2Q"))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /api/v1/bookings returns 400 Bad Request on invalid request body")
    void testCreateBooking_ValidationFailure() throws Exception {
        BookingCreateRequest invalidRequest = BookingCreateRequest.builder()
                .flightId("") // Blank
                .cabinClass(null) // Null
                .passengers(List.of()) // Empty
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("POST /api/v1/bookings returns 409 Conflict on insufficient seat availability")
    void testCreateBooking_Conflict() throws Exception {
        PassengerDto passenger = PassengerDto.builder()
                .title("Mr")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("MALE")
                .nationality("Indian")
                .build();

        BookingCreateRequest request = BookingCreateRequest.builder()
                .flightId("66c1e101f1a2b3c4d5e6f702")
                .cabinClass(CabinClass.ECONOMY)
                .passengers(List.of(passenger))
                .build();

        when(bookingService.createBooking(any(BookingCreateRequest.class), any(), any()))
                .thenThrow(new ConflictException("Insufficient seat availability for the selected cabin: ECONOMY"));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Insufficient seat availability for the selected cabin: ECONOMY"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings returns paginated list of user bookings")
    void testGetUserBookings() throws Exception {
        PageResponse<BookingResponse> pageResponse = PageResponse.from(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1));
        when(bookingService.getUserBookings(any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].bookingReference").value("ST8K4P2Q"))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} returns booking details")
    void testGetBookingById() throws Exception {
        when(bookingService.getBookingById(eq("66c1e101f1a2b3c4d5e6f801"), any(), eq(false)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/bookings/66c1e101f1a2b3c4d5e6f801")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("66c1e101f1a2b3c4d5e6f801"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/{id} returns 404 when not found or unauthorized")
    void testGetBookingById_NotFound() throws Exception {
        when(bookingService.getBookingById(eq("unknown-id"), any(), eq(false)))
                .thenThrow(new ResourceNotFoundException("Booking", "id", "unknown-id"));

        mockMvc.perform(get("/api/v1/bookings/unknown-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/bookings/reference/{bookingReference} returns booking details")
    void testGetBookingByReference() throws Exception {
        when(bookingService.getBookingByReference(eq("ST8K4P2Q"), any(), eq(false)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/bookings/reference/ST8K4P2Q")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("ST8K4P2Q"));
    }

    @Test
    @DisplayName("PATCH /api/v1/bookings/{id}/cancel cancels booking and returns 200 OK")
    void testCancelBooking_Success() throws Exception {
        sampleResponse.setStatus(BookingStatus.CANCELLED);
        sampleResponse.setCancellationReason("Customer request");

        when(bookingService.cancelBooking(eq("66c1e101f1a2b3c4d5e6f801"), any(), any(), eq(false)))
                .thenReturn(sampleResponse);

        BookingCancelRequest cancelReq = new BookingCancelRequest("Customer request");

        mockMvc.perform(patch("/api/v1/bookings/66c1e101f1a2b3c4d5e6f801/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/bookings/{id}/cancel returns 409 Conflict when already cancelled")
    void testCancelBooking_AlreadyCancelled() throws Exception {
        when(bookingService.cancelBooking(eq("66c1e101f1a2b3c4d5e6f801"), any(), any(), eq(false)))
                .thenThrow(new InvalidStateTransitionException("Invalid booking status transition from CANCELLED to CANCELLED"));

        mockMvc.perform(patch("/api/v1/bookings/66c1e101f1a2b3c4d5e6f801/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }
}
