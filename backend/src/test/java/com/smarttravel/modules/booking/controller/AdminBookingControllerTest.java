package com.smarttravel.modules.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.modules.booking.dto.BookingCancelRequest;
import com.smarttravel.modules.booking.dto.BookingResponse;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminBookingController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AdminBookingControllerTest {

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
    @DisplayName("GET /api/v1/admin/bookings returns all bookings paginated")
    void testGetAllBookings() throws Exception {
        PageResponse<BookingResponse> pageResponse = PageResponse.from(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1));
        when(bookingService.getAllBookings(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/admin/bookings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].bookingReference").value("ST8K4P2Q"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/bookings/{id} returns booking by ID")
    void testGetBookingById() throws Exception {
        when(bookingService.getBookingById(eq("66c1e101f1a2b3c4d5e6f801"), any(), eq(true)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/admin/bookings/66c1e101f1a2b3c4d5e6f801")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("66c1e101f1a2b3c4d5e6f801"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/bookings/reference/{bookingReference} returns booking by PNR")
    void testGetBookingByReference() throws Exception {
        when(bookingService.getBookingByReference(eq("ST8K4P2Q"), any(), eq(true)))
                .thenReturn(sampleResponse);

        mockMvc.perform(get("/api/v1/admin/bookings/reference/ST8K4P2Q")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookingReference").value("ST8K4P2Q"));
    }

    @Test
    @DisplayName("PATCH /api/v1/admin/bookings/{id}/cancel administratively cancels booking")
    void testAdminCancelBooking() throws Exception {
        sampleResponse.setStatus(BookingStatus.CANCELLED);
        sampleResponse.setCancellationReason("Admin policy enforcement");

        when(bookingService.cancelBooking(eq("66c1e101f1a2b3c4d5e6f801"), any(), any(), eq(true)))
                .thenReturn(sampleResponse);

        BookingCancelRequest cancelReq = new BookingCancelRequest("Admin policy enforcement");

        mockMvc.perform(patch("/api/v1/admin/bookings/66c1e101f1a2b3c4d5e6f801/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));
    }
}
