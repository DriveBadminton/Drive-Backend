package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameSettingPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameMatchRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameRoundRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameSettingRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoadFreeGamePersistenceAdapter implements
        LoadFreeGamePort,
        LoadFreeGameSettingPort,
        LoadFreeGameRoundPort,
        LoadFreeGameMatchPort,
        LoadGameParticipantPort {

    private final GameRepository gameRepository;
    private final FreeGameSettingRepository freeGameSettingRepository;
    private final FreeGameRoundRepository freeGameRoundRepository;
    private final FreeGameMatchRepository freeGameMatchRepository;
    private final GameParticipantRepository gameParticipantRepository;

    @Override
    public Optional<FreeGame> loadGameById(UUID gameId) {
        return gameRepository.findById(gameId);
    }

    @Override
    public Optional<FreeGame> loadGameByShareCode(String shareCode) {
        return gameRepository.findByShareCode(shareCode);
    }

    @Override
    public Optional<FreeGameSetting> loadSettingByGameId(UUID gameId) {
        return freeGameSettingRepository.findByFreeGameId(gameId);
    }

    @Override
    public List<FreeGameRound> loadRoundsByGameIdOrderByRoundNumber(UUID gameId) {
        return freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId);
    }

    @Override
    public List<FreeGameMatch> loadMatchesByRoundIdsOrderByCourtNumber(List<UUID> roundIds) {
        return freeGameMatchRepository.findByRoundIdInOrderByCourtNumber(roundIds);
    }

    @Override
    public Optional<FreeGameMatch> loadMatchByGameIdAndRoundNumberAndCourtNumber(
            UUID gameId,
            Integer roundNumber,
            Integer courtNumber
    ) {
        return freeGameMatchRepository.findByRoundFreeGameIdAndRoundRoundNumberAndCourtNumber(
                gameId,
                roundNumber,
                courtNumber
        );
    }

    @Override
    public List<GameParticipant> loadParticipantsByGameId(UUID gameId) {
        return gameParticipantRepository.findByFreeGameId(gameId);
    }

    @Override
    public Optional<GameParticipant> loadParticipantById(UUID participantId) {
        return gameParticipantRepository.findById(participantId);
    }
}
