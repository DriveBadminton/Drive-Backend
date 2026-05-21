package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantsQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.support.CourtManagerTestFixtures;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetFreeGameParticipantsServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private LoadGameParticipantPort loadGameParticipantPort;

    @Mock
    private LoadFreeGameRoundPort loadFreeGameRoundPort;

    @Mock
    private LoadFreeGameMatchPort loadFreeGameMatchPort;

    @InjectMocks
    private GetFreeGameParticipantsService service;

    @Test
    @DisplayName("통계 미포함 조회는 createdAt, id 순으로 정렬하고 기본 정보만 반환한다")
    void get_returns_basic_participants_sorted_by_created_at_and_id() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);

        GameParticipant later = CourtManagerTestFixtures.participant(
                freeGame,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "나중 생성",
                "나중 생성",
                Gender.MALE,
                Grade.B,
                20,
                LocalDateTime.of(2026, 3, 25, 12, 0)
        );
        GameParticipant earlier = CourtManagerTestFixtures.participant(
                freeGame,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "먼저 생성",
                "먼저 생성",
                Gender.FEMALE,
                Grade.A,
                19,
                LocalDateTime.of(2026, 3, 25, 11, 0)
        );

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadGameParticipantPort.loadParticipantsByGameId(gameId)).willReturn(List.of(later, earlier));

        FreeGameParticipantsResponse result = service.get(new GetFreeGameParticipantsQuery(organizerAccountId, gameId, false));

        assertThat(result.participants()).hasSize(2);
        assertThat(result.participants().get(0).participantId()).isEqualTo(earlier.getId());
        assertThat(result.participants().get(0).assignedMatchCount()).isNull();
        assertThat(result.participants().get(1).participantId()).isEqualTo(later.getId());
    }

    @Test
    @DisplayName("WINNER_ONLY 모드 통계 조회는 승패를 포함한다")
    void get_returns_stats_with_win_loss_in_result_mode() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);

        GameParticipant participant1 = participant(freeGame, "서승재");
        GameParticipant participant2 = participant(freeGame, "김원호");
        GameParticipant participant3 = participant(freeGame, "안세영");
        GameParticipant participant4 = participant(freeGame, "정나은");

        FreeGameMatch match = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                1,
                participant1,
                participant2,
                participant3,
                participant4,
                MatchStatus.COMPLETED,
                MatchResult.TEAM_A_WIN,
                true
        );

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadGameParticipantPort.loadParticipantsByGameId(gameId))
                .willReturn(List.of(participant1, participant2, participant3, participant4));
        given(loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId)).willReturn(List.of(round));
        given(loadFreeGameMatchPort.loadMatchesByRoundIdsOrderByCourtNumber(List.of(round.getId()))).willReturn(List.of(match));

        FreeGameParticipantsResponse result = service.get(new GetFreeGameParticipantsQuery(organizerAccountId, gameId, true));

        assertThat(result.participants().getFirst().assignedMatchCount()).isEqualTo(1);
        assertThat(result.participants().getFirst().completedMatchCount()).isEqualTo(1);
        assertThat(result.participants().getFirst().winCount()).isEqualTo(1);
        assertThat(result.participants().getFirst().lossCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("STATUS_ONLY 모드 통계 조회는 승패를 null로 반환한다")
    void get_returns_null_win_loss_in_status_only_mode() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.STATUS_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);
        GameParticipant participant1 = participant(freeGame, "서승재");
        GameParticipant participant2 = participant(freeGame, "김원호");
        GameParticipant participant3 = participant(freeGame, "안세영");
        GameParticipant participant4 = participant(freeGame, "정나은");
        FreeGameMatch match = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                1,
                participant1,
                participant2,
                participant3,
                participant4,
                MatchStatus.COMPLETED,
                MatchResult.TEAM_A_WIN,
                true
        );

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadGameParticipantPort.loadParticipantsByGameId(gameId))
                .willReturn(List.of(participant1, participant2, participant3, participant4));
        given(loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId)).willReturn(List.of(round));
        given(loadFreeGameMatchPort.loadMatchesByRoundIdsOrderByCourtNumber(List.of(round.getId()))).willReturn(List.of(match));

        FreeGameParticipantsResponse result = service.get(new GetFreeGameParticipantsQuery(organizerAccountId, gameId, true));

        assertThat(result.participants().getFirst().assignedMatchCount()).isEqualTo(1);
        assertThat(result.participants().getFirst().completedMatchCount()).isEqualTo(1);
        assertThat(result.participants().getFirst().winCount()).isNull();
        assertThat(result.participants().getFirst().lossCount()).isNull();
    }

    private GameParticipant participant(FreeGame freeGame, String name) {
        return CourtManagerTestFixtures.participant(
                freeGame,
                UUID.randomUUID(),
                UUID.randomUUID(),
                name,
                name,
                Gender.MALE,
                Grade.A,
                20,
                LocalDateTime.now()
        );
    }
}
