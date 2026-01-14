package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.constants.MatchResult;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class FreeGameMatchResponse {
    Long courtNumber;
    List<Long> teamAIds;
    List<Long> teamBIds;
    MatchStatus matchStatus;
    MatchResult matchResult;
    Boolean isActive;
}
