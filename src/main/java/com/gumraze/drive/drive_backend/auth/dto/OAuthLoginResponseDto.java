package com.gumraze.drive.drive_backend.auth.dto;

public record OAuthLoginResponseDto(
        Long userId,
        String accessToken
) {}
