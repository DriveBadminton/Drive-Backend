package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameMatchRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameRoundRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameSettingRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameRepository;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.support.CourtManagerTestFixtures;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LoadFreeGamePersistenceAdapterTest {

    private GameRepository gameRepository;
    private FreeGameSettingRepository freeGameSettingRepository;
    private FreeGameRoundRepository freeGameRoundRepository;
    private FreeGameMatchRepository freeGameMatchRepository;
    private GameParticipantRepository gameParticipantRepository;

    private LoadFreeGamePersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        gameRepository = mock(GameRepository.class);
        freeGameSettingRepository = mock(FreeGameSettingRepository.class);
        freeGameRoundRepository = mock(FreeGameRoundRepository.class);
        freeGameMatchRepository = mock(FreeGameMatchRepository.class);
        gameParticipantRepository = mock(GameParticipantRepository.class);
        adapter = new LoadFreeGamePersistenceAdapter(
                gameRepository,
                freeGameSettingRepository,
                freeGameRoundRepository,
                freeGameMatchRepository,
                gameParticipantRepository
        );
    }

    @Test
    @DisplayName("게임과 세팅을 조회한다")
    void load_game_and_setting() {
        UUID gameId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, UUID.randomUUID(), MatchRecordMode.WINNER_ONLY);
        FreeGameSetting setting = CourtManagerTestFixtures.setting(freeGame, 2, 4);

        given(gameRepository.findById(gameId)).willReturn(Optional.of(freeGame));
        given(freeGameSettingRepository.findByFreeGameId(gameId)).willReturn(Optional.of(setting));

        assertThat(adapter.loadGameById(gameId)).contains(freeGame);
        assertThat(adapter.loadSettingByGameId(gameId)).contains(setting);
    }

    @Test
    @DisplayName("라운드, 매치, 참가자 조회를 위임한다")
    void load_rounds_matches_and_participants() {
        UUID gameId = UUID.randomUUID();
        UUID roundId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, UUID.randomUUID(), MatchRecordMode.WINNER_ONLY);
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, roundId, 1, RoundStatus.NOT_STARTED);
        GameParticipant participant = CourtManagerTestFixtures.participant(
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
        FreeGameMatch match = CourtManagerTestFixtures.match(
                round,
                UUID.randomUUID(),
                1,
                participant,
                null,
                null,
                null,
                null,
                null,
                true
        );

        given(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).willReturn(List.of(round));
        given(freeGameMatchRepository.findByRoundIdInOrderByCourtNumber(List.of(roundId))).willReturn(List.of(match));
        given(gameParticipantRepository.findByFreeGameId(gameId)).willReturn(List.of(participant));
        given(gameParticipantRepository.findById(participant.getId())).willReturn(Optional.of(participant));

        assertThat(adapter.loadRoundsByGameIdOrderByRoundNumber(gameId)).containsExactly(round);
        assertThat(adapter.loadMatchesByRoundIdsOrderByCourtNumber(List.of(roundId))).containsExactly(match);
        assertThat(adapter.loadParticipantsByGameId(gameId)).containsExactly(participant);
        assertThat(adapter.loadParticipantById(participant.getId())).contains(participant);

        verify(freeGameRoundRepository).findByFreeGameIdOrderByRoundNumber(gameId);
        verify(freeGameMatchRepository).findByRoundIdInOrderByCourtNumber(List.of(roundId));
    }
}
