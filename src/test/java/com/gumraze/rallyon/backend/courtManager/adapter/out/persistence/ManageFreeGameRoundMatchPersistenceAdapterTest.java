package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameMatchRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameRoundRepository;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.support.CourtManagerTestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ManageFreeGameRoundMatchPersistenceAdapterTest {

    private FreeGameRoundRepository freeGameRoundRepository;
    private FreeGameMatchRepository freeGameMatchRepository;
    private ManageFreeGameRoundMatchPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        freeGameRoundRepository = mock(FreeGameRoundRepository.class);
        freeGameMatchRepository = mock(FreeGameMatchRepository.class);
        adapter = new ManageFreeGameRoundMatchPersistenceAdapter(freeGameRoundRepository, freeGameMatchRepository);
    }

    @Test
    @DisplayName("기존 라운드의 매치를 교체할 때는 기존 매치를 삭제한 뒤 저장한다")
    void replace_matches_deletes_existing_matches_then_saves() {
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(UUID.randomUUID(), UUID.randomUUID(), MatchRecordMode.WINNER_ONLY);
        UUID roundId = UUID.randomUUID();
        FreeGameRound round = CourtManagerTestFixtures.round(freeGame, roundId, 1, RoundStatus.NOT_STARTED);
        FreeGameMatch match = mock(FreeGameMatch.class);

        adapter.replaceMatches(round, List.of(match));

        verify(freeGameMatchRepository).deleteByRoundId(roundId);
        verify(freeGameMatchRepository).saveAll(List.of(match));
    }

    @Test
    @DisplayName("아직 저장되지 않은 라운드는 delete 없이 매치만 저장한다")
    void replace_matches_skips_delete_when_round_id_is_missing() {
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(UUID.randomUUID(), UUID.randomUUID(), MatchRecordMode.WINNER_ONLY);
        FreeGameRound round = FreeGameRound.create(freeGame, 1, RoundStatus.NOT_STARTED);
        ReflectionTestUtils.setField(round, "id", null);
        FreeGameMatch match = mock(FreeGameMatch.class);

        adapter.replaceMatches(round, List.of(match));

        verify(freeGameMatchRepository, never()).deleteByRoundId(org.mockito.ArgumentMatchers.any());
        verify(freeGameMatchRepository).saveAll(List.of(match));
    }

    @Test
    @DisplayName("라운드를 저장하면 repository 결과를 반환한다")
    void save_round_returns_saved_round() {
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(UUID.randomUUID(), UUID.randomUUID(), MatchRecordMode.WINNER_ONLY);
        FreeGameRound round = FreeGameRound.create(freeGame, 1, RoundStatus.NOT_STARTED);
        FreeGameRound savedRound = CourtManagerTestFixtures.round(freeGame, UUID.randomUUID(), 1, RoundStatus.NOT_STARTED);
        given(freeGameRoundRepository.save(same(round))).willReturn(savedRound);

        adapter.saveRound(round);

        verify(freeGameRoundRepository).save(round);
    }
}
