package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageFreeGameRoundMatchPort;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameMatchRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameRoundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ManageFreeGameRoundMatchPersistenceAdapter implements ManageFreeGameRoundMatchPort {

    private final FreeGameRoundRepository freeGameRoundRepository;
    private final FreeGameMatchRepository freeGameMatchRepository;

    @Override
    public FreeGameRound saveRound(FreeGameRound round) {
        return freeGameRoundRepository.save(round);
    }

    @Override
    public FreeGameMatch saveMatch(FreeGameMatch match) {
        return freeGameMatchRepository.save(match);
    }

    @Override
    public void replaceMatches(FreeGameRound round, List<FreeGameMatch> matches) {
        if (round.getId() != null) {
            freeGameMatchRepository.deleteByRoundId(round.getId());
        }
        freeGameMatchRepository.saveAll(matches);
    }
}
