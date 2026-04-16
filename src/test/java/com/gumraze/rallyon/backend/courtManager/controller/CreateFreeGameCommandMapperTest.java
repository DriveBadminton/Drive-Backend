package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateFreeGameCommandMapperTest {

    private final CreateFreeGameCommandMapper mapper = new CreateFreeGameCommandMapper();

    @Test
    @DisplayName("create request를 create command로 변환한다")
    void toCommand_mapsRequestToCommand() {
        // given
        UUID managerId1 = UUID.randomUUID();
        UUID managerId2 = UUID.randomUUID();
        UUID participantAccountId = UUID.randomUUID();
        CreateFreeGameRequest request = new CreateFreeGameRequest(
                "수요 자유게임",
                MatchRecordMode.STATUS_ONLY,
                GradeType.NATIONAL,
                2,
                1,
                "2026-04-01T15:10",
                "잠실 배드민턴장",
                List.of(managerId1, managerId2),
                List.of(
                        new CreateFreeGameRequest.ParticipantRequest(
                                1L,
                                participantAccountId,
                                "김대환",
                                Gender.MALE,
                                Grade.C,
                                20
                        )
                ),
                List.of(
                        new CreateFreeGameRequest.RoundRequest(
                                1,
                                List.of(
                                        new CreateFreeGameRequest.CourtRequest(
                                                1,
                                                Arrays.asList(1L, null, null, null)
                                        )
                                )
                        )
                )
        );

        // when
        CreateFreeGameCommand command = mapper.toCommand(request);

        // then
        assertThat(command.title()).isEqualTo("수요 자유게임");
        assertThat(command.matchRecordMode()).isEqualTo(MatchRecordMode.STATUS_ONLY);
        assertThat(command.gradeType()).isEqualTo(GradeType.NATIONAL);
        assertThat(command.courtCount()).isEqualTo(2);
        assertThat(command.roundCount()).isEqualTo(1);
        assertThat(command.scheduledAt()).isEqualTo("2026-04-01T15:10");
        assertThat(command.location()).isEqualTo("잠실 배드민턴장");
        assertThat(command.managerIds()).containsExactly(managerId1, managerId2);

        assertThat(command.participants()).hasSize(1);
        assertThat(command.participants().getFirst().participantId()).isEqualTo(1L);
        assertThat(command.participants().getFirst().accountId()).isEqualTo(participantAccountId);
        assertThat(command.participants().getFirst().originalName()).isEqualTo("김대환");

        assertThat(command.rounds()).hasSize(1);
        assertThat(command.rounds().getFirst().roundNumber()).isEqualTo(1);
        assertThat(command.rounds().getFirst().courts()).hasSize(1);
        assertThat(command.rounds().getFirst().courts().getFirst().courtNumber()).isEqualTo(1);
        assertThat(command.rounds().getFirst().courts().getFirst().slots())
                .containsExactly(1L, null, null, null);
    }
}
