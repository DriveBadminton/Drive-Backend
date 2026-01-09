package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.common.exception.ForbiddenException;
import com.gumraze.drive.drive_backend.common.exception.NotFoundException;
import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.courtManager.dto.*;
import com.gumraze.drive.drive_backend.courtManager.entity.FreeGameSetting;
import com.gumraze.drive.drive_backend.courtManager.entity.Game;
import com.gumraze.drive.drive_backend.courtManager.entity.GameParticipant;
import com.gumraze.drive.drive_backend.courtManager.repository.FreeGameSettingRepository;
import com.gumraze.drive.drive_backend.courtManager.repository.GameParticipantRepository;
import com.gumraze.drive.drive_backend.courtManager.repository.GameRepository;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import com.gumraze.drive.drive_backend.user.entity.User;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class FreeGameServiceImplTest {
    @Mock
    GameRepository gameRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    GameParticipantRepository gameParticipantRepository;

    @Mock
    FreeGameSettingRepository freeGameSettingRepository;

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

        // 사용자 검증
        User organizer = mock(User.class);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));

        // 게임 저장 결과 stub
        Game savedGame = new Game(
                1L,                     // gameId 1로 stub
                request.getTitle(),        // title 설정
                organizer,                      // 게임 생성 유저의 id
                GradeType.NATIONAL,
                GameType.FREE,             // 자유게임(기본값)
                GameStatus.NOT_STARTED,    // 시작전(기본값)
                MatchRecordMode.STATUS_ONLY,     // STATUS_ONLY 기본값
                null,                       // 공유 코드, 아직 없음
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // 생성한 게임 저장
        when(gameRepository.save(any(Game.class)))
                .thenReturn(savedGame);


        // when: createFreeGame() 호출함.
        CreateFreeGameResponse createdGame = freeGameService.createFreeGame(1L, request);

        // then: 반환값을 검증함.
        assertNotNull(createdGame);
        assertEquals(createdGame.getGameId(), savedGame.getId());
        // save가 호출되었는지 검증
        verify(gameRepository).save(any(Game.class));
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
        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 사용자 검증
        User organizer = mock(User.class);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));

        // when: createFreeGame() 호출함.
        freeGameService.createFreeGame(1L, request);

        // 내부 전달값 capture
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        // 내부 전달값 저장
        verify(gameRepository).save(captor.capture());

        Game savedGame = captor.getValue();

        // then
        // MatchRecordMode 검증
        assertEquals(savedGame.getMatchRecordMode(), MatchRecordMode.STATUS_ONLY);
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
        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 사용자 검증
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));

        // when: createFreeGame() 호출함.
        freeGameService.createFreeGame(1L, request);

        // 내부 전달값 capture
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        // 내부 전달값 저장
        verify(gameRepository).save(captor.capture());

        Game savedGame = captor.getValue();

        // then
        // MatchRecordMode 검증
        assertEquals(savedGame.getMatchRecordMode(), MatchRecordMode.RESULT);
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
        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 사용자 검증
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));

        // when: createFreeGame 호출했을 때
        freeGameService.createFreeGame(1L, request);

        // save가 호출되었는지 검증
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        // 내부 전달값 저장
        verify(gameRepository).save(captor.capture());
        Game savedGame = captor.getValue();

        // then: GameType과, GameStatus가 기본값 FREE, NOT_STARTED로 저장됨.
        assertEquals(savedGame.getGameType(), GameType.FREE);
        assertEquals(savedGame.getGameStatus(), GameStatus.NOT_STARTED);
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

        verify(gameRepository, never()).save(any());
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

        // stub
        User organizer = mock(User.class);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));

        when(gameRepository.save(any(Game.class)))
                .thenReturn(
                        new Game(
                                1L,
                                "자유게임",
                                organizer,
                                GradeType.REGIONAL,
                                GameType.FREE,
                                GameStatus.NOT_STARTED,
                                MatchRecordMode.STATUS_ONLY,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        )
                );
        when(gameParticipantRepository.save(any(GameParticipant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when: createFreeGame 호출 시
        freeGameService.createFreeGame(1L, request);

        // then: displayName이 다르게 설정되어야함.
        ArgumentCaptor<GameParticipant> captor = ArgumentCaptor.forClass(GameParticipant.class);

        verify(gameParticipantRepository, times(2)).save(captor.capture());

        List<GameParticipant> saved = captor.getAllValues();
        assertEquals("홍길동", saved.get(0).getDisplayName());
        assertEquals("홍길동A", saved.get(1).getDisplayName());
    }

    @Test
    @DisplayName("자유게임 생성 시, 참여자 목록이 있으면 참가자 저장을 호출한다.")
    void createFreeGame_withParticipants_callsSaveParticipants() {
        // given: 참여자 목록이 있는 게임을 생성함.
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(2)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("박지성")
                                        .gender(Gender.MALE)
                                        .grade(Grade.A)
                                        .ageGroup(20)
                                        .build(),
                                ParticipantCreateRequest.builder()
                                        .originalName("손흥민")
                                        .gender(Gender.MALE)
                                        .grade(Grade.A)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        // stub
        User organizer = mock(User.class);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(gameRepository.save(any(Game.class)))
                .thenReturn(
                        new Game(
                                1L,
                                "자유게임",
                                organizer,
                                GradeType.NATIONAL,
                                GameType.FREE,
                                GameStatus.NOT_STARTED,
                                MatchRecordMode.STATUS_ONLY,
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                        )
                );
        // when: 자유게임 생성이 호출되었을 때
        freeGameService.createFreeGame(1L, request);

        // then: 참가자 목록 저장이 호출됨
        verify(gameParticipantRepository, times(2)).save(any(GameParticipant.class));
    }

    @Test
    @DisplayName("자유게임 생성 시 organizerId가 저장된다")
    void createFreeGame_setsOrganizerId() {
        // given: 게임을 생성한 User가 organizerId에 등록됨.
        Long userId = 1L;

        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .build();

        // stub
        User organizer = mock(User.class);
        when(organizer.getId()).thenReturn(userId);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(organizer));
        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when: 자유게임을 생성했을 때
        freeGameService.createFreeGame(userId, request);

        // then: organizerId가 userId와 동일함.
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());

        Game saved = captor.getValue();
        assertSame(organizer, saved.getOrganizer());
        assertEquals(userId, saved.getOrganizer().getId());
    }

    @Test
    @DisplayName("자유게임 생성 시, free game setting 저장 호출")
    void createFreeGame_savesFreeGameSetting() {
        // given: 자유게임 생성 시, courtCount, roundCount를 저장이 되어야함.
        Long userId = 1L;
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .courtCount(2)
                .roundCount(3)
                .build();

        // stub
        User organizer = mock(User.class);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(organizer));

        Game savedGame = Game.builder()
                .title("자유게임")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(gameRepository.save(any(Game.class))).thenReturn(savedGame);

        // when
        freeGameService.createFreeGame(userId, request);

        // then
        ArgumentCaptor<FreeGameSetting> captor = ArgumentCaptor.forClass(FreeGameSetting.class);
        verify(freeGameSettingRepository).save(captor.capture());

        FreeGameSetting savedSetting = captor.getValue();
        assertEquals(request.getCourtCount(), savedSetting.getCourtCount());
        assertEquals(request.getRoundCount(), savedSetting.getRoundCount());
        assertEquals(savedGame, savedSetting.getGame());
    }

    @Test
    @DisplayName("자유게임 상세 조회 성공 시 기본 정보와 설정을 매핑하여 반환함")
    void getFreeGameDetail_success() {
        // given: 생성된 게임이 존재함.
        Long userId = 99L;
        Long gameId = 1L;
        User organizer = mock(User.class);
        when(organizer.getId()).thenReturn(99L);

        // entity
        Game game = buildGame(gameId, organizer);
        FreeGameSetting setting = buildSetting(game, 2, 3);

        // stub
        when(organizer.getId()).thenReturn(userId);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(freeGameSettingRepository.findByGameId(gameId)).thenReturn(Optional.of(setting));

        // when: getFreeGameDetail 호출 시 response가 채워짐
        FreeGameDetailResponse response = freeGameService.getFreeGameDetail(userId, gameId);

        // then
        assertEquals(gameId, response.getGameId());
        assertEquals(game.getTitle(), response.getTitle());
        assertEquals(game.getGameType(), response.getGameType());
        assertEquals(game.getGameStatus(), response.getGameStatus());
        assertEquals(game.getMatchRecordMode(), response.getMatchRecordMode());
        assertEquals(game.getGradeType(), response.getGradeType());
        assertEquals(setting.getCourtCount(), response.getCourtCount());
        assertEquals(setting.getRoundCount(), response.getRoundCount());
        assertEquals(game.getOrganizer().getId(), response.getOrganizerId());
        // shareCode는 제외
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 존재하지 않는 gameId면 예외 발생")
    void getFreeGameDetail_withUnknownGameId_throwsException() {
        // given
        Long userId = 1L;
        Long gameId = 99999L;

        // stub
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> freeGameService.getFreeGameDetail(userId, gameId));

        // 서비스가 gameRepository.findById를 호출했는지 검증
        verify(gameRepository).findById(gameId);

        // game이 없는 경우에 freeGameSettingRepository는 호출하지 않음
        verify(freeGameSettingRepository, never()).findByGameId(anyLong());
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 요청자가 생성자가 아니면 예외 발생")
    void getFreeGameDetail_withNotOrganizer_throwsForbidden() {
        // given
        Long userId = 1L;
        Long gameId = 1L;

        // entity
        User organizer = mock(User.class);
        Game game = buildGame(gameId, organizer);

        // stub
        when(organizer.getId()).thenReturn(99L);    // 요청자와 다름
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // when & then
        assertThrows(ForbiddenException.class, () -> freeGameService.getFreeGameDetail(userId, gameId));
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 성공 테스트")
    void updateFreeGameInfo_success() {
        // given: 수정된 정보
        UpdateFreeGameRequest request = buildUpdateFreeGameRequest();
        Long gameId = 1L;
        Long userId = 1L;
        User organizer = mock(User.class);
        when(organizer.getId()).thenReturn(userId);

        Game game = buildGame(gameId, organizer);

        // stub
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UpdateFreeGameResponse response = freeGameService.updateFreeGameInfo(userId, gameId, request);

        // then
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());

        Game savedGame = captor.getValue();
        assertEquals(gameId, savedGame.getId());
        assertEquals(request.getTitle(), savedGame.getTitle());
        assertEquals(request.getGradeType(), savedGame.getGradeType());
        assertEquals(request.getMatchRecordMode(), savedGame.getMatchRecordMode());
    }

    /*
    Builder 메서드
     */

    private UpdateFreeGameRequest buildUpdateFreeGameRequest() {
        return UpdateFreeGameRequest.builder()
                .title("수정된 게임 제목")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                //.managerIds(List.of(1L, 2L)) //TODO: 현재 매니저 관련 기능 미개발, 개발 완료 시 처리
                .build();
    }

    private Game buildGame(Long gameId, User organizer) {
        return Game.builder()
                .id(gameId)
                .title("자유게임")
                .organizer(organizer)
                .gradeType(GradeType.NATIONAL)
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .shareCode(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private FreeGameSetting buildSetting(Game game, int courtCount, int roundCount) {
        return FreeGameSetting.builder()
                .game(game)
                .courtCount(courtCount)
                .roundCount(roundCount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}