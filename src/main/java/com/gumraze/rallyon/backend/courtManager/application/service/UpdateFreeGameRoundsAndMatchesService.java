package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.UpdateFreeGameRoundsAndMatchesUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageFreeGameRoundMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.domain.RoundMatchValidationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateFreeGameRoundsAndMatchesService implements UpdateFreeGameRoundsAndMatchesUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final LoadFreeGameRoundPort loadFreeGameRoundPort;
    private final LoadGameParticipantPort loadGameParticipantPort;
    private final ManageFreeGameRoundMatchPort manageFreeGameRoundMatchPort;

    @Override
    public UpdateFreeGameRoundMatchResponse update(UpdateFreeGameRoundsAndMatchesCommand command) {
        FreeGame freeGame = FreeGameAccessSupport.loadOrganizerGame(
                loadFreeGamePort,
                command.organizerId(),
                command.gameId()
        );

        if (freeGame.getGameStatus() == GameStatus.COMPLETED) {
            throw new IllegalArgumentException("게임 상태가 COMPLETED이므로 수정이 불가합니다.");
        }

        if (command.rounds() == null) {
            return UpdateFreeGameRoundMatchResponse.from(command.gameId());
        }

        Map<Integer, FreeGameRound> roundsByNumber = loadFreeGameRoundPort
                .loadRoundsByGameIdOrderByRoundNumber(command.gameId())
                .stream()
                .collect(Collectors.toMap(FreeGameRound::getRoundNumber, Function.identity()));

        Map<UUID, GameParticipant> participantsById = loadGameParticipantPort.loadParticipantsByGameId(command.gameId())
                .stream()
                .collect(Collectors.toMap(GameParticipant::getId, Function.identity()));

        Set<UUID> participantIdsInGame = participantsById.keySet();
        RoundMatchValidationPolicy.validate(command.rounds(), participantIdsInGame);

        for (UpdateFreeGameRoundsAndMatchesCommand.Round roundCommand : command.rounds()) {
            FreeGameRound round = roundsByNumber.get(roundCommand.roundNumber());
            if (round == null) {
                round = manageFreeGameRoundMatchPort.saveRound(
                        FreeGameRound.create(
                                freeGame,
                                roundCommand.roundNumber(),
                                RoundStatus.NOT_STARTED
                        )
                );
            }

            FreeGameRound targetRound = round;
            List<FreeGameMatch> matches = roundCommand.matches().stream()
                    .map(match -> FreeGameMatch.create(
                            targetRound,
                            match.courtNumber(),
                            resolveParticipant(participantsById, match.teamAIds().get(0)),
                            resolveParticipant(participantsById, match.teamAIds().get(1)),
                            resolveParticipant(participantsById, match.teamBIds().get(0)),
                            resolveParticipant(participantsById, match.teamBIds().get(1)),
                            null,
                            MatchStatus.NOT_STARTED,
                            MatchResult.NULL,
                            true
                    ))
                    .toList();

            manageFreeGameRoundMatchPort.replaceMatches(targetRound, matches);
        }

        return UpdateFreeGameRoundMatchResponse.from(command.gameId());
    }

    private GameParticipant resolveParticipant(Map<UUID, GameParticipant> participantsById, UUID participantId) {
        if (participantId == null) {
            return null;
        }
        return participantsById.get(participantId);
    }
}
