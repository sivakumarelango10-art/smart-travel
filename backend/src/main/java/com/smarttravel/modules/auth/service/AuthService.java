package com.smarttravel.modules.auth.service;

import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.ChangePasswordRequest;
import com.smarttravel.modules.auth.dto.DeleteAccountRequest;
import com.smarttravel.modules.auth.dto.LoginRequest;
import com.smarttravel.modules.auth.dto.RegisterRequest;
import com.smarttravel.modules.auth.dto.UpdateProfileRequest;
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
     * Authenticates or registers a user via verified Google ID Token credential.
     *
     * @param request Google login payload containing verified Google ID Token
     * @return AuthResponse containing the SmartTravel JWT access token and user summary
     */
    AuthResponse authenticateWithGoogle(com.smarttravel.modules.auth.dto.GoogleLoginRequest request);

    /**
     * Retrieves the profile of the currently authenticated user from SecurityContext.
     *
     * @return UserResponse with complete profile details
     */
    UserResponse getCurrentUser();

    /**
     * Updates the authenticated user's profile and travel preferences.
     *
     * @param request Profile update payload
     * @return UserResponse with updated profile details
     */
    UserResponse updateProfile(UpdateProfileRequest request);

    /**
     * Changes the authenticated user's password after verifying current credentials.
     *
     * @param request Password change payload
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Permanently deactivates and deletes the authenticated user's account,
     * anonymizing PII and revoking access.
     *
     * @param request Optional deletion reason and verification
     */
    void deleteAccount(DeleteAccountRequest request);

    /**
     * Retrieves the travel preferences for the authenticated user.
     *
     * @return UserPreferences entity
     */
    com.smarttravel.modules.user.model.UserPreferences getUserPreferences();

    /**
     * Updates the travel preferences for the authenticated user.
     *
     * @param preferences Travel preferences payload
     * @return Updated UserPreferences entity
     */
    com.smarttravel.modules.user.model.UserPreferences updateUserPreferences(com.smarttravel.modules.user.model.UserPreferences preferences);
}
