package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.CompleteFreeGameMatchUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CompleteFreeGameMatchCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageFreeGameRoundMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchWinnerTeam;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompleteFreeGameMatchService implements CompleteFreeGameMatchUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final SaveFreeGamePort saveFreeGamePort;
    private final LoadFreeGameRoundPort loadFreeGameRoundPort;
    private final LoadFreeGameMatchPort loadFreeGameMatchPort;
    private final ManageFreeGameRoundMatchPort manageFreeGameRoundMatchPort;

    @Override
    public void complete(CompleteFreeGameMatchCommand command) {
        FreeGame freeGame = FreeGameAccessSupport.loadOrganizerGame(
                loadFreeGamePort,
                command.organizerId(),
                command.gameId()
        );
        if (freeGame.getGameStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("진행 중인 자유게임의 매치만 완료할 수 있습니다.");
        }

        FreeGameMatch match = loadMatch(command);
        completeMatchByRecordMode(freeGame, match, command);
        manageFreeGameRoundMatchPort.saveMatch(match);

        refreshRoundAndGameStatus(freeGame, command.gameId(), match.getRound());
    }

    private FreeGameMatch loadMatch(CompleteFreeGameMatchCommand command) {
        return loadFreeGameMatchPort.loadMatchByGameIdAndRoundNumberAndCourtNumber(
                        command.gameId(),
                        command.roundNumber(),
                        command.courtNumber()
                )
                .orElseThrow(() -> new NotFoundException(
                        "존재하지 않는 매치입니다. gameId: " + command.gameId()
                                + ", roundNumber: " + command.roundNumber()
                                + ", courtNumber: " + command.courtNumber()
                ));
    }

    private void completeMatchByRecordMode(
            FreeGame freeGame,
            FreeGameMatch match,
            CompleteFreeGameMatchCommand command
    ) {
        switch (freeGame.getMatchRecordMode()) {
            case STATUS_ONLY -> match.completeStatusOnly();
            case WINNER_ONLY -> match.completeWithWinner(toMatchResult(command.winnerTeam()));
            case SCORE -> match.completeWithScore(command.teamAScore(), command.teamBScore());
        }
    }

    private MatchResult toMatchResult(MatchWinnerTeam winnerTeam) {
        if (winnerTeam == null) {
            throw new IllegalArgumentException("승자 기록은 승리 팀이 필요합니다.");
        }
        return switch (winnerTeam) {
            case TEAM_A -> MatchResult.TEAM_A_WIN;
            case TEAM_B -> MatchResult.TEAM_B_WIN;
        };
    }

    private void refreshRoundAndGameStatus(
            FreeGame freeGame,
            UUID gameId,
            FreeGameRound targetRound
    ) {
        List<FreeGameRound> rounds = loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId);
        if (rounds.isEmpty()) {
            return;
        }

        List<UUID> roundIds = rounds.stream()
                .map(FreeGameRound::getId)
                .toList();
        List<FreeGameMatch> activeMatches = loadFreeGameMatchPort.loadMatchesByRoundIdsOrderByCourtNumber(roundIds)
                .stream()
                .filter(match -> Boolean.TRUE.equals(match.getIsActive()))
                .toList();

        if (isRoundCompleted(targetRound, activeMatches)) {
            targetRound.finish(LocalDateTime.now());
            manageFreeGameRoundMatchPort.saveRound(targetRound);
        }

        if (!activeMatches.isEmpty() && activeMatches.stream().allMatch(this::isCompleted)) {
            freeGame.complete();
            saveFreeGamePort.save(freeGame);
        }
    }

    private boolean isRoundCompleted(FreeGameRound round, List<FreeGameMatch> activeMatches) {
        List<FreeGameMatch> roundMatches = activeMatches.stream()
                .filter(match -> match.getRound().getId().equals(round.getId()))
                .toList();
        return !roundMatches.isEmpty() && roundMatches.stream().allMatch(this::isCompleted);
    }

    private boolean isCompleted(FreeGameMatch match) {
        return match.getMatchStatus() == MatchStatus.COMPLETED;
    }
}
