package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record AccountStatusResponseDto(
        @Schema(description = "사용자 ID", example = "1")
        Long userId,
        @Schema(description = "계정 상태", example = "ACTIVE")
        UserStatus status,
        @Schema(description = "프로필 생성 여부", example = "true")
        boolean hasProfile
) { }
