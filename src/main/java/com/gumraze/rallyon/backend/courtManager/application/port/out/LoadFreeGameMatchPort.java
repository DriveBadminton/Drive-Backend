package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadFreeGameMatchPort {

    List<FreeGameMatch> loadMatchesByRoundIdsOrderByCourtNumber(List<UUID> roundIds);

    Optional<FreeGameMatch> loadMatchByGameIdAndRoundNumberAndCourtNumber(
            UUID gameId,
            Integer roundNumber,
            Integer courtNumber
    );
}
