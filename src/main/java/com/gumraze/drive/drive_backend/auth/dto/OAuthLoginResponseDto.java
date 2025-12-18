package com.gumraze.drive.drive_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record OAuthLoginResponseDto(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken
) {}
