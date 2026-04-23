package com.gumraze.rallyon.backend.courtManager.dto;

import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;

import java.util.List;
import java.util.UUID;

public record FreeGameMatchResponse(
        Long courtNumber,
        List<UUID> teamAIds,
        List<UUID> teamBIds,
        MatchStatus matchStatus,
        MatchResult matchResult,
        Integer teamAScore,
        Integer teamBScore,
        Boolean isActive
) {}
