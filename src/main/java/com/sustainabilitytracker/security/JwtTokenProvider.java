package com.sustainabilitytracker.security;


import com.sustainabilitytracker.config.JwtProperties;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.exceptions.BadRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        return generateToken(user, jwtProperties.getAccessTokenExpiration());
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, jwtProperties.getRefreshTokenExpiration());
    }

    private String generateToken(User user, long tokenExpiration) {
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("name", user.getFullName())
                .claim("role", user.getRole())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * tokenExpiration))
                .signWith(jwtProperties.getSecretKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            var claims = getClaims(token);
            return !claims.getExpiration().before(new Date());   // ← Fixed
        } catch (Exception e) {
            return false;
        }
    }

    public Long extractUserIdFromToken(String token) {
        String subject = getClaims(token).getSubject();
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid user ID in token");
        }
    }

    public String extractRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String extractEmailFromToken(String token) {
        return getClaims(token).get("email", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtProperties.getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}