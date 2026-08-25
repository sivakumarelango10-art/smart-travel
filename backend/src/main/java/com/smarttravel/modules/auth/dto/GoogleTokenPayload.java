package com.smarttravel.modules.auth.dto;

/**
 * Verified identity claims extracted from a valid Google ID Token.
 */
public record GoogleTokenPayload(
        String subject,
        String email,
        boolean emailVerified,
        String name,
        String firstName,
        String lastName,
        String pictureUrl
) {}
