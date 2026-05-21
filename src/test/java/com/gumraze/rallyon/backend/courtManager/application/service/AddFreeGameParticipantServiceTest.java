package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.AddFreeGameParticipantCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.out.AddGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.constants.GameStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AddFreeGameParticipantServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private AddGameParticipantPort addGameParticipantPort;

    @InjectMocks
    private AddFreeGameParticipantService service;

    @Test
    @DisplayName("운영자는 자신의 자유게임에 참가자 1명을 추가할 수 있다")
    void add_addsParticipantToOwnedGame() {
        UUID organizerAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.create(
                "자유게임",
                organizerAccountId,
                GradeType.NATIONAL,
                MatchRecordMode.WINNER_ONLY,
                "share-code",
                "숙지배드민턴"
        );
        ReflectionTestUtils.setField(freeGame, "id", gameId);

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        UUID participantId = UUID.randomUUID();
        AddFreeGameParticipantCommand command = new AddFreeGameParticipantCommand(
                null,
                "서승재",
                Gender.MALE,
                Grade.C,
                20
        );

        GameParticipant savedParticipant = GameParticipant.create(
                freeGame,
                null,
                "서승재",
                "서승재",
                Gender.MALE,
                Grade.C,
                20
        );
        ReflectionTestUtils.setField(savedParticipant, "id", participantId);

        given(addGameParticipantPort.add(freeGame, command)).willReturn(savedParticipant);

        UUID result = service.add(organizerAccountId, gameId, command);

        assertThat(result).isEqualTo(participantId);
        verify(loadFreeGamePort).loadGameById(gameId);
        verify(addGameParticipantPort).add(freeGame, command);
    }

    @Test
    @DisplayName("운영자는 진행 중인 자유게임에도 참가자를 추가할 수 있다")
    void add_addsParticipantToInProgressGame() {
        UUID organizerAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.create(
                "자유게임",
                organizerAccountId,
                GradeType.NATIONAL,
                MatchRecordMode.WINNER_ONLY,
                "share-code",
                "숙지배드민턴"
        );
        ReflectionTestUtils.setField(freeGame, "id", gameId);
        ReflectionTestUtils.setField(freeGame, "gameStatus", GameStatus.IN_PROGRESS);

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        UUID participantId = UUID.randomUUID();
        AddFreeGameParticipantCommand command = new AddFreeGameParticipantCommand(
                null,
                "서승재",
                Gender.MALE,
                Grade.C,
                20
        );

        GameParticipant savedParticipant = GameParticipant.create(
                freeGame,
                null,
                "서승재",
                "서승재",
                Gender.MALE,
                Grade.C,
                20
        );
        ReflectionTestUtils.setField(savedParticipant, "id", participantId);

        given(addGameParticipantPort.add(freeGame, command)).willReturn(savedParticipant);

        UUID result = service.add(organizerAccountId, gameId, command);

        assertThat(result).isEqualTo(participantId);
    }

    @Test
    @DisplayName("존재하지 않는 자유게임이면 예외가 발생한다")
    void add_throwsWhenGameDoesNotExist() {
        UUID organizerAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.add(
                organizerAccountId,
                gameId,
                new AddFreeGameParticipantCommand(null, "서승재", Gender.MALE, Grade.C, 20)
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(gameId.toString());
    }

    @Test
    @DisplayName("운영자가 아니면 참가자를 추가할 수 없다")
    void add_throwsWhenRequesterIsNotOrganizer() {
        UUID organizerAccountId = UUID.randomUUID();
        UUID requesterAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.create(
                "자유게임",
                organizerAccountId,
                GradeType.NATIONAL,
                MatchRecordMode.WINNER_ONLY,
                "share-code",
                "숙지배드민턴"
        );
        ReflectionTestUtils.setField(freeGame, "id", gameId);

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        assertThatThrownBy(() -> service.add(
                requesterAccountId,
                gameId,
                new AddFreeGameParticipantCommand(null, "서승재", Gender.MALE, Grade.C, 20)
        ))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining(gameId.toString());
    }

    @Test
    @DisplayName("완료된 자유게임에는 참가자를 추가할 수 없다")
    void add_throwsWhenGameIsCompleted() {
        UUID organizerAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        FreeGame freeGame = FreeGame.create(
                "자유게임",
                organizerAccountId,
                GradeType.NATIONAL,
                MatchRecordMode.WINNER_ONLY,
                "share-code",
                "숙지배드민턴"
        );
        ReflectionTestUtils.setField(freeGame, "id", gameId);
        ReflectionTestUtils.setField(freeGame, "gameStatus", GameStatus.COMPLETED);

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));

        assertThatThrownBy(() -> service.add(
                organizerAccountId,
                gameId,
                new AddFreeGameParticipantCommand(null, "서승재", Gender.MALE, Grade.C, 20)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료된 자유게임");
    }
}
