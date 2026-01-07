package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantCreateRequest {
    private Long userId;            // 우리 서비스의 사용자인 경우에만 수집

    @NotBlank
    private String originalName;    // 참가자 원본 이름

    @NotNull
    private Gender gender;          // 참가자 성별

    @NotNull
    private Grade grade;            // 참가자 급수

    @NotNull
    @Min(10)
    @Max(70)
    private Integer ageGroup;       // 연령대
}
