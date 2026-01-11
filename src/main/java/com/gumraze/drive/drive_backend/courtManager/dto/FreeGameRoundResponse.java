package com.gumraze.drive.drive_backend.courtManager.dto;

import com.gumraze.drive.drive_backend.courtManager.constants.RoundStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class FreeGameRoundResponse {
    Long roundNumber;
    RoundStatus roundStatus;
    List<FreeGameMatchResponse> matches;
}
