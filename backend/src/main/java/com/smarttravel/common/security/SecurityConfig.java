package com.smarttravel.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarttravel.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 6 Configuration.
 * Configures stateless JWT filter chain, RequestIdFilter correlation, RBAC method security,
 * production security headers, CORS, and endpoint permissions.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final RequestIdFilter requestIdFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,https://smart-travel-sage.vercel.app}")
    private String allowedOrigins;

    public SecurityConfig(RequestIdFilter requestIdFilter,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          ObjectMapper objectMapper) {
        this.requestIdFilter = requestIdFilter;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(contentTypeOptions -> {})
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            String requestId = MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY);
                            ErrorResponse errorResponse = ErrorResponse.builder()
                                     .timestamp(Instant.now())
                                     .status(HttpStatus.UNAUTHORIZED.value())
                                     .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                                     .message("Full authentication is required to access this resource")
                                     .path(request.getRequestURI())
                                     .requestId(requestId)
                                     .build();

                            response.getOutputStream().println(objectMapper.writeValueAsString(errorResponse));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                            String requestId = MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY);
                            ErrorResponse errorResponse = ErrorResponse.builder()
                                     .timestamp(Instant.now())
                                     .status(HttpStatus.FORBIDDEN.value())
                                     .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                                     .message("Access denied: You do not have permission to access this resource")
                                     .path(request.getRequestURI())
                                     .requestId(requestId)
                                     .build();

                            response.getOutputStream().println(objectMapper.writeValueAsString(errorResponse));
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // Global CORS Preflight Handling (OPTIONS requests must NEVER require authentication)
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // Public Health & Actuator Probes
                        .requestMatchers("/api/health", "/api/v1/health", "/v1/health", "/health", "/api/health/**", "/api/v1/health/**", "/v1/health/**", "/health/**", "/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        // Public Swagger / OpenAPI documentation
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        // Public Authentication endpoints (register, login, refresh, forgot/reset password)
                        .requestMatchers(
                                "/api/auth/register", "/api/v1/auth/register", "/v1/auth/register", "/auth/register",
                                "/api/v1/v1/auth/register", "/v1/v1/auth/register",
                                "/api/auth/register/**", "/api/v1/auth/register/**", "/v1/auth/register/**", "/auth/register/**",
                                "/api/v1/v1/auth/register/**", "/v1/v1/auth/register/**",
                                "/api/auth/login", "/api/v1/auth/login", "/v1/auth/login", "/auth/login",
                                "/api/v1/v1/auth/login", "/v1/v1/auth/login",
                                "/api/auth/login/**", "/api/v1/auth/login/**", "/v1/auth/login/**", "/auth/login/**",
                                "/api/v1/v1/auth/login/**", "/v1/v1/auth/login/**",
                                "/api/auth/google", "/api/v1/auth/google", "/v1/auth/google", "/auth/google",
                                "/api/v1/v1/auth/google", "/v1/v1/auth/google",
                                "/api/auth/google/**", "/api/v1/auth/google/**", "/v1/auth/google/**", "/auth/google/**",
                                "/api/v1/v1/auth/google/**", "/v1/v1/auth/google/**",
                                "/api/auth/refresh", "/api/v1/auth/refresh", "/v1/auth/refresh", "/auth/refresh",
                                "/api/v1/v1/auth/refresh", "/v1/v1/auth/refresh",
                                "/api/auth/refresh/**", "/api/v1/auth/refresh/**", "/v1/auth/refresh/**", "/auth/refresh/**",
                                "/api/v1/v1/auth/refresh/**", "/v1/v1/auth/refresh/**",
                                "/api/auth/refresh-token", "/api/v1/auth/refresh-token", "/v1/auth/refresh-token", "/auth/refresh-token",
                                "/api/v1/v1/auth/refresh-token", "/v1/v1/auth/refresh-token",
                                "/api/auth/refresh-token/**", "/api/v1/auth/refresh-token/**", "/v1/auth/refresh-token/**", "/auth/refresh-token/**",
                                "/api/v1/v1/auth/refresh-token/**", "/v1/v1/auth/refresh-token/**",
                                "/api/auth/forgot-password", "/api/v1/auth/forgot-password", "/v1/auth/forgot-password", "/auth/forgot-password",
                                "/api/auth/forgot-password/**", "/api/v1/auth/forgot-password/**", "/v1/auth/forgot-password/**", "/auth/forgot-password/**",
                                "/api/auth/reset-password", "/api/v1/auth/reset-password", "/v1/auth/reset-password", "/auth/reset-password",
                                "/api/auth/reset-password/**", "/api/v1/auth/reset-password/**", "/v1/auth/reset-password/**", "/auth/reset-password/**"
                        ).permitAll()
                        // Public Flight Catalog & Search (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/api/v1/flights/**", "/v1/flights/**", "/api/flights/**", "/flights/**"
                        ).permitAll()
                        // Public Hotel Search & Catalog (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/v1/hotels/**", "/api/v1/hotels/**"
                        ).permitAll()
                        // Public Reviews (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/v1/reviews/**", "/api/v1/reviews/**"
                        ).permitAll()
                        // Public Recommendations (GET endpoints)
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/v1/recommendations/**", "/api/v1/recommendations/**"
                        ).permitAll()
                        // Public Pricing (GET endpoints)
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/v1/pricing/**", "/api/v1/pricing/**"
                        ).permitAll()
                        // Public Boarding Pass Scanner Verification (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/v1/boarding-passes/verify", "/api/v1/boarding-passes/verify"
                        ).permitAll()
                        // Public Web Push public key
                        .requestMatchers(org.springframework.http.HttpMethod.GET,
                                "/v1/notifications/push/public-key", "/api/v1/notifications/push/public-key"
                        ).permitAll()
                        // Razorpay Webhook Callback (Server-to-Server authenticated by HMAC-SHA256 signature)
                        .requestMatchers(org.springframework.http.HttpMethod.POST,
                                "/api/v1/payments/webhook", "/v1/payments/webhook", "/api/payments/webhook", "/payments/webhook"
                        ).permitAll()
                        // Admin Endpoints
                        .requestMatchers("/api/admin/**", "/api/v1/admin/**", "/v1/admin/**", "/admin/**").hasRole("ADMIN")
                        // WebSocket Handshake Endpoint
                        .requestMatchers("/ws/**").permitAll()
                        // Error Dispatch
                        .requestMatchers("/error").permitAll()
                        // All other endpoints require authentication (including /api/auth/me)
                        .anyRequest().authenticated()
                )
                .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        configuration.setAllowedOrigins(origins);
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://*.vercel.app",
                "https://smart-travel-sage.vercel.app"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "X-Requested-With", "Accept",
                "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers",
                "X-Request-ID", "X-Correlation-ID"
        ));
        configuration.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count", "X-Request-ID", "X-Correlation-ID", "X-Response-Time", "Server-Timing"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
