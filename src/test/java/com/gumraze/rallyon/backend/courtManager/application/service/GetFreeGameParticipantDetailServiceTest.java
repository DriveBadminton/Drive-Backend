package com.gumraze.rallyon.backend.courtManager.application.service;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadFreeGamePort;
import com.gumraze.rallyon.backend.courtManager.application.port.out.LoadGameParticipantPort;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.support.CourtManagerTestFixtures;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetFreeGameParticipantDetailServiceTest {

    @Mock
    private LoadFreeGamePort loadFreeGamePort;

    @Mock
    private LoadGameParticipantPort loadGameParticipantPort;

    @InjectMocks
    private GetFreeGameParticipantDetailService service;

    @Test
    @DisplayName("자유게임 참가자 상세를 조회한다")
    void get_returns_participant_detail() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        UUID participantAccountId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        GameParticipant participant = CourtManagerTestFixtures.participant(
                freeGame,
                UUID.randomUUID(),
                participantAccountId,
                "서승재",
                "서승재",
                Gender.MALE,
                Grade.A,
                20,
                LocalDateTime.now()
        );

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadGameParticipantPort.loadParticipantById(participant.getId())).willReturn(Optional.of(participant));

        FreeGameParticipantDetailResponse result = service.get(
                new GetFreeGameParticipantDetailQuery(organizerAccountId, gameId, participant.getId())
        );

        assertThat(result.participantId()).isEqualTo(participant.getId());
        assertThat(result.accountId()).isEqualTo(participantAccountId);
        assertThat(result.displayName()).isEqualTo("서승재");
    }

    @Test
    @DisplayName("참가자가 없으면 예외가 발생한다")
    void get_throws_when_participant_is_missing() {
        UUID gameId = UUID.randomUUID();
        UUID organizerAccountId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadGameParticipantPort.loadParticipantById(participantId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(
                new GetFreeGameParticipantDetailQuery(organizerAccountId, gameId, participantId)
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("존재하지 않는 참가자입니다.");
    }

    @Test
    @DisplayName("다른 게임의 참가자는 조회할 수 없다")
    void get_throws_when_participant_belongs_to_another_game() {
        UUID organizerAccountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID anotherGameId = UUID.randomUUID();
        FreeGame freeGame = CourtManagerTestFixtures.freeGame(gameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        FreeGame anotherFreeGame = CourtManagerTestFixtures.freeGame(anotherGameId, organizerAccountId, MatchRecordMode.WINNER_ONLY);
        GameParticipant participant = CourtManagerTestFixtures.participant(
                anotherFreeGame,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "서승재",
                "서승재",
                Gender.MALE,
                Grade.A,
                20,
                LocalDateTime.now()
        );

        given(loadFreeGamePort.loadGameById(gameId)).willReturn(Optional.of(freeGame));
        given(loadGameParticipantPort.loadParticipantById(participant.getId())).willReturn(Optional.of(participant));

        assertThatThrownBy(() -> service.get(
                new GetFreeGameParticipantDetailQuery(organizerAccountId, gameId, participant.getId())
        ))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("참가자가 다른 게임에 속해 있습니다.");
    }
}
