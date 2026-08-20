package com.smarttravel.modules.auth.service;

import com.smarttravel.common.exception.DuplicateResourceException;
import com.smarttravel.common.exception.ForbiddenException;
import com.smarttravel.common.exception.UnauthorizedException;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.common.security.UserPrincipal;
import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.LoginRequest;
import com.smarttravel.modules.auth.dto.RegisterRequest;
import com.smarttravel.modules.auth.dto.UserResponse;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id("user-123")
                .fullName("Alice Wonderland")
                .email("alice@smarttravel.com")
                .normalizedEmail("alice@smarttravel.com")
                .phoneNumber("+919876543210")
                .passwordHash("$2a$12$hashedPasswordExample")
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("1. Successful user registration hashes password and defaults to ROLE_USER and ACTIVE status")
    void testSuccessfulRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Alice Wonderland")
                .email("Alice@SmartTravel.com")
                .phoneNumber("+919876543210")
                .password("Travel2026!Secure")
                .build();

        when(userRepository.existsByNormalizedEmail("alice@smarttravel.com")).thenReturn(false);
        when(userRepository.existsByEmail("Alice@SmartTravel.com")).thenReturn(false);
        when(passwordEncoder.encode("Travel2026!Secure")).thenReturn("$2a$12$hashedPasswordExample");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId("user-123");
            return u;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("user-123", response.getId());
        assertEquals("Alice Wonderland", response.getFullName());
        assertEquals("Alice@SmartTravel.com", response.getEmail());
        assertEquals(AccountStatus.ACTIVE, response.getAccountStatus());
        assertTrue(response.isEmailVerified());
        assertTrue(response.getRoles().contains("ROLE_USER"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertEquals("alice@smarttravel.com", saved.getNormalizedEmail());
        assertEquals("$2a$12$hashedPasswordExample", saved.getPasswordHash());
    }

    @Test
    @DisplayName("2. Duplicate email registration throws DuplicateResourceException")
    void testDuplicateEmailRegistration() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Alice Wonderland")
                .email("alice@smarttravel.com")
                .phoneNumber("+919876543210")
                .password("Travel2026!Secure")
                .build();

        when(userRepository.existsByNormalizedEmail("alice@smarttravel.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("3. Password is never stored in plain text and is properly encoded")
    void testPasswordIsHashed() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Bob Builder")
                .email("bob@smarttravel.com")
                .phoneNumber("+919876543211")
                .password("PlaintextSecret123!")
                .build();

        when(userRepository.existsByNormalizedEmail("bob@smarttravel.com")).thenReturn(false);
        when(passwordEncoder.encode("PlaintextSecret123!")).thenReturn("$2a$12$secureEncodedHash");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("$2a$12$secureEncodedHash", captor.getValue().getPasswordHash());
        assertFalse(captor.getValue().getPasswordHash().contains("PlaintextSecret123!"));
    }

    @Test
    @DisplayName("4. Successful login returns JWT access token and user summary")
    void testSuccessfulLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("ALICE@SmartTravel.com")
                .password("Travel2026!Secure")
                .rememberMe(false)
                .build();

        when(userRepository.findByNormalizedEmail("alice@smarttravel.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Travel2026!Secure", sampleUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.getJwtExpirationMs(false)).thenReturn(86400000L);
        when(jwtTokenProvider.generateTokenFromUserIdAndEmail(eq("user-123"), eq("alice@smarttravel.com"), any(), eq(false)))
                .thenReturn("mocked.jwt.token");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(86400000L, response.getExpiresIn());
        assertNotNull(response.getUser());
        assertEquals("user-123", response.getUser().getId());
        assertEquals("Alice Wonderland", response.getUser().getFullName());
        assertEquals("alice@smarttravel.com", response.getUser().getEmail());
        assertTrue(response.getUser().getRoles().contains("ROLE_USER"));
        assertNotNull(sampleUser.getLastLoginAt());
    }

    @Test
    @DisplayName("4b. Remember Me login returns 30-day extended JWT access token")
    void testRememberMeLogin() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@smarttravel.com")
                .password("Travel2026!Secure")
                .rememberMe(true)
                .build();

        when(userRepository.findByNormalizedEmail("alice@smarttravel.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Travel2026!Secure", sampleUser.getPasswordHash())).thenReturn(true);
        when(jwtTokenProvider.getJwtExpirationMs(true)).thenReturn(2592000000L);
        when(jwtTokenProvider.generateTokenFromUserIdAndEmail(eq("user-123"), eq("alice@smarttravel.com"), any(), eq(true)))
                .thenReturn("mocked.extended.jwt.token");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mocked.extended.jwt.token", response.getAccessToken());
        assertEquals(2592000000L, response.getExpiresIn());
    }

    @Test
    @DisplayName("5. Login with wrong password throws UnauthorizedException")
    void testLoginWithWrongPassword() {
        LoginRequest request = LoginRequest.builder()
                .email("alice@smarttravel.com")
                .password("WrongPassword123!")
                .build();

        when(userRepository.findByNormalizedEmail("alice@smarttravel.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("WrongPassword123!", sampleUser.getPasswordHash())).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
        verify(jwtTokenProvider, never()).generateTokenFromUserIdAndEmail(anyString(), anyString(), any(), anyBoolean());
    }

    @Test
    @DisplayName("6. Login with unknown email throws UnauthorizedException without revealing email absence")
    void testLoginWithUnknownEmail() {
        LoginRequest request = LoginRequest.builder()
                .email("unknown@smarttravel.com")
                .password("AnyPassword123!")
                .build();

        when(userRepository.findByNormalizedEmail("unknown@smarttravel.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("unknown@smarttravel.com")).thenReturn(Optional.empty());

        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("7. Inactive account login is rejected with ForbiddenException")
    void testLoginWithInactiveAccount() {
        sampleUser.setAccountStatus(AccountStatus.INACTIVE);
        sampleUser.setActive(false);

        LoginRequest request = LoginRequest.builder()
                .email("alice@smarttravel.com")
                .password("Travel2026!Secure")
                .build();

        when(userRepository.findByNormalizedEmail("alice@smarttravel.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Travel2026!Secure", sampleUser.getPasswordHash())).thenReturn(true);

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("inactive"));
    }

    @Test
    @DisplayName("8. Suspended account login is rejected with ForbiddenException")
    void testLoginWithSuspendedAccount() {
        sampleUser.setAccountStatus(AccountStatus.SUSPENDED);
        sampleUser.setActive(false);

        LoginRequest request = LoginRequest.builder()
                .email("alice@smarttravel.com")
                .password("Travel2026!Secure")
                .build();

        when(userRepository.findByNormalizedEmail("alice@smarttravel.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Travel2026!Secure", sampleUser.getPasswordHash())).thenReturn(true);

        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("suspended"));
    }

    @Test
    @DisplayName("9. Get current user resolves authenticated profile without passwordHash")
    void testGetCurrentUser() {
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById("user-123")).thenReturn(Optional.of(sampleUser));

        UserResponse profile = authService.getCurrentUser();

        assertNotNull(profile);
        assertEquals("user-123", profile.getId());
        assertEquals("Alice Wonderland", profile.getFullName());
        assertEquals("alice@smarttravel.com", profile.getEmail());
        assertEquals("+919876543210", profile.getPhoneNumber());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("10. Update profile saves updated preferences and name")
    void testUpdateProfile() {
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById("user-123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        com.smarttravel.modules.user.model.UserPreferences prefs = new com.smarttravel.modules.user.model.UserPreferences();
        prefs.setPreferredSeatType("WINDOW");
        prefs.setPreferredClass("BUSINESS");
        prefs.setCity("Mumbai");

        com.smarttravel.modules.auth.dto.UpdateProfileRequest updateReq = com.smarttravel.modules.auth.dto.UpdateProfileRequest.builder()
                .fullName("Alice In Wonderland")
                .phoneNumber("+91 99999 88888")
                .preferences(prefs)
                .build();

        UserResponse updated = authService.updateProfile(updateReq);

        assertNotNull(updated);
        assertEquals("Alice In Wonderland", updated.getFullName());
        assertEquals("+91 99999 88888", updated.getPhoneNumber());
        assertEquals("WINDOW", updated.getPreferences().getPreferredSeatType());
        assertEquals("BUSINESS", updated.getPreferences().getPreferredClass());
        assertEquals("Mumbai", updated.getPreferences().getCity());

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("11. Change password verifies old password and updates password hash")
    void testChangePassword() {
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById("user-123")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("Travel2026!Old", sampleUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("Travel2026!NewSecure")).thenReturn("$2a$12$newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        com.smarttravel.modules.auth.dto.ChangePasswordRequest changeReq = com.smarttravel.modules.auth.dto.ChangePasswordRequest.builder()
                .currentPassword("Travel2026!Old")
                .newPassword("Travel2026!NewSecure")
                .confirmPassword("Travel2026!NewSecure")
                .build();

        authService.changePassword(changeReq);

        assertEquals("$2a$12$newHashedPassword", sampleUser.getPasswordHash());
        verify(userRepository).save(sampleUser);

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("12. Delete account deactivates user and anonymizes personal data")
    void testDeleteAccount() {
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById("user-123")).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        com.smarttravel.modules.auth.dto.DeleteAccountRequest deleteReq = new com.smarttravel.modules.auth.dto.DeleteAccountRequest(null, "No longer needed");
        authService.deleteAccount(deleteReq);

        assertEquals(AccountStatus.DELETED, sampleUser.getAccountStatus());
        assertFalse(sampleUser.isActive());
        assertEquals("Deleted User", sampleUser.getFullName());
        assertNull(sampleUser.getPhoneNumber());

        SecurityContextHolder.clearContext();
    }
}
