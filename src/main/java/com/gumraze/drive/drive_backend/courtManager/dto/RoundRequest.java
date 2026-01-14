package com.gumraze.drive.drive_backend.courtManager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoundRequest {
    @NotNull @Min(1)
    private Integer roundNumber;
    @NotNull @Valid
    private List<MatchRequest> matches;
}
