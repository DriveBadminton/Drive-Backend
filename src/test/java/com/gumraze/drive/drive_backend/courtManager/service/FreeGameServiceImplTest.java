package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.entity.CourtGame;
import com.gumraze.drive.drive_backend.courtManager.repository.CourtGameRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FreeGameServiceImplTest {
    @Mock
    CourtGameRepository courtGameRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    FreeGameServiceImpl freeGameService;

    @Test
    @DisplayName("필수 입력 값들이 모두 입력되면, 자유게임 생성 성공")
    void createFreeGame_withRequiredFields_returnsGameId() {
        // given: title, courtCount
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")        // title 입력
                .courtCount(1)            // courtCount 입력
                .build();


        // 게임 저장 결과 stub
        CourtGame savedGame = new CourtGame(
                1L,                     // gameId 1로 stub
                request.getTitle(),        // title 설정
                null,                      // 게임 생성 유저의 id
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

        // when: createFreeGame() 호출함.
        CreateFreeGameResponse createdGame = freeGameService.createFreeGame(request);

        // then: 반환값을 검증함.
        assertNotNull(createdGame);
        assertEquals(createdGame.getGameId(), savedGame.getId());
        // save가 호출되었는지 검증
        verify(courtGameRepository).save(any(CourtGame.class));
    }
}