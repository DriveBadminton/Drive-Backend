package com.gumraze.drive.drive_backend.auth.token;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt.access-token")
public record JwtProperties(
        String secret,
        long expirationMs
) { }