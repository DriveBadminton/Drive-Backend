package com.gumraze.rallyon.backend.courtManager.application.port.out;

import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;

import java.util.List;

public interface ManageFreeGameRoundMatchPort {

    FreeGameRound saveRound(FreeGameRound round);

    FreeGameMatch saveMatch(FreeGameMatch match);

    void replaceMatches(FreeGameRound round, List<FreeGameMatch> matches);
}
