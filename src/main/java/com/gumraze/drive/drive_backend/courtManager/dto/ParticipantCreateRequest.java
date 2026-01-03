package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantCreateRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                // 참가자 ID, 각 참가자의 구분을 위한 Id

    private Long userId;            // 우리 서비스의 사용자인 경우에만 수집

    @NotNull
    private String name;            // 참가자 이름

    @NotNull
    private Gender gender;          // 참가자 성별

    @NotNull
    private Grade grade;            // 참가자 급수

    @NotNull
    private Integer ageGroup;       // 연령대
}
