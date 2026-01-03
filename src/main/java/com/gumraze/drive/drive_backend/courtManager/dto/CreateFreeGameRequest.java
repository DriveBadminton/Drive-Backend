package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class CreateFreeGameRequest {
    private String title;                                   // 게임 제목

    @Enumerated(EnumType.STRING)
    private MatchRecordMode matchRecordMode;                // 매치 기록 모드: RESULT/STATUS_ONLY

    private Integer courtCount;                             // 코트 수
    private Integer roundCount;                             // 라운드 수(null 허용)

    private List<Long> managerIds;                          // 게임 공동 운영자(최대 2명)
    
    private List<ParticipantCreateRequest> participants;    // 게임 참가자(null 허용)
}