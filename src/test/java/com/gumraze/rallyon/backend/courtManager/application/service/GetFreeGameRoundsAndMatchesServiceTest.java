package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameRoundsAndMatchesQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameMatchPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundMatchResponse;
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
class GetFreeGameRoundsAndMatchesServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private LoadFreeGameRoundPort loadFreeGameRoundPort;

    @Mock
    private LoadFreeGameMatchPort loadFreeGameMatchPort;

    @InjectMocks
    private GetFreeGameRoundsAndMatchesService service;

    @Test
    @DisplayName("라운드가 없으면 빈 응답을 반환한다")
    void get_returns_empty_response_when_rounds_are_missing() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId)).willReturn(List.of());

        FreeGameRoundMatchResponse result = service.get(new GetFreeGameRoundsAndMatchesQuery(organizerAccountId, gameId));

        assertThat(result.gameId()).isEqualTo(gameId);
        assertThat(result.rounds()).isEmpty();
    }

    @Test
    @DisplayName("라운드와 매치를 court 순으로 응답으로 변환한다")
    void get_maps_rounds_and_matches_to_response() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);
        GameParticipant participant1 = CourtManagerTestFixtures.participant(
                freeGame,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "서승재",
                "서승재",
                Gender.MALE,
                Grade.A,
                20,
                LocalDateTime.now()
        );
        GameParticipant participant2 = CourtManagerTestFixtures.participant(
                freeGame,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "김원호",
                "김원호",
                Gender.MALE,
                Grade.A,
                20,
                LocalDateTime.now()
        );
        FreeGameMatch match = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                1,
                participant1,
                null,
                participant2,
                null,
                MatchStatus.NOT_STARTED,
                null,
                true
        );

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId)).willReturn(List.of(round));
        given(loadFreeGameMatchPort.loadMatchesByRoundIdsOrderByCourtNumber(List.of(round.getId()))).willReturn(List.of(match));

        FreeGameRoundMatchResponse result = service.get(new GetFreeGameRoundsAndMatchesQuery(organizerAccountId, gameId));

        assertThat(result.rounds()).hasSize(1);
        assertThat(result.rounds().getFirst().roundNumber()).isEqualTo(1);
        assertThat(result.rounds().getFirst().matches()).hasSize(1);
        assertThat(result.rounds().getFirst().matches().getFirst().courtNumber()).isEqualTo(1L);
        assertThat(result.rounds().getFirst().matches().getFirst().teamAIds()).containsExactly(participant1.getId(), null);
        assertThat(result.rounds().getFirst().matches().getFirst().teamBIds()).containsExactly(participant2.getId(), null);
        assertThat(result.rounds().getFirst().matches().getFirst().matchResult()).isEqualTo(MatchResult.NULL);
    }
}
