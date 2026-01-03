package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantCreateRequest {
    private Long userId; // 우리 서비스의 사용자인 경우에만 수집

    @NotBlank
    private String name;            // 참가자 이름

    @NotNull
    private Gender gender;          // 참가자 성별

    @NotNull
    private Grade grade;            // 참가자 급수

    private Integer ageGroup;       // (선택) 연령대
}
