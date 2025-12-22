package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserGradeHistory;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.repository.JpaUserGradeHistoryRepository;
import com.gumraze.drive.drive_backend.user.repository.JpaUserProfileRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService{
    private final UserRepository userRepository;
    private final JpaUserProfileRepository jpaUserProfileRepository;
    private final JpaUserGradeHistoryRepository jpaUserGradeHistoryRepository;
    private final UserProfileValidator validator;
    private final RegionService regionService;

    @Override
    public void createProfile(Long userId, UserProfileCreateRequest request) {
        if (jpaUserProfileRepository.existsById(userId)) {
            throw new IllegalArgumentException("이미 프로필이 존재합니다.");
        }

        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자가 존재하지 않습니다.")
        );

        if (request.regionId() != null && !regionService.existsById(request.regionId())) {
            throw new IllegalArgumentException("지역이 존재하지 않습니다.");
        }

        validator.validateForCreate(request);

        // 닉네임 설정
        String resolvedNickname = request.nickname();
        if (resolvedNickname == null || resolvedNickname.isBlank()) {
            throw new IllegalArgumentException("nickname이 필요합니다.");
        }

        UserProfile profile = new UserProfile(
                userId,
                resolvedNickname,
                request.grade(),
                null
        );

        // grade 저장
        if (request.grade() != null) {
            jpaUserGradeHistoryRepository.save(
                    new UserGradeHistory(user, request.grade())
            );
        }

        // birth 파싱
        LocalDate birth = LocalDate.parse(
                request.birth(),
                DateTimeFormatter.BASIC_ISO_DATE.withLocale(Locale.KOREA));
        profile.setBirth(birth.atStartOfDay());

        // gender 세팅
        profile.setGender(request.gender());

        // user 상태 전환
        user.setStatus(UserStatus.ACTIVE);

        // user 저장
        jpaUserProfileRepository.save(profile);
    }

    @Override
    public void updateProfile(Long userId, UserProfileCreateRequest request) {

    }
}
