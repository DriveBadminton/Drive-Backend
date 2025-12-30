package com.gumraze.drive.drive_backend.auth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        AccessToken accessToken,
        RefreshToken refreshToken
) {
    public record AccessToken(String secret, long expirationMs) {}
    public record RefreshToken(long expirationHours) {}
}