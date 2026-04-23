package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameInfoCommand;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRequest;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateFreeGameInfoCommandMapperTest {

    private final UpdateFreeGameInfoCommandMapper mapper = new UpdateFreeGameInfoCommandMapper();

    @Test
    @DisplayName("게임 정보 수정 request를 command로 변환한다")
    void toCommand_maps_request_to_command() {
        UUID organizerAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID managerId = UUID.randomUUID();
        UpdateFreeGameRequest request = new UpdateFreeGameRequest(
                "수정된 자유게임",
                MatchRecordMode.WINNER_ONLY,
                GradeType.NATIONAL,
                "2026-04-01T15:10",
                "올림픽공원",
                List.of(managerId)
        );

        UpdateFreeGameInfoCommand command = mapper.toCommand(organizerAccountId, gameId, request);

        assertThat(command.organizerId()).isEqualTo(organizerAccountId);
        assertThat(command.gameId()).isEqualTo(gameId);
        assertThat(command.title()).isEqualTo("수정된 자유게임");
        assertThat(command.matchRecordMode()).isEqualTo(MatchRecordMode.WINNER_ONLY);
        assertThat(command.gradeType()).isEqualTo(GradeType.NATIONAL);
        assertThat(command.scheduledAt()).isEqualTo("2026-04-01T15:10");
        assertThat(command.location()).isEqualTo("올림픽공원");
        assertThat(command.managerIds()).containsExactly(managerId);
    }
}
