package com.smarttravel.modules.auth.service;

import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.LoginRequest;
import com.smarttravel.modules.auth.dto.RegisterRequest;
import com.smarttravel.modules.auth.dto.UserResponse;

/**
 * Service interface for User Authentication, Registration, and Profile resolution.
 */
public interface AuthService {

    /**
     * Registers a new user account with default ROLE_USER and ACTIVE status.
     *
     * @param request Registration payload containing user information and password
     * @return UserResponse representing the created user profile (without credentials)
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates a user with email and password and generates a signed JWT token.
     *
     * @param request Login credentials
     * @return AuthResponse containing the access token and user summary
     */
    AuthResponse login(LoginRequest request);

    /**
     * Retrieves the profile of the currently authenticated user from SecurityContext.
     *
     * @return UserResponse with complete profile details
     */
    UserResponse getCurrentUser();
}
