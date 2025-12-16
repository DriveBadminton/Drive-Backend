package com.gumraze.drive.drive_backend.auth.service;

public class FakeRefreshTokenService implements RefreshTokenService{
    @Override
    public String rotate(Long userId) {
        return "fake-refresh-token" + userId;
    }
}
