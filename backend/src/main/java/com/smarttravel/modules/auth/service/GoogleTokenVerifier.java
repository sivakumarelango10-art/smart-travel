package com.smarttravel.modules.auth.service;

import com.smarttravel.modules.auth.dto.GoogleTokenPayload;

/**
 * Service contract for verifying Google ID Tokens server-side.
 */
public interface GoogleTokenVerifier {

    /**
     * Verifies the Google ID token and returns the parsed identity claims.
     *
     * @param idTokenString the raw Google credential / ID token string
     * @return verified GoogleTokenPayload
     * @throws com.smarttravel.common.exception.UnauthorizedException if verification fails
     */
    GoogleTokenPayload verify(String idTokenString);
}
