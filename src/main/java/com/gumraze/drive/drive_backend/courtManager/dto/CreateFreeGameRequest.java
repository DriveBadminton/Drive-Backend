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
    private String title;

    @Enumerated(EnumType.STRING)
    private MatchRecordMode matchRecordMode;

    private Integer courtCount;
    private Integer roundCount;

    private List<Long> managerIds;
    private List<ParticipantCreateRequest> participants;





}
