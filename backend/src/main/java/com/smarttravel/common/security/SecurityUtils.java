package com.smarttravel.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Utility helpers to access authenticated user context.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.of(principal);
        }
        return Optional.empty();
    }

    public static Optional<String> getCurrentUserId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getId);
    }

    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUserPrincipal().map(UserPrincipal::getEmail);
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()));
    }
}
