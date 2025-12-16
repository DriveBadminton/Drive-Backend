package com.gumraze.drive.drive_backend.auth.dto;

public record OAuthRefreshTokenResponseDto(
        Long userId,
        String accessToken
) { }
