package com.smarttravel.modules.notification.controller;

import com.smarttravel.common.response.PageResponse;

import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.notification.dto.NotificationResponse;
import com.smarttravel.modules.notification.dto.UnreadCountResponse;
import com.smarttravel.modules.notification.model.NotificationStatus;
import com.smarttravel.modules.notification.model.NotificationType;
import com.smarttravel.modules.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerNotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class CustomerNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Customer can retrieve paginated notifications")
    @WithMockUser(username = "sarah@smarttravel.com")
    void shouldGetUserNotifications() throws Exception {
        NotificationResponse res = NotificationResponse.builder()
                .id("notif-1")
                .subject("Flight Delay Notice")
                .notificationType(NotificationType.FLIGHT_DELAYED)
                .status(NotificationStatus.SENT)
                .build();

        PageResponse<NotificationResponse> page = PageResponse.from(new PageImpl<>(List.of(res), PageRequest.of(0, 10), 1));
        when(notificationService.getUserNotifications(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].subject").value("Flight Delay Notice"));
    }

    @Test
    @DisplayName("Customer can retrieve unread count")
    @WithMockUser(username = "sarah@smarttravel.com")
    void shouldGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(any())).thenReturn(new UnreadCountResponse(4L));

        mockMvc.perform(get("/api/v1/notifications/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(4));
    }

    @Test
    @DisplayName("Customer can mark notification as read")
    @WithMockUser(username = "sarah@smarttravel.com")
    void shouldMarkNotificationAsRead() throws Exception {
        NotificationResponse res = NotificationResponse.builder()
                .id("notif-1")
                .read(true)
                .build();

        when(notificationService.markAsRead(eq("notif-1"), any(), eq(false))).thenReturn(res);

        mockMvc.perform(patch("/api/v1/notifications/notif-1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read").value(true));
    }
}
