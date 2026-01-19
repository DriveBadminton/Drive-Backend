package com.gumraze.drive.drive_backend.user.dto;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 프로필 생성 요청 DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileCreateRequest {
        @Schema(description = "닉네임", example = "SYAN")
        private String nickname;

        @Schema(description = "지역 ID", example = "1")
        private Long districtId;

        @Schema(description = "지역 급수", example = "ROOKIE")
        private Grade regionalGrade;

        @Schema(description = "전국 급수", example = "ROOKIE")
        private Grade nationalGrade;

        @Schema(description = "생년월일", example = "19990101")
        private String birth;

        @Schema(description = "성별", example = "MALE")
        private Gender gender;
}