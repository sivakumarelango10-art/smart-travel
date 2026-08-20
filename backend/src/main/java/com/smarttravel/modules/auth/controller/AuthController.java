package com.smarttravel.modules.auth.controller;

import com.smarttravel.common.response.ApiResponse;
import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.LoginRequest;
import com.smarttravel.modules.auth.dto.RegisterRequest;
import com.smarttravel.modules.auth.dto.UserResponse;
import com.smarttravel.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.smarttravel.modules.auth.dto.ChangePasswordRequest;
import com.smarttravel.modules.auth.dto.DeleteAccountRequest;
import com.smarttravel.modules.auth.dto.UpdateProfileRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for User Authentication, Registration, Profile Management,
 * Password Change, and Account Deletion.
 */
@RestController
@RequestMapping({"/api/auth", "/api/v1/auth", "/v1/auth", "/auth", "/api/v1/v1/auth", "/v1/v1/auth"})
@Tag(name = "Authentication & Account", description = "User Registration, Login, Profile & Account Lifecycle Management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"/register", "/register/"})
    @Operation(
            summary = "Register New User Account",
            description = "Creates a new user profile with strongly hashed credentials, normalized email, and default ROLE_USER."
    )
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping({"/login", "/login/"})
    @Operation(
            summary = "User Login & JWT Token Issuance",
            description = "Authenticates user credentials, validates account status, and generates a signed JWT Access Token with optional Remember Me session extension."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("User authenticated successfully", response));
    }

    @PostMapping({"/refresh", "/refresh/", "/refresh-token", "/refresh-token/"})
    @Operation(
            summary = "Refresh JWT Access Token",
            description = "Validates existing refresh token and returns refreshed access credentials."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken() {
        // Returns current user session or authenticated token refreshed
        UserResponse user = authService.getCurrentUser();
        AuthResponse response = AuthResponse.builder()
                .tokenType("Bearer")
                .user(com.smarttravel.modules.auth.dto.UserSummaryDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .roles(user.getRoles())
                        .build())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @GetMapping({"/me", "/me/", "/profile", "/profile/"})
    @Operation(
            summary = "Get Authenticated User Profile",
            description = "Retrieves the current user's profile and preferences using the authenticated JWT Bearer token context.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", response));
    }

    @PutMapping({"/me", "/me/", "/profile", "/profile/"})
    @Operation(
            summary = "Update User Profile & Travel Preferences",
            description = "Updates editable profile fields (name, phone, travel & address preferences) for the authenticated caller.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = authService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PutMapping({"/password", "/password/", "/change-password", "/change-password/"})
    @Operation(
            summary = "Change Password",
            description = "Verifies the current password and securely hashes and updates the new password.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping({"/me", "/me/", "/profile", "/profile/", "/account", "/account/"})
    @Operation(
            summary = "Delete User Account",
            description = "Permanently deactivates and soft-deletes the authenticated user's account, anonymizing personal data.",
            security = @SecurityRequirement(name = "BearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@RequestBody(required = false) DeleteAccountRequest request) {
        authService.deleteAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }
}
