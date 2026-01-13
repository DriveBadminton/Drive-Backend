package com.gumraze.drive.drive_backend.courtManager.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpdateFreeGameRoundMatchRequest {
    private List<RoundRequest> rounds;
}
