package com.gumraze.drive.drive_backend.courtManager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FreeGameParticipantResponse {
    private Long participantId;
    private Long userId;
    private String displayName;
    private Gender gender;
    private Grade grade;
    private Integer ageGroup;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer assignedMatchCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer completedMatchCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer winCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer lossCount;
}
