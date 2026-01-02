package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ParticipantCreateRequest {
    private Long userId; // 우리 서비스의 사용자인 경우에만 수집

    @NotBlank
    private String name;

    @NotNull
    private Gender gender;

    @NotNull
    private Grade grade;

    private Integer ageGroup; // (선택) 연령대
}
