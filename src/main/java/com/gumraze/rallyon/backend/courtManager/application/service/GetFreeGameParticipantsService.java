package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameParticipantsUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantsQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.application.support.FreeGameAccessSupport;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.domain.ParticipantStatsCalculator;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetFreeGameParticipantsService implements GetFreeGameParticipantsUseCase {

    private final LoadFreeGamePort loadFreeGamePort;
    private final LoadGameParticipantPort loadGameParticipantPort;
    private final LoadFreeGameRoundPort loadFreeGameRoundPort;
    private final LoadFreeGameMatchPort loadFreeGameMatchPort;

    @Override
    public FreeGameParticipantsResponse get(GetFreeGameParticipantsQuery query) {
        FreeGame freeGame = FreeGameAccessSupport.loadOrganizerGame(
                loadFreeGamePort,
                query.organizerId(),
                query.gameId()
        );

        List<GameParticipant> participants = loadGameParticipantPort.loadParticipantsByGameId(query.gameId()).stream()
                .sorted(Comparator
                        .comparing(GameParticipant::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(GameParticipant::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        MatchRecordMode matchRecordMode = freeGame.getMatchRecordMode();
        if (!query.includeStats() || participants.isEmpty()) {
            return new FreeGameParticipantsResponse(
                    query.gameId(),
                    matchRecordMode,
                    participants.stream().map(this::toBasicResponse).toList()
            );
        }

        List<FreeGameRound> rounds = loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(query.gameId());
        List<UUID> roundIds = rounds.stream()
                .map(FreeGameRound::getId)
                .toList();
        Map<UUID, ParticipantStatsCalculator.ParticipantStats> statsByParticipantId =
                ParticipantStatsCalculator.calculate(
                        matchRecordMode,
                        participants,
                        roundIds.isEmpty() ? List.of() : loadFreeGameMatchPort.loadMatchesByRoundIdsOrderByCourtNumber(roundIds)
                );

        return new FreeGameParticipantsResponse(
                query.gameId(),
                matchRecordMode,
                participants.stream()
                        .map(participant -> toResponse(participant, matchRecordMode, statsByParticipantId.get(participant.getId())))
                        .toList()
        );
    }

    private FreeGameParticipantResponse toBasicResponse(GameParticipant participant) {
        return new FreeGameParticipantResponse(
                participant.getId(),
                participant.getAccountId(),
                participant.getDisplayName(),
                participant.getGender(),
                participant.getGrade(),
                participant.getAgeGroup(),
                null,
                null,
                null,
                null
        );
    }

    private FreeGameParticipantResponse toResponse(
            GameParticipant participant,
            MatchRecordMode matchRecordMode,
            ParticipantStatsCalculator.ParticipantStats stats
    ) {
        Integer winCount = hasWinLossRecord(matchRecordMode) ? stats.winCount() : null;
        Integer lossCount = hasWinLossRecord(matchRecordMode) ? stats.lossCount() : null;

        return new FreeGameParticipantResponse(
                participant.getId(),
                participant.getAccountId(),
                participant.getDisplayName(),
                participant.getGender(),
                participant.getGrade(),
                participant.getAgeGroup(),
                stats.assignedMatchCount(),
                stats.completedMatchCount(),
                winCount,
                lossCount
        );
    }

    private boolean hasWinLossRecord(MatchRecordMode matchRecordMode) {
        return matchRecordMode == MatchRecordMode.WINNER_ONLY
                || matchRecordMode == MatchRecordMode.SCORE;
    }
}
