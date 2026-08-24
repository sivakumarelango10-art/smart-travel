package com.smarttravel.modules.booking.requirement4;

import com.smarttravel.common.security.SecurityUtils;
import com.smarttravel.modules.auth.service.AuthServiceImpl;
import com.smarttravel.modules.user.model.AccountStatus;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;
import com.smarttravel.modules.user.model.UserPreferences;
import com.smarttravel.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Requirement #4 — User Travel Preferences (Seat & Room) Persistence Audit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Requirement #4: User Preferences Persistence & Security Audit")
class PreferencePersistenceAuditTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;
    private MockedStatic<SecurityUtils> securityUtilsMock;
    private User testUser;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, null);
        securityUtilsMock = mockStatic(SecurityUtils.class);

        testUser = User.builder()
                .id("usr-pref-01")
                .email("traveler@smarttravel.com")
                .fullName("Jane Doe")
                .roles(Set.of(Role.ROLE_USER))
                .accountStatus(AccountStatus.ACTIVE)
                .active(true)
                .preferences(new UserPreferences("WINDOW", "DELUXE", "ECONOMY", "BOM", "VEGETARIAN", null, null, null, null, null, null, null, null, null))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
    }

    @Test
    @DisplayName("[PR-1] Retrieves saved seat (WINDOW) and room (DELUXE) preferences for authenticated user")
    void testGetTravelPreferences() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of("usr-pref-01"));
        when(userRepository.findById("usr-pref-01")).thenReturn(Optional.of(testUser));

        UserPreferences prefs = authService.getUserPreferences();

        assertThat(prefs).isNotNull();
        assertThat(prefs.getPreferredSeatType()).isEqualTo("WINDOW");
        assertThat(prefs.getPreferredRoomType()).isEqualTo("DELUXE");
        assertThat(prefs.getHomeAirport()).isEqualTo("BOM");
    }

    @Test
    @DisplayName("[PR-2] Updates and persists modified seat (AISLE) and room (SUITE) preferences to MongoDB")
    void testUpdateTravelPreferences() {
        securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(Optional.of("usr-pref-01"));
        when(userRepository.findById("usr-pref-01")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreferences newPrefs = new UserPreferences("AISLE", "SUITE", "BUSINESS", "DEL", "VEGAN", null, null, null, null, null, null, null, null, null);

        UserPreferences updated = authService.updateUserPreferences(newPrefs);

        assertThat(updated).isNotNull();
        assertThat(updated.getPreferredSeatType()).isEqualTo("AISLE");
        assertThat(updated.getPreferredRoomType()).isEqualTo("SUITE");
        assertThat(updated.getPreferredClass()).isEqualTo("BUSINESS");

        verify(userRepository).save(testUser);
    }
}
