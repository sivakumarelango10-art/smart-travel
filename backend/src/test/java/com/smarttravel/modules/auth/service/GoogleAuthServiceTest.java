package com.smarttravel.modules.auth.service;

import com.smarttravel.common.exception.ForbiddenException;
import com.smarttravel.common.security.JwtTokenProvider;
import com.smarttravel.modules.auth.dto.AuthResponse;
import com.smarttravel.modules.auth.dto.GoogleLoginRequest;
import com.smarttravel.modules.auth.dto.GoogleTokenPayload;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.AuthProvider;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private GoogleTokenVerifier googleTokenVerifier;

    @InjectMocks
    private AuthServiceImpl authService;

    private static final String GOOGLE_SUB = "google-user-123456789";
    private static final String GOOGLE_EMAIL = "rahul.traveler@gmail.com";

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("7. Existing Google user login succeeds when matching googleSubject exists")
    void testExistingGoogleUserLogin() {
        GoogleTokenPayload payload = new GoogleTokenPayload(
                GOOGLE_SUB,
                GOOGLE_EMAIL,
                true,
                "Rahul Traveler",
                "Rahul",
                "Traveler",
                "https://photos.google.com/avatar.jpg"
        );
        when(googleTokenVerifier.verify("valid-google-cred")).thenReturn(payload);

        User existingUser = User.builder()
                .id("usr_existing_123")
                .email(GOOGLE_EMAIL)
                .normalizedEmail(GOOGLE_EMAIL.toLowerCase())
                .fullName("Rahul Traveler")
                .googleSubject(GOOGLE_SUB)
                .authProvider(AuthProvider.GOOGLE)
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .build();

        when(userRepository.findByGoogleSubject(GOOGLE_SUB)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(jwtTokenProvider.generateTokenFromUserIdAndEmail(eq("usr_existing_123"), eq(GOOGLE_EMAIL), anyList(), anyBoolean()))
                .thenReturn("mock-jwt-access-token");
        when(jwtTokenProvider.getJwtExpirationMs(anyBoolean())).thenReturn(86400000L);

        GoogleLoginRequest req = new GoogleLoginRequest("valid-google-cred", true);
        AuthResponse response = authService.authenticateWithGoogle(req);

        assertNotNull(response);
        assertEquals("mock-jwt-access-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("usr_existing_123", response.getUser().getId());
        assertEquals(GOOGLE_EMAIL, response.getUser().getEmail());
    }

    @Test
    @DisplayName("8. New Google user registration automatically creates user with ROLE_USER, preferences and AuthProvider.GOOGLE")
    void testNewGoogleUserRegistration() {
        GoogleTokenPayload payload = new GoogleTokenPayload(
                "new-google-sub-999",
                "new.traveler@gmail.com",
                true,
                "New Traveler",
                "New",
                "Traveler",
                "https://photos.google.com/new_avatar.jpg"
        );
        when(googleTokenVerifier.verify("new-cred")).thenReturn(payload);
        when(userRepository.findByGoogleSubject("new-google-sub-999")).thenReturn(Optional.empty());
        when(userRepository.findByNormalizedEmail("new.traveler@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new.traveler@gmail.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$secureRandomPasswordHash");

        User savedNewUser = User.builder()
                .id("usr_new_999")
                .email("new.traveler@gmail.com")
                .normalizedEmail("new.traveler@gmail.com")
                .fullName("New Traveler")
                .googleSubject("new-google-sub-999")
                .authProvider(AuthProvider.GOOGLE)
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedNewUser);
        when(jwtTokenProvider.generateTokenFromUserIdAndEmail(eq("usr_new_999"), eq("new.traveler@gmail.com"), anyList(), anyBoolean()))
                .thenReturn("new-user-jwt-token");

        GoogleLoginRequest req = new GoogleLoginRequest("new-cred", true);
        AuthResponse response = authService.authenticateWithGoogle(req);

        assertNotNull(response);
        assertEquals("new-user-jwt-token", response.getAccessToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User created = userCaptor.getValue();
        assertEquals("new.traveler@gmail.com", created.getEmail());
        assertEquals("new-google-sub-999", created.getGoogleSubject());
        assertEquals(AuthProvider.GOOGLE, created.getAuthProvider());
        assertTrue(created.isEmailVerified());
        assertNotNull(created.getPreferences());
    }

    @Test
    @DisplayName("9. Duplicate Prevention: Subsequent logins for existing Google account do not create duplicate records")
    void testDuplicatePrevention() {
        GoogleTokenPayload payload = new GoogleTokenPayload(
                GOOGLE_SUB,
                GOOGLE_EMAIL,
                true,
                "Rahul Traveler",
                "Rahul",
                "Traveler",
                null
        );
        when(googleTokenVerifier.verify("token-attempt")).thenReturn(payload);

        User existing = User.builder()
                .id("usr_1")
                .email(GOOGLE_EMAIL)
                .googleSubject(GOOGLE_SUB)
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .build();

        when(userRepository.findByGoogleSubject(GOOGLE_SUB)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);
        when(jwtTokenProvider.generateTokenFromUserIdAndEmail(anyString(), anyString(), anyList(), anyBoolean()))
                .thenReturn("token-1");

        authService.authenticateWithGoogle(new GoogleLoginRequest("token-attempt", true));
        authService.authenticateWithGoogle(new GoogleLoginRequest("token-attempt", true));

        // Verifies user is updated (lastLoginAt updated), no duplicate user is created
        verify(userRepository, times(2)).findByGoogleSubject(GOOGLE_SUB);
    }

    @Test
    @DisplayName("10. Existing account handling: Safely links Google Subject when user already registered via email/password")
    void testExistingAccountLinking() {
        GoogleTokenPayload payload = new GoogleTokenPayload(
                "linked-google-sub-456",
                "existing.local@smarttravel.com",
                true,
                "Local Traveler",
                "Local",
                "Traveler",
                "https://photos.google.com/pic.jpg"
        );
        when(googleTokenVerifier.verify("linking-token")).thenReturn(payload);

        User localUser = User.builder()
                .id("usr_local_456")
                .email("existing.local@smarttravel.com")
                .normalizedEmail("existing.local@smarttravel.com")
                .passwordHash("$2a$12$existingPasswordHash")
                .fullName("Local Traveler")
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .build();

        when(userRepository.findByGoogleSubject("linked-google-sub-456")).thenReturn(Optional.empty());
        when(userRepository.findByNormalizedEmail("existing.local@smarttravel.com")).thenReturn(Optional.of(localUser));
        when(userRepository.save(any(User.class))).thenReturn(localUser);
        when(jwtTokenProvider.generateTokenFromUserIdAndEmail(eq("usr_local_456"), eq("existing.local@smarttravel.com"), anyList(), anyBoolean()))
                .thenReturn("linked-jwt-token");

        AuthResponse response = authService.authenticateWithGoogle(new GoogleLoginRequest("linking-token", true));

        assertNotNull(response);
        assertEquals("linked-jwt-token", response.getAccessToken());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User updated = captor.getValue();
        assertEquals("linked-google-sub-456", updated.getGoogleSubject());
        assertEquals("https://photos.google.com/pic.jpg", updated.getAvatarUrl());
    }

    @Test
    @DisplayName("11. Suspended or deleted user attempting Google login throws ForbiddenException")
    void testSuspendedUserGoogleLogin() {
        GoogleTokenPayload payload = new GoogleTokenPayload(
                GOOGLE_SUB,
                "suspended@smarttravel.com",
                true,
                "Suspended User",
                "Suspended",
                "User",
                null
        );
        when(googleTokenVerifier.verify("suspended-token")).thenReturn(payload);

        User suspendedUser = User.builder()
                .id("usr_suspended")
                .email("suspended@smarttravel.com")
                .googleSubject(GOOGLE_SUB)
                .accountStatus(AccountStatus.SUSPENDED)
                .active(false)
                .build();

        when(userRepository.findByGoogleSubject(GOOGLE_SUB)).thenReturn(Optional.of(suspendedUser));

        assertThrows(ForbiddenException.class, () ->
                authService.authenticateWithGoogle(new GoogleLoginRequest("suspended-token", true))
        );
    }
}
