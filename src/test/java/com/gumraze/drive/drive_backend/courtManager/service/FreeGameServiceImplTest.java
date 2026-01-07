package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.dto.ParticipantCreateRequest;
import com.gumraze.drive.drive_backend.courtManager.entity.CourtGame;
import com.gumraze.drive.drive_backend.courtManager.entity.CourtGameParticipant;
import com.gumraze.drive.drive_backend.courtManager.repository.CourtGameParticipantRepository;
import com.gumraze.drive.drive_backend.courtManager.repository.CourtGameRepository;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FreeGameServiceImplTest {
    @Mock
    CourtGameRepository courtGameRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CourtGameParticipantRepository courtGameParticipantRepository;

    @InjectMocks
    FreeGameServiceImpl freeGameService;

    @Test
    @DisplayName("자유게임 생성 성공 시 gameId 반환")
    void createFreeGame_success_returnsGameId() {
        // given: title, courtCount
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")        // title 입력
                .courtCount(1)            // courtCount 입력
                .roundCount(1)
                .build();


        // 게임 저장 결과 stub
        CourtGame savedGame = new CourtGame(
                1L,                     // gameId 1로 stub
                request.getTitle(),        // title 설정
                null,                      // 게임 생성 유저의 id
                GradeType.NATIONAL,
                GameType.FREE,             // 자유게임(기본값)
                GameStatus.NOT_STARTED,    // 시작전(기본값)
                MatchRecordMode.STATUS_ONLY,     // STATUS_ONLY 기본값
                null,                       // 공유 코드, 아직 없음
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // 생성한 게임 저장
        when(courtGameRepository.save(any(CourtGame.class)))
                .thenReturn(savedGame);

        // 사용자 검증
        when(userRepository.existsById(1L)).thenReturn(true);

        // when: createFreeGame() 호출함.
        CreateFreeGameResponse createdGame = freeGameService.createFreeGame(1L, request);

        // then: 반환값을 검증함.
        assertNotNull(createdGame);
        assertEquals(createdGame.getGameId(), savedGame.getId());
        // save가 호출되었는지 검증
        verify(courtGameRepository).save(any(CourtGame.class));
    }

    @Test
    @DisplayName("MatchRecordMode가 null 일시, 기본값인 STATUS_ONLY로 설정됨.")
    void createFreeGame_withNoMatchRecordMode_returnsStatusOnly() {
        // given: title, courtCount만 입력되고, matchRecordMode는 입력되지 않음.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .courtCount(1)
                .build();

        // 저장값 stub
        when(courtGameRepository.save(any(CourtGame.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 사용자 검증
        when(userRepository.existsById(1L)).thenReturn(true);

        // when: createFreeGame() 호출함.
        freeGameService.createFreeGame(1L, request);

        // 내부 전달값 capture
        ArgumentCaptor<CourtGame> captor = ArgumentCaptor.forClass(CourtGame.class);
        // 내부 전달값 저장
        verify(courtGameRepository).save(captor.capture());

        CourtGame savedCourtGame = captor.getValue();

        // then
        // MatchRecordMode 검증
        assertEquals(savedCourtGame.getMatchRecordMode(), MatchRecordMode.STATUS_ONLY);
    }

    @Test
    @DisplayName("matchRecordMode가 RESULT일시 그대로 저장됨.")
    void createFreeGame_withResultMatchRecordMode_returnsResult() {
        // given: title, courtCount만 입력되고, matchRecordMode는 입력되지 않음.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .courtCount(1)
                .roundCount(1)
                .matchRecordMode(MatchRecordMode.RESULT)
                .build();

        // 저장값 stub
        when(courtGameRepository.save(any(CourtGame.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 사용자 검증
        when(userRepository.existsById(1L)).thenReturn(true);

        // when: createFreeGame() 호출함.
        freeGameService.createFreeGame(1L, request);

        // 내부 전달값 capture
        ArgumentCaptor<CourtGame> captor = ArgumentCaptor.forClass(CourtGame.class);
        // 내부 전달값 저장
        verify(courtGameRepository).save(captor.capture());

        CourtGame savedCourtGame = captor.getValue();

        // then
        // MatchRecordMode 검증
        assertEquals(savedCourtGame.getMatchRecordMode(), MatchRecordMode.RESULT);
    }

    @Test
    @DisplayName("GameType과 GameStatus가 기본값 FREE, NOT_STARTED로 저장됨.")
    void createFreeGame_withDefaultGameTypeAndStatus_returnsDefault() {
        // given: 필수 입력값들만 입력이 됨.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .courtCount(1)
                .roundCount(1)
                .build();

        // 저장값 stub
        when(courtGameRepository.save(any(CourtGame.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 사용자 검증
        when(userRepository.existsById(1L)).thenReturn(true);

        // when: createFreeGame 호출했을 때
        freeGameService.createFreeGame(1L, request);

        // save가 호출되었는지 검증
        ArgumentCaptor<CourtGame> captor = ArgumentCaptor.forClass(CourtGame.class);
        // 내부 전달값 저장
        verify(courtGameRepository).save(captor.capture());
        CourtGame savedCourtGame = captor.getValue();

        // then: GameType과, GameStatus가 기본값 FREE, NOT_STARTED로 저장됨.
        assertEquals(savedCourtGame.getGameType(), GameType.FREE);
        assertEquals(savedCourtGame.getGameStatus(), GameStatus.NOT_STARTED);
    }

    @Test
    @DisplayName("managerIds가 2명 초과이면 예외 발생")
    void createFreeGame_withTooManyManagers_throwsException() {
        // given: managerIds가 3명인 자유게임 생성 요청
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .courtCount(1)
                .roundCount(1)
                .managerIds(List.of(1L, 2L, 3L))
                .build();

        // when & then: createFreeGame을 요청 시, IllegalArgumentException 발생
        // IllegalArgumentException -> type은 일치하나 값이 틀린 경우의 예외
        assertThrows(IllegalArgumentException.class, () -> freeGameService.createFreeGame(1L, request));
    }

    @Test
    @DisplayName("manager가 서비스 사용자가 아니면 예외 발생 테스트")
    void createFreeGame_withUnknownManagerId_throwsException() {
        // given
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .courtCount(1)
                .roundCount(1)
                .managerIds(List.of(2L))
                .build();

        // 게임 생성자 stub
        when(userRepository.existsById(anyLong())).thenReturn(true);

        // manager stub
        when(userRepository.existsById(2L)).thenReturn(false);

        // when & then: createFreeGame을 요청 시, IllegalArgumentException 발생
        assertThrows(IllegalArgumentException.class,
                () -> freeGameService.createFreeGame(1L, request));

        verify(courtGameRepository, never()).save(any());
    }

    @Test
    @DisplayName("동일 정보를 가진 참가자는 displayName을 접미사로 구분함.")
    void createFreeGame_withDuplicateParticipantDisplayName() {
        // given: 두 참가자의 기본 정보가 동일함.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .courtCount(1)
                .roundCount(1)
                // 두 참가자가 동일 정보를 가지고 있음.
                .participants(List.of(
                        ParticipantCreateRequest.builder()
                                .originalName("홍길동")
                                .gender(Gender.MALE)
                                .grade(Grade.A)
                                .ageGroup(20)
                                .build(),
                        ParticipantCreateRequest.builder()
                                .originalName("홍길동")
                                .gender(Gender.MALE)
                                .grade(Grade.A)
                                .ageGroup(20)
                                .build()
                ))
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(courtGameRepository.save(any(CourtGame.class)))
                .thenReturn(
                        new CourtGame(
                                1L,
                                "자유게임",
                                null,
                                GradeType.REGIONAL,
                                GameType.FREE,
                                GameStatus.NOT_STARTED,
                                MatchRecordMode.STATUS_ONLY,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        )
                );
        when(courtGameParticipantRepository.save(any(CourtGameParticipant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when: createFreeGame 호출 시
        freeGameService.createFreeGame(1L, request);

        // then: displayName이 다르게 설정되어야함.
        ArgumentCaptor<CourtGameParticipant> captor = ArgumentCaptor.forClass(CourtGameParticipant.class);

        verify(courtGameParticipantRepository, times(2)).save(captor.capture());

        List<CourtGameParticipant> saved = captor.getAllValues();
        assertEquals("홍길동", saved.get(0).getDisplayName());
        assertEquals("홍길동A", saved.get(1).getDisplayName());
    }
}