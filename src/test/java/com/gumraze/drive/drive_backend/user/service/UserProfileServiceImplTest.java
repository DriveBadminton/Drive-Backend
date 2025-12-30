package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.service.RegionService;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.UserRole;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.repository.UserGradeHistoryRepository;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserProfileRepository userProfileRepository;

    @Mock
    UserGradeHistoryRepository userGradeHistoryRepository;

    @Mock
    RegionService regionService;

    private UserProfileServiceImpl userProfileService;

    @Mock
    UserNicknameProvider userNicknameProvider;

    @BeforeEach
    void setUp() {
        userProfileService = new UserProfileServiceImpl(
                userRepository,
                userProfileRepository,
                new UserProfileValidator(),
                regionService,
                userNicknameProvider,
                userGradeHistoryRepository
        );
    }

    @Test
    @DisplayName("사용자 프로필이 이미 있으면, 새로운 프로필 생성은 실패함.")
    void create_profile_throws_when_profile_already_exists() {
        // given: userId에 해당하는 사용자가 이미 UserProfile을 가지고 있음.
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "kim",
                2L,
                Grade.A,
                Grade.A,
                "19980925",
                Gender.MALE
        );

        // jpa로 existsById(userId)가 호출되면 실제 DB를 조회하지 않고 true를 반환하도록 설정함.
        when(userProfileRepository.existsById(userId)).thenReturn(true);

        // when: 같은 userId로 createProfile을 호출하여 새로운 프로필 생성 시도
        assertThatThrownBy(() ->
                userProfileService.createProfile(userId, request))
                .isInstanceOf(IllegalArgumentException.class);

        // Then: 예외가 발생해서 생성이 실패함.
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(userGradeHistoryRepository);
    }

    @Test
    @DisplayName("사용자가 없으면 프로필 생성은 실패함")
        // user는 제3자 로그인 시 생성됨.
    void create_profile_throws_when_user_does_not_exist() {
        // given: 사용자
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "kim",
                2L,
                Grade.A,
                Grade.A,
                "19980925",
                Gender.MALE
        );

        // userId로 사용자 조회 -> 사용자가 없음
        when(userProfileRepository.existsById(userId)).thenReturn(false);
        // 재검증
        when(userRepository.findById(userId)).thenReturn(empty());

        // when & then: 프로필 생성 시도시 에러 발생
        assertThatThrownBy(() ->
                userProfileService.createProfile(userId, request)
        ).isInstanceOf(IllegalArgumentException.class);

        // then: DB에 저장된 값이 있는지 재검증
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(userGradeHistoryRepository);
    }

    @Test
    @DisplayName("regionId가 있는데 해당 지역이 없으면 프로필 생성은 실패함.")
    void create_profile_throws_when_region_does_not_exist() {
        // given: regionId가 false, userRepository. findById = user 리턴
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "kim",
                2L,
                Grade.A,
                Grade.A,
                "19980925",
                Gender.MALE
        );

        User user = new User(UserStatus.PENDING, UserRole.USER);

        when(userProfileRepository.existsById(userId))
                .thenReturn(false);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userProfileService.createProfile(userId, request))
                .isInstanceOf(IllegalArgumentException.class);

        // then
        verify(userProfileRepository, never()).save(any());
        verifyNoInteractions(userGradeHistoryRepository);
    }

    @Test
    @DisplayName("정상 요청이면 프로필이 저장됨")
    void create_and_save_user_profile_if_request_is_ok() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "kim",
                2L,
                Grade.A,
                Grade.A,
                "19980925",
                Gender.MALE
        );

        // 사용자 객체 생성
        User user = new User(UserStatus.PENDING, UserRole.USER);
        when(userProfileRepository.existsById(userId))
                .thenReturn(false);      // 사용자가 존재하는지 확인

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user)); // 사용자를 조회하면 가짜 객체를 반환

        // mock 지역 객체 생성
        RegionDistrict district = mock(RegionDistrict.class);
        when(regionService.findDistrictsById(2L))
                .thenReturn(Optional.of(district));

        // when
        userProfileService.createProfile(userId, request);

        // then
        verify(userProfileRepository).save(any());
    }

    @Test
    @DisplayName("정상 요청이면 사용자 상태가 ACTIVE로 전환됨")
    void create_profile_activates_user_when_request_is_ok() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "kim",
                2L,
                Grade.A,
                Grade.A,
                "19980925",
                Gender.MALE
        );

        User user = new User(UserStatus.PENDING, UserRole.USER);
        when(userProfileRepository.existsById(userId))
                .thenReturn(false);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Mockito로 RegionDistrict 객체 생성
        RegionDistrict district = mock(RegionDistrict.class);       // .class는 타입 자체를 가리키는 Class 객체임
        when(regionService.findDistrictsById(2L))
                .thenReturn(Optional.of(district));

        // when
        userProfileService.createProfile(userId, request);

        // then
        assertThat(user.getStatus())
                .isEqualTo(UserStatus.ACTIVE);

    }

    @Test
    @DisplayName("Grade가 있으면 등급 히스토리가 저장됨")
    void create_profile_saves_grade_history_when_grade_is_given() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "kim",
                2L,
                Grade.A,
                null,
                "19980925",
                Gender.MALE
        );

        User user = new User(UserStatus.PENDING, UserRole.USER);
        when(userProfileRepository.existsById(userId))
                .thenReturn(false);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        RegionDistrict district = mock(RegionDistrict.class);
        when(regionService.findDistrictsById(2L))
                .thenReturn(Optional.of(district));

        // when
        userProfileService.createProfile(userId, request);

        // then
        verify(userGradeHistoryRepository).save(any());
    }

    @Test
    @DisplayName("grade가 null이면 등급 히스토리를 저장하지 않음")
    void create_profile_does_not_save_grade_history_when_grade_is_null() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "kim",
                2L,
                null,
                null,
                "19980925",
                Gender.MALE
        );

        User user = new User(UserStatus.PENDING, UserRole.USER);
        when(userProfileRepository.existsById(userId))
                .thenReturn(false);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        RegionDistrict district = mock(RegionDistrict.class);
        when(regionService.findDistrictsById(2L))
                .thenReturn(Optional.of(district));

        // when
        userProfileService.createProfile(userId, request);

        // then
        verify(userGradeHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("정상 요청이면 birth/gender가 프로필에 세팅되어 저장됨")
    void create_profile_sets_birth_and_gender_before_save() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "kim",
                2L,
                Grade.A,
                Grade.A,
                "19980925",
                Gender.MALE
        );

        User user = new User(UserStatus.PENDING, UserRole.USER);
        when(userProfileRepository.existsById(userId))
                .thenReturn(false);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        RegionDistrict district = mock(RegionDistrict.class);
        when(regionService.findDistrictsById(2L))
                .thenReturn(Optional.of(district));

        // when
        userProfileService.createProfile(userId, request);

        // then
        verify(userProfileRepository).save(
                argThat(profile ->
                        profile.getBirth().toLocalDate().equals(LocalDate.of(1998, 9, 25))
                                && profile.getGender() == Gender.MALE));
    }

    @Test
    @DisplayName("요청 nickname이 없으면 프로필 생성은 실패한다.")
    void create_profile_throws_when_request_nickname_is_null() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                null,
                2L,
                Grade.A,
                Grade.A,
                "19980925",
                Gender.MALE
        );

        User user = new User(UserStatus.PENDING, UserRole.USER);
        when(userProfileRepository.existsById(userId))
                .thenReturn(false);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userProfileService.createProfile(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nickname이 필요합니다.");
        verify(userProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("요청 nickname이 있으면 DB nickname이 있어도 요청 nickname을 우선한다.")
    void create_profile_uses_request_nickname_when_request_nickname_is_present() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request = new UserProfileCreateRequest(
                "requestNick",
                2L,
                Grade.A,
                Grade.A,
                "19980925",
                Gender.MALE
        );

        User user = new User(UserStatus.PENDING, UserRole.USER);
        when(userProfileRepository.existsById(userId))
                .thenReturn(false);
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        RegionDistrict district = mock(RegionDistrict.class);
        when(regionService.findDistrictsById(2L))
                .thenReturn(Optional.of(district));

        // when
        userProfileService.createProfile(userId, request);

        // then
        verify(userProfileRepository).save(argThat(profile ->
                profile.getNickname().equals("requestNick")));
    }
}