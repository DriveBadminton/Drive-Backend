package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchWinnerTeam;
import jakarta.validation.constraints.Min;

public record CompleteFreeGameMatchRequest(
        MatchWinnerTeam winnerTeam,

        @Min(0)
        Integer teamAScore,

        @Min(0)
        Integer teamBScore
) {
}
