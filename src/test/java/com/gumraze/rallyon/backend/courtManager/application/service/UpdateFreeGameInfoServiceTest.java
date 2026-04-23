package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameInfoCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.domain.FreeGameScheduleValidator;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.support.CourtManagerTestFixtures;
import com.gumraze.rallyon.backend.user.constants.GradeType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateFreeGameInfoServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private SaveFreeGamePort saveFreeGamePort;

    @Mock
    private FreeGameScheduleValidator freeGameScheduleValidator;

    @InjectMocks
    private UpdateFreeGameInfoService service;

    @Test
    @DisplayName("자유게임 기본 정보를 수정하고 저장한다")
    void update_updates_game_info() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.STATUS_ONLY);
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 4, 2, 18, 20);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(saveFreeGamePort.save(same(freeGame))).willReturn(freeGame);
        given(freeGameScheduleValidator.parseOptionalFuture("2026-04-02T18:20")).willReturn(scheduledAt);

        UpdateFreeGameResponse result = service.update(new UpdateFreeGameInfoCommand(
                organizerAccountId,
                gameId,
                "수정된 게임",
                MatchRecordMode.WINNER_ONLY,
                GradeType.NATIONAL,
                "2026-04-02T18:20",
                "올림픽공원",
                null
        ));

        assertThat(result.gameId()).isEqualTo(gameId);
        assertThat(freeGame.getTitle()).isEqualTo("수정된 게임");
        assertThat(freeGame.getMatchRecordMode()).isEqualTo(MatchRecordMode.WINNER_ONLY);
        assertThat(freeGame.getScheduledAt()).isEqualTo(scheduledAt);
        assertThat(freeGame.getLocation()).isEqualTo("올림픽공원");
        verify(saveFreeGamePort).save(freeGame);
    }

    @Test
    @DisplayName("managerIds 수정은 아직 지원하지 않는다")
    void update_throws_when_manager_ids_are_present() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.STATUS_ONLY);
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        assertThatThrownBy(() -> service.update(new UpdateFreeGameInfoCommand(
                organizerAccountId,
                gameId,
                null,
                null,
                null,
                null,
                null,
                List.of(UUID.randomUUID())
        )))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("매니저 수정 기능은 현재 미개발 상태입니다.");

        verify(saveFreeGamePort, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("자유게임 시작 후에는 기록 방식을 수정할 수 없다")
    void update_rejects_match_record_mode_change_after_game_started() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.STATUS_ONLY);
        freeGame.start();
        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(freeGameScheduleValidator.parseOptionalFuture(null)).willReturn(null);

        assertThatThrownBy(() -> service.update(new UpdateFreeGameInfoCommand(
                organizerAccountId,
                gameId,
                null,
                MatchRecordMode.WINNER_ONLY,
                null,
                null,
                null,
                null
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("자유게임 시작 후에는 기록 방식을 변경할 수 없습니다.");

        verify(saveFreeGamePort, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
