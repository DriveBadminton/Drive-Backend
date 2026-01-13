package com.gumraze.drive.drive_backend.courtManager.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoundRequest {
    private Integer roundNumber;
    private List<MatchRequest> matches;
}
