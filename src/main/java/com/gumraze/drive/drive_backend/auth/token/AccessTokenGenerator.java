package com.gumraze.drive.drive_backend.auth.token;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccessTokenGenerator implements TokenProvider{
    @Override
    public String generateAccessToken(Long userId) {
        return "user-" + userId + "-" + UUID.randomUUID();
    }
}
