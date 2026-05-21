package com.gumraze.rallyon.backend.courtManager.application.port.in.command;

import com.gumraze.rallyon.backend.courtManager.constants.MatchWinnerTeam;

import java.util.UUID;

public record CompleteFreeGameMatchCommand(
        UUID organizerId,
        UUID gameId,
        Integer roundNumber,
        Integer courtNumber,
        MatchWinnerTeam winnerTeam,
        Integer teamAScore,
        Integer teamBScore
) {
}
