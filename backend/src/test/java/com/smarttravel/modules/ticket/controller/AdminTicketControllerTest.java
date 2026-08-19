package com.smarttravel.modules.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.GlobalExceptionHandler;
import com.smarttravel.common.response.PageResponse;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.ticket.dto.TicketResponse;
import com.smarttravel.modules.ticket.model.TicketStatus;
import com.smarttravel.modules.ticket.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminTicketController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("AdminTicketController WebMvc Slice Tests")
class AdminTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private TicketService ticketService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/v1/admin/tickets - Should return all tickets for admin")
    void shouldReturnAllTicketsForAdmin() throws Exception {
        TicketResponse ticket = TicketResponse.builder()
                .id("tkt-001")
                .ticketNumber("ST-8K4P2Q7X9Y1Z")
                .bookingReference("ST8K4P2Q")
                .status(TicketStatus.ISSUED)
                .totalAmount(new BigDecimal("5750.00"))
                .currency("INR")
                .issuedAt(Instant.now())
                .build();

        PageResponse<TicketResponse> pageResponse = PageResponse.from(new PageImpl<>(List.of(ticket), PageRequest.of(0, 20), 1));
        when(ticketService.getAllTickets(any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/admin/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].ticketNumber").value("ST-8K4P2Q7X9Y1Z"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/tickets/{bookingId}/issue - Should retry and issue ticket")
    void shouldRetryTicketIssuance() throws Exception {
        TicketResponse ticket = TicketResponse.builder()
                .id("tkt-001")
                .ticketNumber("ST-8K4P2Q7X9Y1Z")
                .bookingReference("ST8K4P2Q")
                .status(TicketStatus.ISSUED)
                .build();

        when(ticketService.retryIssueTicket(eq("bk-001"))).thenReturn(ticket);

        mockMvc.perform(post("/api/v1/admin/tickets/bk-001/issue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ticketNumber").value("ST-8K4P2Q7X9Y1Z"));
    }
}
