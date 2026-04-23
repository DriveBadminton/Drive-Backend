package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.StartFreeGameMatchCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageFreeGameRoundMatchPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StartFreeGameMatchServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private LoadFreeGameMatchPort loadFreeGameMatchPort;

    @Mock
    private ManageFreeGameRoundMatchPort manageFreeGameRoundMatchPort;

    @InjectMocks
    private StartFreeGameMatchService service;

    @Test
    @DisplayName("진행 중 자유게임의 미진행 매치를 시작한다")
    void start_changes_match_and_round_status_to_in_progress() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.STATUS_ONLY);
        freeGame.start();
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);
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
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameMatchPort.loadMatchByGameIdAndRoundNumberAndCourtNumber(gameId, 1, 1))
                .willReturn(Optional.of(match));

        service.start(new StartFreeGameMatchCommand(organizerAccountId, gameId, 1, 1));

        assertThat(round.getRoundStatus()).isEqualTo(RoundStatus.IN_PROGRESS);
        assertThat(match.getMatchStatus()).isEqualTo(MatchStatus.IN_PROGRESS);
        verify(manageFreeGameRoundMatchPort).saveRound(same(round));
        verify(manageFreeGameRoundMatchPort).saveMatch(same(match));
    }
}
