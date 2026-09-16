package com.smarttravel.common.security;

import com.smarttravel.common.exception.UnauthorizedException;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.ofNullable(principal.getId());
        }
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }
        String name = authentication.getName();
        if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
            return Optional.of(name);
        }
        return Optional.empty();
    }

    public static String getRequiredCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new UnauthorizedException("Full authentication is required to perform this action"));
    }

    public static Optional<String> getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return Optional.ofNullable(principal.getEmail());
        }
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return Optional.ofNullable(userDetails.getUsername());
        }
        String name = authentication.getName();
        if (name != null && !name.isBlank() && !"anonymousUser".equals(name)) {
            return Optional.of(name);
        }
        return Optional.empty();
    }

    public static String getRequiredCurrentUserEmail() {
        return getCurrentUserEmail()
                .orElseThrow(() -> new UnauthorizedException("Full authentication is required to perform this action"));
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()));
    }

    public static String getCurrentUsernameOrAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "system";
        }
        if (authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getEmail();
        }
        String name = authentication.getName();
        return (name != null && !name.isBlank() && !"anonymousUser".equals(name)) ? name : "system";
    }
}
