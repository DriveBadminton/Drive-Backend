package com.gumraze.drive.drive_backend.auth.service;

public record OAuthLoginResult(
        Long userId,
        String accessToken,
        String refreshToken
) { }
