package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGameSettingPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameSetting;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetFreeGameDetailServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private LoadFreeGameSettingPort loadFreeGameSettingPort;

    @InjectMocks
    private GetFreeGameDetailService service;

    @Test
    @DisplayName("organizer는 자유게임 상세와 세팅을 조회할 수 있다")
    void get_returns_game_detail_for_organizer() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        FreeGameSetting setting = CourtManagerTestFixtures.setting(freeGame, 2, 4);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameSettingPort.loadSettingByGameId(gameId)).willReturn(Optional.of(setting));

        FreeGameDetailResponse result = service.get(new GetFreeGameDetailQuery(organizerAccountId, gameId));

        assertThat(result.gameId()).isEqualTo(gameId);
        assertThat(result.organizerAccountId()).isEqualTo(organizerAccountId);
        assertThat(result.courtCount()).isEqualTo(2);
        assertThat(result.roundCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("organizer가 아니면 상세 조회가 거부된다")
    void get_throws_when_requester_is_not_organizer() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        assertThatThrownBy(() -> service.get(new GetFreeGameDetailQuery(UUID.randomUUID(), gameId)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("세팅이 없으면 상세 조회가 실패한다")
    void get_throws_when_setting_is_missing() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadFreeGameSettingPort.loadSettingByGameId(gameId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(new GetFreeGameDetailQuery(organizerAccountId, gameId)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 게임 세팅입니다.");
    }
}
