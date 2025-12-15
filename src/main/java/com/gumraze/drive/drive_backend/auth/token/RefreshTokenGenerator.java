package com.gumraze.drive.drive_backend.auth.token;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.UUID;

@Component
public class RefreshTokenGenerator {

    public String generatePlainToken() {
        return UUID.randomUUID().toString();
    }

    public String hash(String plainToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(plainToken.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash refresh token", ex);
        }
    }
}
