package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.entity.CourtGame;
import com.gumraze.drive.drive_backend.courtManager.repository.CourtGameRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FreeGameServiceImpl implements FreeGameService {

    private final CourtGameRepository courtGameRepository;
    private final UserRepository userRepository;

    @Override
    public CreateFreeGameResponse createFreeGame(
            Long userId,
            CreateFreeGameRequest request
    ) {

        // 게임 생성자 규칙
        // 생성자는 우리 서비스의 사용자이어야함.
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("존재하지 않는 userId입니다. :" + userId);
        }

        // 게임 기록 형식 규칙
        // matchRecordMode가 null이면, 기본값으로 설정
        MatchRecordMode matchRecordMode = request.getMatchRecordMode();
        if (matchRecordMode == null) {
            matchRecordMode = MatchRecordMode.STATUS_ONLY;
        }

        // 관리자 규칙
        List<Long> managers = request.getManagerIds();
        if (managers != null) {
            // 추가 관리자가 2명 이상이면 예외 발생
            if (managers.size() > 2) {
                throw new IllegalArgumentException("managerIds는 최대 2명까지 가능합니다.");
            }
            
            // 자유게임 생성자는 manager list에는 포함되지 않음
            if (managers.contains(userId)) {
                throw new IllegalArgumentException("게임 생성자는 managerIds에 포함될 수 없습니다.");
            }
            for (Long managerId : managers) {
                if (!userRepository.existsById(managerId)) {
                    throw new IllegalArgumentException("존재하지 않는 managerId입니다. :" + managerId);
                }
            }
        }

        // 엔티티 생성
        CourtGame courtGame = new CourtGame(
                null,                       // id는 DB에서 생성
                request.getTitle(),
                null,                  // organizer 미구현
                GameType.FREE,
                GameStatus.NOT_STARTED,
                matchRecordMode,
                null,                   // share code 미구현
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // 저장
        CourtGame savedGame = courtGameRepository.save(courtGame);

        return CreateFreeGameResponse.from(savedGame);
    }
}
