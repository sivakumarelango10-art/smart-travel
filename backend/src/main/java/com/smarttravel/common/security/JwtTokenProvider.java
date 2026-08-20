package com.smarttravel.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for issuing and validating HS512/HS256 tokens using JJWT.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${app.jwt.secret:dGhpcy1pcy1hLXNhbXBsZS01MTItYml0LXNlY3JldC1rZXktZm9yLXVzZS13aXRoLWpqd3Qtc21hcnR0cmF2ZWwtYXBwbGljYXRpb24tZGV2ZWxvcG1lbnQtdGVzdGluZw==}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs; // 24 hours default

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromUserIdAndEmail(
                userPrincipal.getId(),
                userPrincipal.getEmail(),
                userPrincipal.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()),
                false
        );
    }

    public String generateTokenFromUserIdAndEmail(String userId, String email, List<String> roles) {
        return generateTokenFromUserIdAndEmail(userId, email, roles, false);
    }

    public String generateTokenFromUserIdAndEmail(String userId, String email, List<String> roles, boolean rememberMe) {
        Date now = new Date();
        long expirationMs = getJwtExpirationMs(rememberMe);
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("roles", roles)
                .claim("rememberMe", rememberMe)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SecurityException | MalformedJwtException ex) {
            log.error("Invalid JWT signature or malformed token");
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty or invalid");
        }
        return false;
    }

    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public long getJwtExpirationMs(boolean rememberMe) {
        if (rememberMe) {
            // Extended session: 30 days
            return 30L * 24 * 60 * 60 * 1000L;
        }
        return jwtExpirationMs;
    }
}
