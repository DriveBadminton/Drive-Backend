package com.gumraze.drive.drive_backend.auth.token;

import java.util.UUID;

public class AccessTokenGenerator implements TokenProvider{
    @Override
    public String generateAccessToken(Long userId) {
        return "user-" + userId + "-" + UUID.randomUUID();
    }
}
