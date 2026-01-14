package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.common.exception.ForbiddenException;
import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.courtManager.constants.RoundStatus;
import com.gumraze.drive.drive_backend.courtManager.dto.MatchRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.RoundRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import com.gumraze.drive.drive_backend.courtManager.entity.FreeGame;
import com.gumraze.drive.drive_backend.courtManager.entity.FreeGameMatch;
import com.gumraze.drive.drive_backend.courtManager.entity.FreeGameRound;
import com.gumraze.drive.drive_backend.courtManager.entity.GameParticipant;
import com.gumraze.drive.drive_backend.courtManager.repository.FreeGameMatchRepository;
import com.gumraze.drive.drive_backend.courtManager.repository.FreeGameRoundRepository;
import com.gumraze.drive.drive_backend.courtManager.repository.GameParticipantRepository;
import com.gumraze.drive.drive_backend.courtManager.repository.GameRepository;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import com.gumraze.drive.drive_backend.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FreeGameRoundMatchSaveServiceTest {
    @InjectMocks
    FreeGameServiceImpl freeGameService;

    @Mock
    GameRepository gameRepository;

    @Mock
    FreeGameRoundRepository freeGameRoundRepository;

    @Mock
    FreeGameMatchRepository freeGameMatchRepository;

    @Mock
    GameParticipantRepository gameParticipantRepository;

    @Test
    @DisplayName("라운드/매치 수정 시, organizer만 가능함.")
    void updateFreeGameRoundMatch_when_not_organizer_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 2L; // organizer가 아님

        User organizer = mock(User.class);

        // 게임 존재
        FreeGame freeGame = buildNewFreeGame(gameId, organizer);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));
        when(organizer.getId()).thenReturn(1L); // 실제 organizer의 id

        // request는 비어있어도 관계 없음
        UpdateFreeGameRoundMatchRequest request = UpdateFreeGameRoundMatchRequest.builder().build();

        // when & then
        assertThatThrownBy(() ->
                freeGameService.updateFreeGameRoundMatch(userId, gameId, request)
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("요청에 라운드와 매치가 포함되어 있으면 서비스는 라운드를 저장한다.")
    void saveRound_when_request_has_round_and_match_then_save_round() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of());

        FreeGameRound savedRound = buildFreeGameRound(freeGame, 1);
        savedRound.setId(10L);
        when(freeGameRoundRepository.save(any(FreeGameRound.class)))
                .thenReturn(savedRound);
        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);
        GameParticipant p3 = mockGameParticipantWithId(3L);
        GameParticipant p4 = mockGameParticipantWithId(4L);
        stubParticipantsExist(p1.getId(), p2.getId(), p3.getId(), p4.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2, p3, p4));

        // 라운드가 1개 있는 요청
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        )).build()
                        )).build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        verify(freeGameRoundRepository, times(1))
                .save(any(FreeGameRound.class));
    }

    @Test
    @DisplayName("라운드에 매치가 포함되어 있으면 매치를 저장한다")
    void saveMatch_when_round_has_matches_then_save_match() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);
        FreeGameRound savedRound = buildFreeGameRound(freeGame, 1);
        savedRound.setId(10L);
        when(freeGameRoundRepository.save(any(FreeGameRound.class)))
                .thenReturn(savedRound);

        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);
        GameParticipant p3 = mockGameParticipantWithId(3L);
        GameParticipant p4 = mockGameParticipantWithId(4L);
        stubParticipantsExist(p1.getId(), p2.getId(), p3.getId(), p4.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2, p3, p4));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        verify(freeGameMatchRepository, times(1))
                .saveAll(anyList());
    }

    @Test
    @DisplayName("라운드에 매치가 없으면 저장할 수 없다.")
    void saveRound_when_round_has_no_matches_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        stubGameWithOrganizer(gameId, userId);

        // request
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() ->
                freeGameService.updateFreeGameRoundMatch(userId, gameId, request)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("roundNumber가 기존 라운드이면 신규 라운드를 생성하지 않는다.")
    void updateFreeGameRoundMatch_when_roundNumber_is_existing_then_not_create_new_round() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        // 기존 라운드
        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));

        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);
        GameParticipant p3 = mockGameParticipantWithId(3L);
        GameParticipant p4 = mockGameParticipantWithId(4L);
        stubParticipantsExist(p1.getId(), p2.getId(), p3.getId(), p4.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2, p3, p4));

        // 기존 라운드의 요청
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1) // 기존 번호
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        // 신규 라운드는 생성되지 않아야함.
        verify(freeGameRoundRepository, never()).save(any(FreeGameRound.class));
    }

    @Test
    @DisplayName("roundNumber가 신규이면 새로운 라운드를 생성함")
    void addRound_when_roundNumber_is_new_then_create_new_round() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        // 기존 라운드
        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(existingRound));
        when(freeGameRoundRepository.save(any(FreeGameRound.class)))
                .thenReturn(existingRound);

        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);
        GameParticipant p3 = mockGameParticipantWithId(3L);
        GameParticipant p4 = mockGameParticipantWithId(4L);
        stubParticipantsExist(p1.getId(), p2.getId(), p3.getId(), p4.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2, p3, p4));

        // 라운드 추가
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(2)     // 신규 라운드
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        // 신규 라운드가 생성됨
        verify(freeGameRoundRepository, times(1)).save(any(FreeGameRound.class));
    }

    @Test
    @DisplayName("기존 라운드의 매치는 전체 교체된다.")
    void replaceMatches_when_round_exists_then_replace_all_matches() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        // 기존 round, match가 존재함
        FreeGameRound freeGameRound = buildFreeGameRound(freeGame, 1);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(freeGameRound));

        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(5L);
        GameParticipant p2 = mockGameParticipantWithId(6L);
        stubParticipantsExist(p1.getId(), p2.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)         // 기존 라운드
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(5L, 6L))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        verify(freeGameMatchRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("신규 라운드의 매치는 해당 라운드에 연결되어 저장된다.")
    void saveMatch_when_new_round_then_match_belongs_to_round() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        // 기존 라운드 없음 -> 신규 라운드 생성 케이스
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of());
        FreeGameRound savedRound = buildFreeGameRound(freeGame, 1);
        savedRound.setId(10L);
        when(freeGameRoundRepository.save(any(FreeGameRound.class)))
                .thenReturn(savedRound);

        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(5L);
        GameParticipant p2 = mockGameParticipantWithId(6L);
        stubParticipantsExist(p1.getId(), p2.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(5L, 6L))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();
        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FreeGameMatch>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(freeGameMatchRepository).saveAll(captor.capture());

        List<FreeGameMatch> savedMatches = captor.getValue();
        assertThat(savedMatches).hasSize(1);
        assertThat(savedMatches.getFirst().getRound()).isNotNull();
    }

    @Test
    @DisplayName("요청한 matches 개수만큼 매치가 저장된다.")
    void saveMatch_when_multiple_matches_then_save_all() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        // 기존 라운드가 없는 경우에는 신규 라운드 생성
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of());
        FreeGameRound savedRound = buildFreeGameRound(freeGame, 1);
        savedRound.setId(10L);
        when(freeGameRoundRepository.save(any(FreeGameRound.class)))
                .thenReturn(savedRound);

        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);
        GameParticipant p3 = mockGameParticipantWithId(3L);
        GameParticipant p4 = mockGameParticipantWithId(4L);
        GameParticipant p5 = mockGameParticipantWithId(5L);
        GameParticipant p6 = mockGameParticipantWithId(6L);
        GameParticipant p7 = mockGameParticipantWithId(7L);
        GameParticipant p8 = mockGameParticipantWithId(8L);

        stubParticipantsExist(p1.getId(), p2.getId(), p3.getId(), p4.getId(), p5.getId(), p6.getId(), p7.getId(), p8.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2, p3, p4, p5, p6, p7, p8));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build(),
                                                MatchRequest.builder()
                                                        .courtNumber(2)
                                                        .teamAIds(List.of(5L, 6L))
                                                        .teamBIds(List.of(7L, 8L))
                                                        .build()
                                        )).build()
                        ))
                        .build();
        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FreeGameMatch>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(freeGameMatchRepository).saveAll(captor.capture());
        List<FreeGameMatch> savedMatches = captor.getValue();
        assertThat(savedMatches).hasSize(2);
    }

    @Test
    @DisplayName("기존 라운드의 매치는 삭제 후 교체된다.")
    void replaceMatches_when_round_exists_then_delete_and_save() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        // 기존 round와 match가 존재함.
        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L); // deleteByRoundId 검증용
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(existingRound));

        // 참가자 stub
        GameParticipant p5 = mockGameParticipantWithId(5L);
        GameParticipant p6 = mockGameParticipantWithId(6L);
        GameParticipant p7 = mockGameParticipantWithId(7L);
        GameParticipant p8 = mockGameParticipantWithId(8L);

        stubParticipantsExist(p5.getId(), p6.getId(), p7.getId(), p8.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p5, p6, p7, p8));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(5L, 6L))
                                                        .teamBIds(List.of(7L, 8L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        // 기존 라운드의 매치는 삭제됨
        verify(freeGameMatchRepository, times(1)).deleteByRoundId(existingRound.getId());
        verify(freeGameMatchRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("기존 라운드의 매치는 요청된 개수만큼만 저장된다")
    void saveMatches_when_round_exists_then_match_count_equals_request() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(existingRound));

        // 참가자 stub
        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);
        GameParticipant p3 = mockGameParticipantWithId(3L);
        GameParticipant p4 = mockGameParticipantWithId(4L);

        stubParticipantsExist(p1.getId(), p2.getId(), p3.getId(), p4.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2, p3, p4));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build(),
                                                MatchRequest.builder()
                                                        .courtNumber(2)
                                                        .teamAIds(Arrays.asList(null, null))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FreeGameMatch>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(freeGameMatchRepository, atLeastOnce()).saveAll(captor.capture());

        int totalSavedMatches =
                captor.getAllValues().stream()
                        .mapToInt(List::size)
                        .sum();
        assertThat(totalSavedMatches).isEqualTo(2);
    }

    @Test
    @DisplayName("저장된 match는 해당 round를 참조한다.")
    void saveMatch_when_round_exists_then_match_references_round() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        // 기존 round
        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(existingRound));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(null, null))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();
        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FreeGameMatch>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(freeGameMatchRepository, atLeastOnce()).saveAll(captor.capture());

        // 저장한 모든 match를 평탄화
        List<FreeGameMatch> savedMatches =
                captor.getAllValues().stream()
                        .flatMap(List::stream)
                        .toList();

        assertThat(savedMatches).isNotEmpty();
        savedMatches.forEach(match ->
                assertThat(match.getRound()).isSameAs(existingRound)
        );
    }

    @Test
    @DisplayName("같은 라운드에 동일한 courtNumber가 있으면 예외가 발생한다.")
    void throwException_when_duplicate_courtNumber_in_same_round() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(100L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(null, null))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build(),
                                                MatchRequest.builder()
                                                        .courtNumber(1) // 동일 courtNumber
                                                        .teamAIds(Arrays.asList(null, null))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("courtNumber");
    }

    @Test
    @DisplayName("courtNumber가 1미만이면 예외가 발생한다.")
    void saveMatch_when_courtNumber_less_then_one_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        // 기존 라운드 사용
        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(100L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(0) // 에러 발생
                                                        .teamAIds(Arrays.asList(null, null))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("courtNumber");
    }

    @Test
    @DisplayName("courtNumber가 1..n 연속이 아니면 예외가 발생한다.")
    void saveMatch_when_courtNumber_is_not_sequential_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(100L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(null, null))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build(),
                                                MatchRequest.builder()
                                                        .courtNumber(3) // courtNumber가 연속이 아닌 경우 에러 발생
                                                        .teamAIds(Arrays.asList(null, null))
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("courtNumber");
    }

    @Test
    @DisplayName("teamAIds가 null이면 예외가 발생한다.")
    void saveMatch_when_teamAIds_is_null_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(100L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(null)     // 리스트 자체가 없음
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teamAIds");
    }

    @Test
    @DisplayName("teamBIds가 null이면 예외가 발생한다.")
    void saveMatch_when_teamBIds_is_null_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(100L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(null, null))     // 리스트 자체가 없음
                                                        .teamBIds(null)     // teamBIds가 null
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("teamBIds");
    }

    @Test
    @DisplayName("teamAIds/teamBIds 길이가 2가 아니면 예외가 발생한다.")
    void saveMatch_when_teamIds_size_is_not_2_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound savedRound = buildFreeGameRound(freeGame, 1);
        savedRound.setId(10L);

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L)) // 길이가 1이므로 예외 발생
                                                        .teamBIds(List.of(2L, 3L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 participantId가 포함되면 예외가 발생한다.")
    void saveMatch_when_participantId_not_found_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;
        Long invalidParticipantId = 100L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(existingRound));

        // 존재하지 않는 participantId만 비정상으로 처리하도록 stub
        when(gameParticipantRepository.findById(invalidParticipantId))
                .thenReturn(Optional.empty());

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(invalidParticipantId, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("teamAIds/teamBIds 요소가 null이어도 매치 저장이 가능하다.")
    void saveMatch_when_teamIds_contains_null_then_allow() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(existingRound));

        // 참가자 stub
        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);

        stubParticipantsExist(p1.getId(), p2.getId());

        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(null, 1L))
                                                        .teamBIds(Arrays.asList(null, 2L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        verify(freeGameMatchRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("하나의 라운드에서 동일 participantId가 중복되면 예외가 발생한다.")
    void saveMatch_when_participant_duplicated_in_round_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;
        Long duplicatedId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(existingRound));

        // participant 존재하도록 stub
        when(gameParticipantRepository.findById(duplicatedId))
                .thenReturn(Optional.of(mock(GameParticipant.class)));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(duplicatedId, 2L))
                                                        .teamBIds(Arrays.asList(3L, 4L))
                                                        .build(),
                                                MatchRequest.builder()
                                                        .courtNumber(2)
                                                        .teamAIds(Arrays.asList(duplicatedId, 6L))      // 같은 라운드 내 중복
                                                        .teamBIds(Arrays.asList(7L, 8L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("하나의 매치에서 동일 participantId가 중복되면 예외가 발생한다.")
    void saveMatch_when_participant_duplicated_in_match_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;
        Long duplicatedId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));
        when(gameParticipantRepository.findById(duplicatedId)).thenReturn(Optional.of(mock(GameParticipant.class)));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(duplicatedId, null))
                                                        .teamBIds(Arrays.asList(duplicatedId, null))  // 동일 매치 내 중복
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("게임에 속하지 않는 participantId가 포함되면 예외가 발생한다.")
    void saveMatch_when_participant_not_in_game_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;
        Long notInGameId = 100L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);
        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId))
                .thenReturn(List.of(existingRound));

        // 존재는 하지만 게임 소속이 아닌 participant
        stubParticipantsExist(notInGameId);

        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);

        // gameId에 속한 참가자 목록 stub
        when(gameParticipantRepository.findByFreeGameId(gameId)).thenReturn(List.of(p1, p2));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(notInGameId, 2L))   // 게임 소속이 아님
                                                        .teamBIds(Arrays.asList(null, null))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("요청한 participantId가 매치의 teamA/teamB로 저장된다.")
    void saveMatch_when_teamIds_then_match_players_are_set() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);

        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));

        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);
        GameParticipant p3 = mockGameParticipantWithId(3L);
        GameParticipant p4 = mockGameParticipantWithId(4L);

        when(gameParticipantRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(gameParticipantRepository.findById(2L)).thenReturn(Optional.of(p2));
        when(gameParticipantRepository.findById(3L)).thenReturn(Optional.of(p3));
        when(gameParticipantRepository.findById(4L)).thenReturn(Optional.of(p4));
        when(gameParticipantRepository.findByFreeGameId(gameId))
                .thenReturn(List.of(p1, p2, p3, p4));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(p1.getId(), p2.getId()))
                                                        .teamBIds(Arrays.asList(p3.getId(), p4.getId()))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when
        freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FreeGameMatch>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(freeGameMatchRepository).saveAll(captor.capture());

        List<FreeGameMatch> savedMatches = captor.getValue();
        assertThat(savedMatches).hasSize(1);

        FreeGameMatch saved = savedMatches.getFirst();
        assertThat(saved.getTeamAPlayer1()).isSameAs(p1);
        assertThat(saved.getTeamAPlayer2()).isSameAs(p2);
        assertThat(saved.getTeamBPlayer1()).isSameAs(p3);
        assertThat(saved.getTeamBPlayer2()).isSameAs(p4);
    }

    @ParameterizedTest
    @EnumSource(value = GameStatus.class, names = {"NOT_STARTED", "IN_PROGRESS"})
    @DisplayName("게임 상태가 NOT_STARTED/IN_PROGRESS이면 수정 가능하다")
    void updateRoundMatch_when_game_status_allowed_then_save(GameStatus status) {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        FreeGame freeGame = stubGameWithOrganizer(gameId, userId);
        FreeGameRound existingRound = buildFreeGameRound(freeGame, 1);
        existingRound.setId(10L);
        when(freeGameRoundRepository.findByFreeGameIdOrderByRoundNumber(gameId)).thenReturn(List.of(existingRound));

        GameParticipant p1 = mockGameParticipantWithId(1L);
        GameParticipant p2 = mockGameParticipantWithId(2L);
        GameParticipant p3 = mockGameParticipantWithId(3L);
        GameParticipant p4 = mockGameParticipantWithId(4L);

        stubParticipantsExist(1L, 2L, 3L, 4L);
        when(gameParticipantRepository.findByFreeGameId(gameId)).thenReturn(List.of(p1, p2, p3, p4));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(1L, 2L))
                                                        .teamBIds(Arrays.asList(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();
        // when
        assertThatCode(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .doesNotThrowAnyException();
        // then
        verify(freeGameMatchRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("게임 상태가 COMPLETED이면 수정할 수 없다.")
    void updateRoundMatch_when_game_status_completed_then_throw_exception() {
        // given
        Long gameId = 1L;
        Long userId = 1L;

        User organizer = mockUserWithId(userId);
        FreeGame freeGame = buildNewFreeGameWithStatus(gameId, organizer, GameStatus.COMPLETED);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(Arrays.asList(1L, 2L))
                                                        .teamBIds(Arrays.asList(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        assertThatThrownBy(() -> freeGameService.updateFreeGameRoundMatch(userId, gameId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("COMPLETED");
        verify(freeGameMatchRepository, never()).saveAll(anyList());
    }




    /*
    빌더 메서드
     */

    private User mockUserWithId(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private GameParticipant mockGameParticipantWithId(Long id) {
        GameParticipant participant = mock(GameParticipant.class);
        when(participant.getId()).thenReturn(id);
        return participant;
    }

    private FreeGame buildNewFreeGame(Long gameId, User organizer) {
        return FreeGame.builder()
                .id(gameId)
                .title("새로운 자유게임")
                .organizer(organizer)
                .gradeType(GradeType.REGIONAL)
                .gameType(GameType.FREE)    // 자유게임
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode("https://newfreegame-sharecode")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private FreeGame buildNewFreeGameWithStatus(Long gameId, User organizer, GameStatus gameStatus) {
        return FreeGame.builder()
                .id(gameId)
                .title("새로운 자유게임")
                .organizer(organizer)
                .gradeType(GradeType.REGIONAL)
                .gameType(GameType.FREE)    // 자유게임
                .gameStatus(gameStatus)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode("https://newfreegame-sharecode")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private FreeGameRound buildFreeGameRound(FreeGame freeGame, Integer roundNumber) {
        return FreeGameRound.builder()
                .freeGame(freeGame)
                .roundNumber(roundNumber)
                .roundStatus(RoundStatus.NOT_STARTED)
                .finishedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private void stubParticipantsExist(Long... ids) {
        for (Long id : ids) {
            when(gameParticipantRepository.findById(id))
                .thenReturn(Optional.of(mock(GameParticipant.class)));
        }
    }

    private FreeGame stubGameWithOrganizer(Long gameId, Long userId) {
        User organizer = mockUserWithId(userId);
        FreeGame freeGame = buildNewFreeGame(gameId, organizer);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(freeGame));
        return freeGame;
    }
}
