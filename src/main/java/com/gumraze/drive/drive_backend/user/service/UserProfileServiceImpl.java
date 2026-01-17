package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.common.exception.NotFoundException;
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
import com.gumraze.drive.drive_backend.user.entity.UserProfileUpdateRequest;
import com.gumraze.drive.drive_backend.user.repository.UserGradeHistoryRepository;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private static final String TAG_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final SecureRandom secureRandom = new SecureRandom();

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
        RegionDistrict regionDistrict = regionService.findDistrictsById(request.getDistrictId())
                .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));

        // 닉네임 설정
        String resolvedNickname = request.getNickname();
        if (resolvedNickname == null || resolvedNickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수 입력 항목입니다.");
        }

        Grade regional = request.getRegionalGrade();
        Grade national = request.getNationalGrade();

        UserProfile profile =
                UserProfile.builder()
                        .user(user)
                        .nickname(resolvedNickname)
                        .regionDistrict(regionDistrict)
                        .regionalGrade(regional)
                        .nationalGrade(national)
                        .build();

        profile.setTag(generateTag());
        profile.setTagChangedAt(LocalDateTime.now());

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
                request.getBirth(),
                DateTimeFormatter.BASIC_ISO_DATE.withLocale(Locale.KOREA));
        profile.setBirth(birth.atStartOfDay());

        // gender 세팅
        profile.setGender(request.getGender());

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
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("사용자를 찾을 수 없습니다.")
        );

        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow(
                () -> new NotFoundException("사용자의 프로필을 찾을 수 없습니다.")
        );

        return UserProfileResponseDto.builder()
                        .status(user.getStatus())
                        .nickname(profile.getNickname())
                        .tag(profile.getTag())
                        .profileImageUrl(profile.getProfileImageUrl())
                        .birth(profile.getBirth())
                        .birthVisible(profile.isBirthVisible())
                        .gender(profile.getGender())
                        .regionalGrade(profile.getRegionalGrade())
                        .nationalGrade(profile.getNationalGrade())
                        .districtName(profile.getRegionDistrict().getName())
                        .provinceName(profile.getRegionDistrict().getProvince().getName())
                        .tagChangedAt(profile.getTagChangedAt())
                        .createdAt(profile.getCreatedAt())
                        .updatedAt(profile.getUpdatedAt())
                        .build();
    }

    @Override
    public void updateMyProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("사용자의 프로필을 찾을 수 없습니다."));

        if (request.getRegionalGrade() != null) {
            profile.setRegionalGrade(request.getRegionalGrade());
        }

        if (request.getNationalGrade() != null) {
            profile.setNationalGrade(request.getNationalGrade());
        }

        if (request.getBirth() != null) {
            LocalDate birth = LocalDate.parse(
                    request.getBirth(),
                    DateTimeFormatter.BASIC_ISO_DATE.withLocale(Locale.KOREA)
            );
            profile.setBirth(birth.atStartOfDay());
        }

        Boolean birthVisible = request.getBirthVisible();
        if (birthVisible != null) {
            profile.setBirthVisible(birthVisible);
        }

        if (request.getDistrictId() != null) {
            RegionDistrict regionDistrict =
                    regionService.findDistrictsById(request.getDistrictId())
                            .orElseThrow(() -> new IllegalArgumentException("지역이 존재하지 않습니다."));
            profile.setRegionDistrict(regionDistrict);
        }

        if (request.getProfileImageUrl() != null) {
            profile.setProfileImageUrl(request.getProfileImageUrl());
        }

        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }

        profile.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(profile);
    }

    // Helper Method
    private String generateTag() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int idx = secureRandom.nextInt(TAG_CHARS.length());
            sb.append(TAG_CHARS.charAt(idx));
        }
        return sb.toString();
    }
}