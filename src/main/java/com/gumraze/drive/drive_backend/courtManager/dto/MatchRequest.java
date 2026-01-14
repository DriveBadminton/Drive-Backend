package com.gumraze.drive.drive_backend.courtManager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MatchRequest {
    @NotNull @Min(1)
    private Integer courtNumber;
    @NotNull @Size(min = 2, max = 2)
    private List<Long> teamAIds;
    @NotNull @Size(min = 2, max = 2)
    private List<Long> teamBIds;
}
