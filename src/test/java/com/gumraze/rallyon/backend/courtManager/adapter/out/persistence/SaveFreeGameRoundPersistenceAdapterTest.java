package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.CourtAssignment;
import com.gumraze.rallyon.backend.courtManager.domain.assignment.RoundAssignment;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameMatch;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGameRound;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameMatchRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.FreeGameRoundRepository;
import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SaveFreeGameRoundPersistenceAdapterTest {

    private FreeGameRoundRepository freeGameRoundRepository;
    private FreeGameMatchRepository freeGameMatchRepository;
    private GameParticipantRepository gameParticipantRepository;

    private SaveFreeGameRoundPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        freeGameRoundRepository = mock(FreeGameRoundRepository.class);
        freeGameMatchRepository = mock(FreeGameMatchRepository.class);
        gameParticipantRepository = mock(GameParticipantRepository.class);

        adapter = new SaveFreeGameRoundPersistenceAdapter(
                freeGameRoundRepository,
                freeGameMatchRepository,
                gameParticipantRepository
        );
    }

    @Test
    @DisplayName("라운드 배정을 저장하면 라운드와 매치를 생성한다")
    void saveAll_savesRoundsAndMatches() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();
        UUID p3Id = UUID.randomUUID();
        UUID p4Id = UUID.randomUUID();

        FreeGame freeGame = freeGame(gameId);

        GameParticipant p1 = participant(p1Id, "서승재");
        GameParticipant p2 = participant(p2Id, "김원호");
        GameParticipant p3 = participant(p3Id, "안세영");
        GameParticipant p4 = participant(p4Id, "정나은");

        given(gameParticipantRepository.findById(p1Id)).willReturn(Optional.of(p1));
        given(gameParticipantRepository.findById(p2Id)).willReturn(Optional.of(p2));
        given(gameParticipantRepository.findById(p3Id)).willReturn(Optional.of(p3));
        given(gameParticipantRepository.findById(p4Id)).willReturn(Optional.of(p4));

        given(freeGameRoundRepository.save(any(FreeGameRound.class)))
                .willAnswer(invocation -> {
                    FreeGameRound round = invocation.getArgument(0);
                    ReflectionTestUtils.setField(round, "id", UUID.randomUUID());
                    return round;
                });

        given(freeGameMatchRepository.save(any(FreeGameMatch.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        List<RoundAssignment> roundAssignments = List.of(
                new RoundAssignment(
                        1,
                        List.of(
                                new CourtAssignment(1, p1Id, p2Id, p3Id, p4Id)
                        )
                )
        );

        // when
        adapter.saveAll(freeGame, roundAssignments);

        // then
        ArgumentCaptor<FreeGameRound> roundCaptor = ArgumentCaptor.forClass(FreeGameRound.class);
        verify(freeGameRoundRepository).save(roundCaptor.capture());

        FreeGameRound savedRound = roundCaptor.getValue();
        assertThat(savedRound.getFreeGame()).isEqualTo(freeGame);
        assertThat(savedRound.getRoundNumber()).isEqualTo(1);
        assertThat(savedRound.getRoundStatus()).isEqualTo(RoundStatus.NOT_STARTED);

        ArgumentCaptor<FreeGameMatch> matchCaptor = ArgumentCaptor.forClass(FreeGameMatch.class);
        verify(freeGameMatchRepository).save(matchCaptor.capture());

        FreeGameMatch savedMatch = matchCaptor.getValue();
        assertThat(savedMatch.getRound().getRoundNumber()).isEqualTo(1);
        assertThat(savedMatch.getCourtNumber()).isEqualTo(1);
        assertThat(savedMatch.getTeamAPlayer1()).isEqualTo(p1);
        assertThat(savedMatch.getTeamAPlayer2()).isEqualTo(p2);
        assertThat(savedMatch.getTeamBPlayer1()).isEqualTo(p3);
        assertThat(savedMatch.getTeamBPlayer2()).isEqualTo(p4);
        assertThat(savedMatch.getMatchStatus()).isEqualTo(MatchStatus.NOT_STARTED);
        assertThat(savedMatch.getMatchResult()).isEqualTo(MatchResult.NULL);
        assertThat(savedMatch.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 participantId가 있으면 예외가 발생한다")
    void saveAll_withUnknownParticipantId_throws() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();
        UUID p3Id = UUID.randomUUID();
        UUID unknownParticipantId = UUID.randomUUID();

        FreeGame freeGame = freeGame(gameId);

        GameParticipant p1 = participant(p1Id, "서승재");
        GameParticipant p2 = participant(p2Id, "김원호");
        GameParticipant p3 = participant(p3Id, "안세영");

        given(gameParticipantRepository.findById(p1Id)).willReturn(Optional.of(p1));
        given(gameParticipantRepository.findById(p2Id)).willReturn(Optional.of(p2));
        given(gameParticipantRepository.findById(p3Id)).willReturn(Optional.of(p3));
        given(gameParticipantRepository.findById(unknownParticipantId)).willReturn(Optional.empty());

        List<RoundAssignment> roundAssignments = List.of(
                new RoundAssignment(
                        1,
                        List.of(
                                new CourtAssignment(1, p1Id, p2Id, p3Id, unknownParticipantId)
                        )
                )
        );

        // when & then
        assertThatThrownBy(() -> adapter.saveAll(freeGame, roundAssignments))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 participantId입니다.")
                .hasMessageContaining(unknownParticipantId.toString());
    }

    @Test
    @DisplayName("같은 코트에 동일한 참가자가 중복되면 예외가 발생한다")
    void saveAll_withDuplicateParticipantInSameCourt_throws() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();
        UUID p3Id = UUID.randomUUID();

        FreeGame freeGame = freeGame(gameId);

        List<RoundAssignment> roundAssignments = List.of(
                new RoundAssignment(
                        1,
                        List.of(
                                new CourtAssignment(1, p1Id, p2Id, p1Id, p3Id)
                        )
                )
        );

        // when & then
        assertThatThrownBy(() -> adapter.saveAll(freeGame, roundAssignments))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("같은 코트에는 동일한 참가자를 중복 배정할 수 없습니다.");
    }

    @Test
    @DisplayName("같은 라운드에 동일한 참가자가 다른 코트에 중복되면 예외가 발생한다")
    void saveAll_withDuplicateParticipantInSameRound_throws() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();
        UUID p3Id = UUID.randomUUID();
        UUID p4Id = UUID.randomUUID();
        UUID p5Id = UUID.randomUUID();
        UUID p6Id = UUID.randomUUID();
        UUID p7Id = UUID.randomUUID();

        FreeGame freeGame = freeGame(gameId);

        List<RoundAssignment> roundAssignments = List.of(
                new RoundAssignment(
                        1,
                        List.of(
                                new CourtAssignment(1, p1Id, p2Id, p3Id, p4Id),
                                new CourtAssignment(2, p5Id, p6Id, p1Id, p7Id)
                        )
                )
        );

        // when & then
        assertThatThrownBy(() -> adapter.saveAll(freeGame, roundAssignments))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("같은 라운드에는 동일한 참가자를 중복 배정할 수 없습니다.");
    }

    @Test
    @DisplayName("null 슬롯은 허용하고 null player로 저장한다")
    void saveAll_withNullSlots_savesMatchWithNullPlayers() {
        // given
        UUID gameId = UUID.randomUUID();
        UUID p1Id = UUID.randomUUID();
        UUID p3Id = UUID.randomUUID();

        FreeGame freeGame = freeGame(gameId);

        GameParticipant p1 = participant(p1Id, "서승재");
        GameParticipant p3 = participant(p3Id, "안세영");

        given(gameParticipantRepository.findById(p1Id)).willReturn(Optional.of(p1));
        given(gameParticipantRepository.findById(p3Id)).willReturn(Optional.of(p3));
        given(freeGameRoundRepository.save(any(FreeGameRound.class)))
                .willAnswer(invocation -> {
                    FreeGameRound round = invocation.getArgument(0);
                    ReflectionTestUtils.setField(round, "id", UUID.randomUUID());
                    return round;
                });
        given(freeGameMatchRepository.save(any(FreeGameMatch.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        List<RoundAssignment> roundAssignments = List.of(
                new RoundAssignment(
                        1,
                        List.of(
                                new CourtAssignment(1, p1Id, null, p3Id, null)
                        )
                )
        );

        // when
        adapter.saveAll(freeGame, roundAssignments);

        // then
        ArgumentCaptor<FreeGameMatch> matchCaptor = ArgumentCaptor.forClass(FreeGameMatch.class);
        verify(freeGameMatchRepository).save(matchCaptor.capture());
        FreeGameMatch savedMatch = matchCaptor.getValue();

        assertThat(savedMatch.getTeamAPlayer1()).isEqualTo(p1);
        assertThat(savedMatch.getTeamAPlayer2()).isNull();
        assertThat(savedMatch.getTeamBPlayer1()).isEqualTo(p3);
        assertThat(savedMatch.getTeamBPlayer2()).isNull();
    }

    private GameParticipant participant(UUID participantId, String originalName) {
        GameParticipant participant = GameParticipant.create(
                freeGame(UUID.randomUUID()),
                null,
                originalName,
                originalName,
                Gender.MALE,
                Grade.A,
                20
        );
        ReflectionTestUtils.setField(participant, "id", participantId);
        return participant;
    }

    private FreeGame freeGame(UUID gameId) {
        FreeGame freeGame = FreeGame.create(
                "수요 자유게임",
                UUID.randomUUID(),
                GradeType.NATIONAL,
                MatchRecordMode.STATUS_ONLY,
                null,
                null
        );
        ReflectionTestUtils.setField(freeGame, "id", gameId);
        return freeGame;
    }
}
