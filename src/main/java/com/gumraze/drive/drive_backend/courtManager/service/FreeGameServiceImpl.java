package com.gumraze.drive.drive_backend.courtManager.service;

import com.gumraze.drive.drive_backend.courtManager.constants.GameStatus;
import com.gumraze.drive.drive_backend.courtManager.constants.GameType;
import com.gumraze.drive.drive_backend.courtManager.constants.MatchRecordMode;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.dto.ParticipantCreateRequest;
import com.gumraze.drive.drive_backend.courtManager.entity.FreeGameSetting;
import com.gumraze.drive.drive_backend.courtManager.entity.Game;
import com.gumraze.drive.drive_backend.courtManager.entity.GameParticipant;
import com.gumraze.drive.drive_backend.courtManager.repository.FreeGameSettingRepository;
import com.gumraze.drive.drive_backend.courtManager.repository.GameParticipantRepository;
import com.gumraze.drive.drive_backend.courtManager.repository.GameRepository;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class FreeGameServiceImpl implements FreeGameService {

    private final GameRepository gameRepository;
    private final GameParticipantRepository gameParticipantRepository;
    private final FreeGameSettingRepository freeGameSettingRepository;
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

        // organizer 조회
        User organizerId = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId입니다. :" + userId));

        // 게임 정보 엔티티 생성
        Game game = Game.builder()
                .title(request.getTitle())
                .organizer(organizerId)
                .gradeType(request.getGradeType())
                // 기본값으로 FREE로 설정
                // TODO: 추후 tournament 등 여러 게임 생성 예정
                .gameType(GameType.FREE)
                // 처음 생성 이전 항상 시작 전
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(matchRecordMode)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 게임 기본 정보 우선 저장
        Game savedGame = gameRepository.save(game);

        // 자유게임 설정 저장
        FreeGameSetting freeGameSetting = FreeGameSetting.builder()
                .game(savedGame)
                .courtCount(request.getCourtCount())
                .roundCount(request.getRoundCount())
                .build();

        freeGameSettingRepository.save(freeGameSetting);

        // 참가자가 있는 경우, 참가자 정보 저장
        // 참가자 규칙
        List<ParticipantCreateRequest> participants = request.getParticipants();
        if (participants != null) {
            Map<ParticipantKey, Integer> duplicateCount = new HashMap<>();

            for (ParticipantCreateRequest participant : participants) {
                ParticipantKey key = new ParticipantKey(
                        participant.getOriginalName(),
                        participant.getGender(),
                        participant.getGrade(),
                        participant.getAgeGroup()
                );

                int count = duplicateCount.getOrDefault(key, 0);
                String displayName = (count == 0)
                        ? participant.getOriginalName()
                        : participant.getOriginalName() + suffix(count);

                duplicateCount.put(key, count + 1);

                User participantUser = null;
                if (participant.getUserId() != null) {
                    participantUser = userRepository.findById(participant.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 userId입니다." + participant.getUserId())
                            );
                }

                GameParticipant toSave = GameParticipant.builder()
                        .game(savedGame)
                        .user(participantUser)
                        .originalName(participant.getOriginalName())
                        .displayName(displayName)
                        .gender(participant.getGender())
                        .grade(participant.getGrade())
                        .ageGroup(participant.getAgeGroup())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                gameParticipantRepository.save(toSave);
            }
        }
        return CreateFreeGameResponse.from(savedGame);
    }

    private String suffix(int count) {
        return String.valueOf((char) ('A' + count - 1));
    }

    private record ParticipantKey(
            String originalName,
            Gender gender,
            Grade grade,
            Integer ageGroup
    ) {
    }
}
