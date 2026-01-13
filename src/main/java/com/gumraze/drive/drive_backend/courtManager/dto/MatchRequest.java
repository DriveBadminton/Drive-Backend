package com.gumraze.drive.drive_backend.courtManager.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MatchRequest {
    private Integer courtNumber;
    private List<Long> teamAIds;
    private List<Long> teamBIds;
}
