package com.gumraze.rallyon.backend.courtManager.adapter.out.persistence;

import com.gumraze.rallyon.backend.courtManager.adapter.out.persistence.repository.GameParticipantRepository;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.entity.FreeGame;
import com.gumraze.rallyon.backend.courtManager.entity.GameParticipant;
import com.gumraze.rallyon.backend.identity.adapter.out.persistence.repository.AccountRepository;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class SaveGameParticipantPersistenceAdapterTest {

    private GameParticipantRepository gameParticipantRepository;
    private AccountRepository accountRepository;
    private SaveGameParticipantPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        gameParticipantRepository = mock(GameParticipantRepository.class);
        accountRepository = mock(AccountRepository.class);
        adapter = new SaveGameParticipantPersistenceAdapter(gameParticipantRepository, accountRepository);
    }

    @Test
    @DisplayName("참가자를 저장하고 participantId 기준 매핑을 반환한다")
    void saveAll_returnsParticipantsByClientId() {
        FreeGame freeGame = freeGame();

        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant(1L, null, "서승재", Gender.MALE, Grade.SS, 20),
                new CreateFreeGameCommand.Participant(2L, null, "김원호", Gender.MALE, Grade.SS, 20)
        );

        AtomicInteger sequence = new AtomicInteger(1);
        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> {
                    GameParticipant participant = invocation.getArgument(0);
                    UUID participantId = UUID.fromString(
                            String.format("018f1a1e-2b2f-7c11-9a55-%012d", sequence.getAndIncrement())
                    );
                    ReflectionTestUtils.setField(participant, "id", participantId);
                    return participant;
                });

        Map<Long, GameParticipant> result = adapter.saveAll(freeGame, participants);

        assertThat(result).hasSize(2);
        assertThat(result).containsKeys(1L, 2L);
        assertThat(result.get(1L).getOriginalName()).isEqualTo("서승재");
        assertThat(result.get(2L).getOriginalName()).isEqualTo("김원호");
    }

    @Test
    @DisplayName("accountId가 있으면 회원 참가자로 저장한다")
    void saveAll_withUserId_loadsAndAssignsAccountId() {
        UUID accountId = UUID.randomUUID();
        FreeGame freeGame = freeGame();
        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant(1L, accountId, "서승재", Gender.MALE, Grade.A, 20)
        );

        given(accountRepository.findById(accountId)).willReturn(Optional.of(mock()));
        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        adapter.saveAll(freeGame, participants);

        ArgumentCaptor<GameParticipant> captor = ArgumentCaptor.forClass(GameParticipant.class);
        verify(gameParticipantRepository).save(captor.capture());
        assertThat(captor.getValue().getAccountId()).isEqualTo(accountId);
    }

    @Test
    @DisplayName("존재하지 않는 accountId면 예외가 발생한다")
    void saveAll_withUnknownUserId_throws() {
        UUID accountId = UUID.randomUUID();
        FreeGame freeGame = freeGame();
        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant(1L, accountId, "서승재", Gender.MALE, Grade.A, 20)
        );

        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.saveAll(freeGame, participants))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("accountId");
    }

    @Test
    @DisplayName("동일 정보를 가진 참가자는 displayName 접미사로 구분한다")
    void saveAll_withDuplicateParticipants_assignsDistinctDisplayNames() {
        FreeGame freeGame = freeGame();
        List<CreateFreeGameCommand.Participant> participants = List.of(
                new CreateFreeGameCommand.Participant(1L, null, "홍길동", Gender.MALE, Grade.A, 20),
                new CreateFreeGameCommand.Participant(2L, null, "홍길동", Gender.MALE, Grade.A, 20)
        );

        given(gameParticipantRepository.save(any(GameParticipant.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        adapter.saveAll(freeGame, participants);

        ArgumentCaptor<GameParticipant> captor = ArgumentCaptor.forClass(GameParticipant.class);
        verify(gameParticipantRepository, times(2)).save(captor.capture());
        List<GameParticipant> savedParticipants = captor.getAllValues();

        assertThat(savedParticipants.get(0).getDisplayName()).isEqualTo("홍길동");
        assertThat(savedParticipants.get(1).getDisplayName()).isEqualTo("홍길동A");
    }

    private FreeGame freeGame() {
        FreeGame freeGame = FreeGame.create(
                "자유게임",
                UUID.randomUUID(),
                GradeType.NATIONAL,
                com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode.STATUS_ONLY,
                null,
                null
        );
        ReflectionTestUtils.setField(freeGame, "id", UUID.randomUUID());
        return freeGame;
    }
}
