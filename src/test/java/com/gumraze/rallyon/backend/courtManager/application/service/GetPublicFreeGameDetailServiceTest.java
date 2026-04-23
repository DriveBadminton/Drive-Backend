package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetPublicFreeGameDetailQuery;
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
class GetPublicFreeGameDetailServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private LoadFreeGameSettingPort loadFreeGameSettingPort;

    @InjectMocks
    private GetPublicFreeGameDetailService service;

    @Test
    @DisplayName("share code로 공개 자유게임 상세를 조회한다")
    void get_returns_public_game_detail() {
        UUID gameId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, UUID.randomUUID(), MatchRecordMode.WINNER_ONLY);
        FreeGameSetting setting = CourtManagerTestFixtures.setting(freeGame, 2, 3);
        given(loadFreeGamePort.loadGameByShareCode("share-code")).willReturn(Optional.of(freeGame));
        given(loadFreeGameSettingPort.loadSettingByGameId(gameId)).willReturn(Optional.of(setting));

        FreeGameDetailResponse result = service.get(new GetPublicFreeGameDetailQuery("share-code"));

        assertThat(result.gameId()).isEqualTo(gameId);
        assertThat(result.courtCount()).isEqualTo(2);
        assertThat(result.roundCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("share code가 없으면 예외가 발생한다")
    void get_throws_when_share_code_is_missing() {
        given(loadFreeGamePort.loadGameByShareCode("missing")).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(new GetPublicFreeGameDetailQuery("missing")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 공유 링크입니다.");
    }

    @Test
    @DisplayName("게임 세팅이 없으면 예외가 발생한다")
    void get_throws_when_setting_is_missing() {
        UUID gameId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, UUID.randomUUID(), MatchRecordMode.WINNER_ONLY);
        given(loadFreeGamePort.loadGameByShareCode("share-code")).willReturn(Optional.of(freeGame));
        given(loadFreeGameSettingPort.loadSettingByGameId(gameId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(new GetPublicFreeGameDetailQuery("share-code")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 게임 세팅입니다.");
    }
}
