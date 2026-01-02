package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.entity.CourtGame;
import com.gumraze.drive.drive_backend.courtManager.repository.CourtGameRepository;

import java.time.LocalDateTime;

public class FreeGameServiceImpl implements FreeGameService {

    private final CourtGameRepository courtGameRepository;

    public FreeGameServiceImpl(CourtGameRepository courtGameRepository) {
        this.courtGameRepository = courtGameRepository;
    }

    @Override
    public CreateFreeGameResponse createFreeGame(CreateFreeGameRequest request) {
        // 엔티티 생성
        CourtGame courtGame = new CourtGame(
                null,                       // id는 DB에서 생성
                request.getTitle(),
                null,                  // organizer 미구현
                GameType.FREE,
                GameStatus.NOT_STARTED,
                request.getMatchRecordMode(),
                null,                   // share code 미구현
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // 저장
        CourtGame savedGame = courtGameRepository.save(courtGame);

        return CreateFreeGameResponse.from(savedGame);
    }
}
