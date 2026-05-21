package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CompleteFreeGameMatchCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageFreeGameRoundMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchWinnerTeam;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.support.CourtManagerTestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompleteFreeGameMatchServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private SaveFreeGamePort saveFreeGamePort;

    @Mock
    private LoadFreeGameRoundPort loadFreeGameRoundPort;

    @Mock
    private LoadFreeGameMatchPort loadFreeGameMatchPort;

    @Mock
    private ManageFreeGameRoundMatchPort manageFreeGameRoundMatchPort;

    @InjectMocks
    private CompleteFreeGameMatchService service;

    @Test
    @DisplayName("승자 기록 모드에서는 승리 팀으로 매치를 완료하고 전체 완료 시 자유게임도 완료한다")
    void complete_winner_only_match_and_game() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = startedGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.IN_PROGRESS);
        FreeGameMatch match = inProgressMatch(round);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameMatchPort.loadMatchByGameIdAndRoundNumberAndCourtNumber(gameId, 1, 1))
                .willReturn(Optional.of(match));
        given(loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId)).willReturn(List.of(round));
        given(loadFreeGameMatchPort.loadMatchesByRoundIdsOrderByCourtNumber(List.of(round.getId())))
                .willReturn(List.of(match));

        service.complete(new CompleteFreeGameMatchCommand(
                organizerAccountId,
                gameId,
                1,
                1,
                MatchWinnerTeam.TEAM_A,
                null,
                null
        ));

        assertThat(match.getMatchStatus()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(match.getMatchResult()).isEqualTo(MatchResult.TEAM_A_WIN);
        assertThat(round.getRoundStatus()).isEqualTo(RoundStatus.COMPLETED);
        assertThat(freeGame.getGameStatus()).isEqualTo(GameStatus.COMPLETED);
        verify(manageFreeGameRoundMatchPort).saveMatch(same(match));
        verify(manageFreeGameRoundMatchPort).saveRound(same(round));
        verify(saveFreeGamePort).save(same(freeGame));
    }

    @Test
    @DisplayName("점수 기록 모드에서는 동점을 허용하지 않는다")
    void complete_score_rejects_tie() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = startedGame(gameId, organizerAccountId, MatchRecordMode.SCORE);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.IN_PROGRESS);
        FreeGameMatch match = inProgressMatch(round);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameMatchPort.loadMatchByGameIdAndRoundNumberAndCourtNumber(gameId, 1, 1))
                .willReturn(Optional.of(match));

        assertThatThrownBy(() -> service.complete(new CompleteFreeGameMatchCommand(
                organizerAccountId,
                gameId,
                1,
                1,
                null,
                21,
                21
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("점수 기록에서는 동점을 허용하지 않습니다.");

        verify(manageFreeGameRoundMatchPort, never()).saveMatch(any());
    }

    @Test
    @DisplayName("진행만 모드에서는 결과와 점수 없이 매치를 완료한다")
    void complete_status_only_without_result() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = startedGame(gameId, organizerAccountId, MatchRecordMode.STATUS_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.IN_PROGRESS);
        FreeGameMatch match = inProgressMatch(round);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameMatchPort.loadMatchByGameIdAndRoundNumberAndCourtNumber(gameId, 1, 1))
                .willReturn(Optional.of(match));
        given(loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId)).willReturn(List.of(round));
        given(loadFreeGameMatchPort.loadMatchesByRoundIdsOrderByCourtNumber(List.of(round.getId())))
                .willReturn(List.of(match));

        service.complete(new CompleteFreeGameMatchCommand(
                organizerAccountId,
                gameId,
                1,
                1,
                null,
                null,
                null
        ));

        assertThat(match.getMatchStatus()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(match.getMatchResult()).isNull();
        assertThat(match.getTeamAScore()).isNull();
        assertThat(match.getTeamBScore()).isNull();
    }

    private FreeGame startedGame(UUID gameId, UUID organizerAccountId, MatchRecordMode matchRecordMode) {
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, matchRecordMode);
        freeGame.start();
        return freeGame;
    }

    private FreeGameMatch inProgressMatch(FreeGameRound round) {
        FreeGameMatch match = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                1,
                null,
                null,
                null,
                null,
                MatchStatus.NOT_STARTED,
                MatchResult.NULL,
                true
        );
        match.start();
        return match;
    }
}
