package com.smarttravel.modules.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.notification.dto.PushSubscriptionRequest;
import com.smarttravel.modules.notification.model.PushSubscription;
import com.smarttravel.modules.notification.service.WebPushService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebPushController.class)
@AutoConfigureMockMvc(addFilters = false)
class WebPushControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebPushService webPushService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should retrieve VAPID public key")
    void shouldGetVapidPublicKey() throws Exception {
        when(webPushService.getVapidPublicKey()).thenReturn("BEl62iUYgUivxIkv69yViEuiBIa-Ib9-SkvMeAtA3LFgDzkrxZJjSgSnfckjBJuBkr3qBUYIHBQFLXYp5NTH8-U");

        mockMvc.perform(get("/v1/notifications/push/public-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.publicKey").value("BEl62iUYgUivxIkv69yViEuiBIa-Ib9-SkvMeAtA3LFgDzkrxZJjSgSnfckjBJuBkr3qBUYIHBQFLXYp5NTH8-U"));
    }

    @Test
    @DisplayName("Should register device push subscription")
    void shouldRegisterSubscription() throws Exception {
        PushSubscriptionRequest req = new PushSubscriptionRequest(
                "https://fcm.googleapis.com/fcm/send/sample-token",
                "sample-p256dh-key",
                "sample-auth-key",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 16_4 like Mac OS X)"
        );

        PushSubscription saved = PushSubscription.builder()
                .id("sub-1")
                .userId("user-123")
                .endpoint("https://fcm.googleapis.com/fcm/send/sample-token")
                .p256dhKey("sample-p256dh-key")
                .authKey("sample-auth-key")
                .userAgent(req.userAgent())
                .active(true)
                .build();

        when(webPushService.registerSubscription(eq("user-123"), any(PushSubscriptionRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/v1/notifications/push/subscribe")
                        .principal(new UsernamePasswordAuthenticationToken("user-123", "password"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("sub-1"))
                .andExpect(jsonPath("$.data.endpoint").value("https://fcm.googleapis.com/fcm/send/sample-token"));
    }

    @Test
    @DisplayName("Should unsubscribe device from push notifications")
    void shouldUnsubscribe() throws Exception {
        doNothing().when(webPushService).removeSubscription("user-123", "https://fcm.googleapis.com/fcm/send/sample-token");

        mockMvc.perform(post("/v1/notifications/push/unsubscribe")
                        .principal(new UsernamePasswordAuthenticationToken("user-123", "password"))
                        .param("endpoint", "https://fcm.googleapis.com/fcm/send/sample-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(webPushService, times(1)).removeSubscription("user-123", "https://fcm.googleapis.com/fcm/send/sample-token");
    }

    @Test
    @DisplayName("Should send test push notification")
    void shouldSendTestPush() throws Exception {
        doNothing().when(webPushService).sendPushToUser(eq("user-123"), any());

        mockMvc.perform(post("/v1/notifications/push/test")
                        .principal(new UsernamePasswordAuthenticationToken("user-123", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(webPushService, times(1)).sendPushToUser(eq("user-123"), any());
    }
}
