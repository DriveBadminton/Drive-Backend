package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.IssueShareCodePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadAccountPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameRoundPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveFreeGameSettingPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.SaveGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.GameType;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.domain.FreeGameScheduleValidator;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.CourtAssignment;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.RoundAssignment;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CreateFreeGameServiceTest {

    @Mock
    private LoadAccountPort loadAccountPort;

    @Mock
    private IssueShareCodePort issueShareCodePort;

    @Mock
    private SaveFreeGamePort saveFreeGamePort;

    @Mock
    private SaveFreeGameSettingPort saveFreeGameSettingPort;

    @Mock
    private SaveGameParticipantPort saveGameParticipantPort;

    @Mock
    private SaveFreeGameRoundPort saveFreeGameRoundPort;

    @Mock
    private FreeGameScheduleValidator freeGameScheduleValidator;

    @InjectMocks
    private CreateFreeGameService createFreeGameService;

    @Test
    @DisplayName("자유게임 생성 시 shareCode, setting, 참가자, 코트 배정을 함께 처리한다")
    void createFreeGame_savesShareCodeSettingParticipantsAndRoundAssignments() {
        UUID organizerAccountId = UUID.randomUUID();
        String shareCode = "share-code-123";
        UUID gameId = UUID.randomUUID();
        GameParticipant p1 = savedParticipant("서승재");
        GameParticipant p2 = savedParticipant("김원호");
        GameParticipant p3 = savedParticipant("안세영");
        GameParticipant p4 = savedParticipant("정나은");
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 4, 1, 15, 10);

        CreateFreeGameCommand command = createCommand(
                MatchRecordMode.RESULT,
                null,
                List.of(1L, 2L, 3L, 4L),
                null
        );

        FreeGame savedGame = FreeGame.create(
                "수요 자유게임",
                organizerAccountId,
                GradeType.NATIONAL,
                MatchRecordMode.RESULT,
                shareCode,
                scheduledAt,
                "잠실 배드민턴장"
        );
        ReflectionTestUtils.setField(savedGame, "id", gameId);

        given(loadAccountPort.existsById(organizerAccountId)).willReturn(true);
        given(issueShareCodePort.issue()).willReturn(shareCode);
        given(freeGameScheduleValidator.parseRequiredFuture(command.scheduledAt())).willReturn(scheduledAt);
        given(saveFreeGamePort.save(any())).willReturn(savedGame);
        given(saveGameParticipantPort.saveAll(any(), any())).willReturn(Map.of(
                1L, p1,
                2L, p2,
                3L, p3,
                4L, p4
        ));

        UUID createdGameId = createFreeGameService.create(organizerAccountId, command);

        assertThat(createdGameId).isEqualTo(gameId);
        then(issueShareCodePort).should().issue();

        ArgumentCaptor<FreeGame> freeGameCaptor = ArgumentCaptor.forClass(FreeGame.class);
        then(saveFreeGamePort).should().save(freeGameCaptor.capture());
        FreeGame freeGame = freeGameCaptor.getValue();
        assertThat(freeGame.getOrganizerAccountId()).isEqualTo(organizerAccountId);
        assertThat(freeGame.getShareCode()).isEqualTo(shareCode);
        assertThat(freeGame.getGameType()).isEqualTo(GameType.FREE);
        assertThat(freeGame.getGameStatus()).isEqualTo(GameStatus.NOT_STARTED);
        assertThat(freeGame.getMatchRecordMode()).isEqualTo(MatchRecordMode.RESULT);
        assertThat(freeGame.getScheduledAt()).isEqualTo(scheduledAt);
        assertThat(freeGame.getLocation()).isEqualTo("잠실 배드민턴장");

        then(saveFreeGameSettingPort).should().save(savedGame, 1, 1);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoundAssignment>> roundAssignmentsCaptor =
                (ArgumentCaptor<List<RoundAssignment>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        then(saveFreeGameRoundPort).should().saveAll(any(), roundAssignmentsCaptor.capture());
        CourtAssignment courtAssignment = roundAssignmentsCaptor.getValue().getFirst().courts().getFirst();
        assertThat(courtAssignment.slot1ParticipantId()).isEqualTo(p1.getId());
        assertThat(courtAssignment.slot2ParticipantId()).isEqualTo(p2.getId());
        assertThat(courtAssignment.slot3ParticipantId()).isEqualTo(p3.getId());
        assertThat(courtAssignment.slot4ParticipantId()).isEqualTo(p4.getId());
    }

    @Test
    @DisplayName("managerIds가 2명을 초과하면 예외가 발생한다")
    void createFreeGame_withTooManyManagers_throws() {
        UUID organizerAccountId = UUID.randomUUID();
        given(loadAccountPort.existsById(organizerAccountId)).willReturn(true);

        CreateFreeGameCommand command = createCommand(
                MatchRecordMode.STATUS_ONLY,
                List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                List.of(1L, 2L, 3L, 4L),
                null
        );

        assertThatThrownBy(() -> createFreeGameService.create(organizerAccountId, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("managerIds");

        then(saveFreeGamePort).should(never()).save(any());
    }

    @Test
    @DisplayName("slot에 null이 있어도 라운드 배정을 생성할 수 있다")
    void createFreeGame_withNullSlots_allowsSparseRoundAssignment() {
        UUID organizerAccountId = UUID.randomUUID();
        String shareCode = "share-code-123";
        UUID gameId = UUID.randomUUID();
        GameParticipant p1 = savedParticipant("서승재");
        LocalDateTime scheduledAt = LocalDateTime.of(2026, 4, 1, 15, 10);

        CreateFreeGameCommand command = createCommand(
                MatchRecordMode.STATUS_ONLY,
                null,
                Arrays.asList(1L, null, null, null),
                null
        );

        FreeGame savedGame = FreeGame.create(
                "수요 자유게임",
                organizerAccountId,
                GradeType.NATIONAL,
                MatchRecordMode.STATUS_ONLY,
                shareCode,
                scheduledAt,
                "잠실 배드민턴장"
        );
        ReflectionTestUtils.setField(savedGame, "id", gameId);

        given(loadAccountPort.existsById(organizerAccountId)).willReturn(true);
        given(issueShareCodePort.issue()).willReturn(shareCode);
        given(freeGameScheduleValidator.parseRequiredFuture(command.scheduledAt())).willReturn(scheduledAt);
        given(saveFreeGamePort.save(any())).willReturn(savedGame);
        given(saveGameParticipantPort.saveAll(any(), any())).willReturn(Map.of(1L, p1));

        createFreeGameService.create(organizerAccountId, command);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoundAssignment>> roundAssignmentsCaptor =
                (ArgumentCaptor<List<RoundAssignment>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(List.class);
        then(saveFreeGameRoundPort).should().saveAll(any(), roundAssignmentsCaptor.capture());
        CourtAssignment courtAssignment = roundAssignmentsCaptor.getValue().getFirst().courts().getFirst();
        assertThat(courtAssignment.slot1ParticipantId()).isEqualTo(p1.getId());
        assertThat(courtAssignment.slot2ParticipantId()).isNull();
        assertThat(courtAssignment.slot3ParticipantId()).isNull();
        assertThat(courtAssignment.slot4ParticipantId()).isNull();
    }

    private CreateFreeGameCommand createCommand(
            MatchRecordMode matchRecordMode,
            List<UUID> managerIds,
            List<Long> slots,
            UUID participantAccountId
    ) {
        return new CreateFreeGameCommand(
                "수요 자유게임",
                matchRecordMode,
                GradeType.NATIONAL,
                1,
                1,
                "2026-04-01T15:10",
                "잠실 배드민턴장",
                managerIds,
                List.of(
                        new CreateFreeGameCommand.Participant(1L, participantAccountId, "서승재", Gender.MALE, Grade.A, 20),
                        new CreateFreeGameCommand.Participant(2L, null, "김원호", Gender.MALE, Grade.A, 20),
                        new CreateFreeGameCommand.Participant(3L, null, "안세영", Gender.FEMALE, Grade.A, 20),
                        new CreateFreeGameCommand.Participant(4L, null, "정나은", Gender.FEMALE, Grade.A, 20)
                ),
                List.of(new CreateFreeGameCommand.Round(
                        1,
                        List.of(new CreateFreeGameCommand.Court(1, slots))
                ))
        );
    }

    private GameParticipant savedParticipant(String originalName) {
        GameParticipant participant = GameParticipant.create(
                FreeGame.create("temp", UUID.randomUUID(), GradeType.NATIONAL, MatchRecordMode.STATUS_ONLY, null, null, null),
                null,
                originalName,
                originalName,
                Gender.MALE,
                Grade.A,
                20
        );
        ReflectionTestUtils.setField(participant, "id", UUID.randomUUID());
        return participant;
    }
}
