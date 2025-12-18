package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.Grade;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileCreateRequest(
        @Schema(description = "표시할 닉네임", example = "kim")
        String nickname,
        @Schema(description = "지역 ID", example = "1")
        Long regionId,
        @Schema(description = "실력 등급(영문 또는 표시명)", example = "초심")
        Grade grade
) {
}
