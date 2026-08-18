package com.smarttravel.modules.auth.mapper;

import com.smarttravel.modules.auth.dto.UserResponse;
import com.smarttravel.modules.auth.dto.UserSummaryDto;
import com.smarttravel.modules.user.model.Role;
import com.smarttravel.modules.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility for transforming between User entity and API DTOs.
 */
public final class AuthMapper {

    private AuthMapper() {
    }

    public static UserSummaryDto toUserSummaryDto(User user) {
        if (user == null) {
            return null;
        }

        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::name).collect(Collectors.toList())
                : List.of();

        return UserSummaryDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roles)
                .accountStatus(user.getAccountStatus() != null ? user.getAccountStatus().name() : null)
                .build();
    }

    public static UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        List<String> roles = user.getRoles() != null
                ? user.getRoles().stream().map(Role::name).collect(Collectors.toList())
                : List.of();

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .roles(roles)
                .accountStatus(user.getAccountStatus())
                .emailVerified(user.isEmailVerified())
                .preferences(user.getPreferences())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
