package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.ManageFreeGameRoundMatchPort;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchResponse;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateFreeGameRoundsAndMatchesServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private LoadFreeGameRoundPort loadFreeGameRoundPort;

    @Mock
    private LoadGameParticipantPort loadGameParticipantPort;

    @Mock
    private ManageFreeGameRoundMatchPort manageFreeGameRoundMatchPort;

    @InjectMocks
    private UpdateFreeGameRoundsAndMatchesService service;

    @Test
    @DisplayName("COMPLETED 게임은 라운드/매치 수정을 허용하지 않는다")
    void update_throws_when_game_is_completed() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        ReflectionTestUtils.setField(freeGame, "gameStatus", GameStatus.COMPLETED);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        assertThatThrownBy(() -> service.update(new UpdateFreeGameRoundsAndMatchesCommand(
                organizerAccountId,
                gameId,
                List.of()
        )))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("COMPLETED");
    }

    @Test
    @DisplayName("rounds가 null이면 변경 없이 응답만 반환한다")
    void update_returns_early_when_rounds_are_null() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        UpdateFreeGameRoundMatchResponse result = service.update(new UpdateFreeGameRoundsAndMatchesCommand(
                organizerAccountId,
                gameId,
                null
        ));

        assertThat(result.gameId()).isEqualTo(gameId);
        verify(loadFreeGameRoundPort, never()).loadRoundsByGameIdOrderByRoundNumber(any());
        verify(manageFreeGameRoundMatchPort, never()).replaceMatches(any(), any());
    }

    @Test
    @DisplayName("없는 라운드는 새로 생성하고 매치를 교체 저장한다")
    void update_creates_new_round_and_replaces_matches() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        GameParticipant participant1 = participant(freeGame, "서승재");
        GameParticipant participant2 = participant(freeGame, "김원호");

        UpdateFreeGameRoundsAndMatchesCommand command = new UpdateFreeGameRoundsAndMatchesCommand(
                organizerAccountId,
                gameId,
                List.of(
                        new UpdateFreeGameRoundsAndMatchesCommand.Round(
                                1,
                                List.of(
                                        new UpdateFreeGameRoundsAndMatchesCommand.Match(
                                                1,
                                                Arrays.asList(participant1.getId(), null),
                                                Arrays.asList(participant2.getId(), null)
                                        )
                                )
                        )
                )
        );

        FreeGameRound savedRound = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId)).willReturn(List.of());
        given(loadGameParticipantPort.loadParticipantsByGameId(gameId)).willReturn(List.of(participant1, participant2));
        given(manageFreeGameRoundMatchPort.saveRound(any())).willReturn(savedRound);

        service.update(command);

        ArgumentCaptor<FreeGameRound> roundCaptor = ArgumentCaptor.forClass(FreeGameRound.class);
        verify(manageFreeGameRoundMatchPort).saveRound(roundCaptor.capture());
        assertThat(roundCaptor.getValue().getRoundNumber()).isEqualTo(1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FreeGameMatch>> matchCaptor = (ArgumentCaptor<List<FreeGameMatch>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        verify(manageFreeGameRoundMatchPort).replaceMatches(org.mockito.ArgumentMatchers.eq(savedRound), matchCaptor.capture());
        List<FreeGameMatch> matches = matchCaptor.getValue();
        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().getTeamAPlayer1()).isEqualTo(participant1);
        assertThat(matches.getFirst().getTeamAPlayer2()).isNull();
        assertThat(matches.getFirst().getTeamBPlayer1()).isEqualTo(participant2);
    }

    @Test
    @DisplayName("기존 라운드는 새로 만들지 않고 매치만 교체한다")
    void update_replaces_matches_for_existing_round() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        FreeGameRound existingRound = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);
        GameParticipant participant1 = participant(freeGame, "서승재");
        GameParticipant participant2 = participant(freeGame, "김원호");

        UpdateFreeGameRoundsAndMatchesCommand command = new UpdateFreeGameRoundsAndMatchesCommand(
                organizerAccountId,
                gameId,
                List.of(
                        new UpdateFreeGameRoundsAndMatchesCommand.Round(
                                1,
                                List.of(
                                        new UpdateFreeGameRoundsAndMatchesCommand.Match(
                                                1,
                                                Arrays.asList(participant1.getId(), null),
                                                Arrays.asList(participant2.getId(), null)
                                        )
                                )
                        )
                )
        );

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameRoundPort.loadRoundsByGameIdOrderByRoundNumber(gameId)).willReturn(List.of(existingRound));
        given(loadGameParticipantPort.loadParticipantsByGameId(gameId)).willReturn(List.of(participant1, participant2));

        service.update(command);

        verify(manageFreeGameRoundMatchPort, never()).saveRound(any());
        verify(manageFreeGameRoundMatchPort).replaceMatches(org.mockito.ArgumentMatchers.eq(existingRound), any());
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
