package com.smarttravel.common.exception;

import com.smarttravel.common.security.CustomUserDetailsService;
import com.smarttravel.common.security.JwtAuthenticationFilter;
import com.smarttravel.common.security.JwtTokenProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.TestSampleController.class, excludeAutoConfiguration = { UserDetailsServiceAutoConfiguration.class })
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestSampleController.class})
class GlobalExceptionHandlerTest {

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

    @RestController
    public static class TestSampleController {

        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Flight", "id", "ST-999");
        }

        @GetMapping("/test/bad-request")
        public void throwBadRequest() {
            throw new BadRequestException("Invalid booking date sequence");
        }

        @GetMapping("/test/conflict")
        public void throwConflict() {
            throw new DuplicateResourceException("User", "email", "john@example.com");
        }

        @GetMapping("/test/forbidden")
        public void throwForbidden() {
            throw new ForbiddenException("Cannot access another traveler's booking");
        }

        @PostMapping("/test/validation")
        public void testValidation(@Valid @RequestBody DummyValidationDto dto) {
        }
    }

    public static class DummyValidationDto {
        @NotBlank(message = "Field 'name' is mandatory")
        private String name;

        public DummyValidationDto() {
        }

        public DummyValidationDto(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    @DisplayName("ResourceNotFoundException should return 404 with structured error")
    void testHandleResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Flight not found with id: 'ST-999'"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("BadRequestException should return 400 with structured error")
    void testHandleBadRequest() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid booking date sequence"));
    }

    @Test
    @DisplayName("DuplicateResourceException should return 409 Conflict")
    void testHandleConflict() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User already exists with email: 'john@example.com'"));
    }

    @Test
    @DisplayName("ForbiddenException should return 403 Forbidden")
    void testHandleForbidden() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Cannot access another traveler's booking"));
    }

    @Test
    @DisplayName("Validation failure should return 400 with field errors")
    void testValidationFailure() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request payload validation failed"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("name"))
                .andExpect(jsonPath("$.validationErrors[0].message").value("Field 'name' is mandatory"));
    }
}
