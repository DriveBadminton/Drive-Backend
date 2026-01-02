package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.dto.ParticipantCreateRequest;
import com.gumraze.drive.drive_backend.courtManager.entity.CourtGame;
import com.gumraze.drive.drive_backend.courtManager.repository.CourtGameRepository;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

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
    @DisplayName("자유게임 생성 성공")
    void createFreeGame() {
        // given: 유효한 요청 데이터를 전달하며, 운영자 유저가 존재한다고 가정함.
        // request = 운영자 1 + 참여자 1명
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .courtCount(1)
                .roundCount(1)
                .managerIds(List.of(1L))
                .participants(List.of(
                        ParticipantCreateRequest.builder()
                                .name("Kim")
                                .gender(Gender.MALE)
                                .grade(Grade.ROOKIE)
                                .ageGroup(30)
                                .build()
                ))
                .build();


        // 게임 저장 결과 stub
        CourtGame savedGame = new CourtGame(
                1L,
                request.getTitle(),
                null,
                GameType.FREE,
                GameStatus.NOT_STARTED,
                MatchRecordMode.STATUS_ONLY,
                null,
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