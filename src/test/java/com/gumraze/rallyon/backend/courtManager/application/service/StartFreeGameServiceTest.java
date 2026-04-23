package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.StartFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
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
class StartFreeGameServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private SaveFreeGamePort saveFreeGamePort;

    @InjectMocks
    private StartFreeGameService service;

    @Test
    @DisplayName("미진행 자유게임을 진행 중으로 전환한다")
    void start_changes_game_status_to_in_progress() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.STATUS_ONLY);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(saveFreeGamePort.save(same(freeGame))).willReturn(freeGame);

        service.start(new StartFreeGameCommand(organizerAccountId, gameId));

        assertThat(freeGame.getGameStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        verify(saveFreeGamePort).save(freeGame);
    }
}
