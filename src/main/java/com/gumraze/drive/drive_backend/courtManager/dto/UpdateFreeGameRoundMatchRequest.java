package com.gumraze.drive.drive_backend.courtManager.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UpdateFreeGameRoundMatchRequest {
    @NotEmpty @Valid
    private List<RoundRequest> rounds;
}
