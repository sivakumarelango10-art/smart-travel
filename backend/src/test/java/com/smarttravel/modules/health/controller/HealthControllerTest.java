package com.smarttravel.modules.health.controller;

import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@WebMvcTest(controllers = HealthController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MongoTemplate mongoTemplate;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/health should return UP status and HTTP 200")
    void testGetHealthSuccess() throws Exception {
        Document pingDoc = new Document("ok", 1.0);
        when(mongoTemplate.executeCommand(any(Document.class))).thenReturn(pingDoc);

        mockMvc.perform(get("/api/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("SmartTravel Backend"))
                .andExpect(jsonPath("$.data.database").value("CONNECTED"))
                .andExpect(jsonPath("$.data.timestamp").exists());
    }

    @Test
    @DisplayName("GET /api/v1/health should also return UP status and HTTP 200")
    void testGetV1HealthSuccess() throws Exception {
        when(mongoTemplate.executeCommand(any(Document.class))).thenThrow(new RuntimeException("DB Connection timeout"));

        mockMvc.perform(get("/api/v1/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("SmartTravel Backend"))
                .andExpect(jsonPath("$.data.database").value("DISCONNECTED"));
    }
}
