package com.gumraze.drive.drive_backend.auth.token;

import java.util.UUID;

public class AccessTokenGenerator implements TokenProvider{
    @Override
    public String generateAccessToken() {
        return UUID.randomUUID().toString();
    }
}
