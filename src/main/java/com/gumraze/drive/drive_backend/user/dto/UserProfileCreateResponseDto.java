package com.gumraze.drive.drive_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileCreateResponseDto(
        @Schema(description = "사용자 ID", example = "123")
        Long userId
) {
}
