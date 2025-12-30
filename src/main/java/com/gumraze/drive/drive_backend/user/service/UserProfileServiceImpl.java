package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.service.RegionService;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import com.gumraze.drive.drive_backend.user.dto.UserProfilePrefillResponseDto;
import com.gumraze.drive.drive_backend.user.dto.UserProfileResponseDto;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserGradeHistory;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.repository.UserGradeHistoryRepository;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService{
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileValidator validator;
    private final RegionService regionService;
    private final UserNicknameProvider userNicknameProvider;
    private final UserGradeHistoryRepository userGradeHistoryRepository;

    /**
     * Creates and persists a user profile for the given user, saves regional and national grade history entries when provided,
     * sets the profile's birth and gender, and marks the associated user as ACTIVE.
     *
     * @param userId  the identifier of the user for whom the profile will be created
     * @param request the profile creation request containing nickname, district id, birth, gender, and grade information
     * @throws IllegalArgumentException if a profile already exists for the user, the user does not exist, the district cannot be resolved,
     *                                  the nickname is null or blank, or any validator check fails
     */
    @Override
    public void createProfile(Long userId, UserProfileCreateRequest request) {
        if (userProfileRepository.existsById(userId)) {
            throw new IllegalArgumentException("이미 프로필이 존재합니다.");
        }

        // 사용자 조회
        User user = userRepository.findById(userId).orElseThrow(() ->
                new IllegalArgumentException("사용자가 존재하지 않습니다.")
        );

        // 사용자가 존재하면 request 검증 수행
        validator.validateForCreate(request);

        // 지역 조회
        Optional<RegionDistrict> regionDist = Optional.ofNullable(regionService.findDistrictsById(request.districtId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다.")));


        // 닉네임 설정
        String resolvedNickname = request.nickname();
        if (resolvedNickname == null || resolvedNickname.isBlank()) {
            throw new IllegalArgumentException("nickname이 필요합니다.");
        }

        Grade regional = request.regionalGrade();
        Grade national = request.nationalGrade();

        UserProfile profile = new UserProfile(
                userId,
                resolvedNickname,
                regional,
                national,
                regionDist.get()
        );

        // grade 저장
        if (regional != null) {
            userGradeHistoryRepository.save(
                    new UserGradeHistory(user, regional, GradeType.REGIONAL)
            );
        }
        if (national != null) {
            userGradeHistoryRepository.save(
                    new UserGradeHistory(user, national, GradeType.NATIONAL)
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
        userProfileRepository.save(profile);
    }

    @Override
    public void updateProfile(Long userId, UserProfileCreateRequest request) {

    }

    @Override
    public UserProfilePrefillResponseDto getProfilePrefill(Long userId) {
        Optional<String> nickname = userNicknameProvider.findNicknameByUserId(userId);
        return new UserProfilePrefillResponseDto(nickname.orElse(null), nickname.isPresent());
    }

    @Override
    public UserProfileResponseDto getMyProfile(Long userId) {

        // 사용자 조회, 없으면 실패 처리
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 프로필 조회, 없으면 null 반환
        UserProfile profile = null;
        if (user.getStatus() == UserStatus.ACTIVE) {
            profile = userProfileRepository.findByUserId(userId).orElse(null);
        }

        // 사용자 상태 + 프로필 정보가 있으면 DTO로 반환
        return UserProfileResponseDto.from(user, profile);
    }
}