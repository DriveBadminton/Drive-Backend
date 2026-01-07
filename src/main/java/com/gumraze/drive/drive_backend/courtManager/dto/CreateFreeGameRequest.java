package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CreateFreeGameRequest {
    @NotBlank
    private String title;                                   // 게임 제목

    @Enumerated(EnumType.STRING)
    private MatchRecordMode matchRecordMode;                // 매치 기록 모드: RESULT/STATUS_ONLY

    @NotNull
    private GradeType gradeType;                            // 게임 참가자들의 급수 형식

    @NotNull @Min(1)
    private Integer courtCount;                             // 코트 수

    @NotNull @Min(1)
    private Integer roundCount;                             // 라운드 수

    @Size(max = 2)
    private List<Long> managerIds;                          // 게임 공동 운영자(최대 2명, null 허용)

    @Valid
    private List<ParticipantCreateRequest> participants;    // 게임 참가자(null 허용)
}