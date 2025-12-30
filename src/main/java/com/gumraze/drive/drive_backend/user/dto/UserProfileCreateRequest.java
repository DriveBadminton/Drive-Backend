package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import io.swagger.v3.oas.annotations.media.Schema;

public record UserProfileCreateRequest(
        @Schema(description = "표시할 닉네임", example = "kim")
        String nickname,
        @Schema(description = "시/군/구 ID", example = "1")
        Long districtId,
        @Schema(description = "지역 급수", example = "초심")
        Grade regionalGrade,
        @Schema(description = "전국 급수", example = "초심")
        Grade nationalGrade,
        @Schema(description = "생년월일", example = "19900101")
        String birth,
        @Schema(description = "성별", example = "MALE")
        Gender gender
) {
}
