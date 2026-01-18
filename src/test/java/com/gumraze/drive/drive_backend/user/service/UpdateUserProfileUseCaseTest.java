package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.common.exception.ConflictException;
import com.gumraze.drive.drive_backend.common.exception.UnprocessableEntityException;
import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.service.RegionService;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserProfileIdentityUpdateRequest;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.entity.UserProfileUpdateRequest;
import com.gumraze.drive.drive_backend.user.repository.UserGradeHistoryRepository;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateUserProfileUseCaseTest {

    @Mock UserProfileRepository userProfileRepository;
    @Mock UserRepository userRepository;
    @Mock RegionService regionService;
    @Mock UserGradeHistoryRepository userGradeHistoryRepository;
    @Mock UserNicknameProvider userNicknameProvider;

    @Spy UserProfileValidator validator = new UserProfileValidator();

    @InjectMocks UserProfileServiceImpl userProfileService;

    @Test
    @DisplayName("프로필 기본 정보 부분 수정 성공")
    void update_profile_updates_only_requested_fields() {
        // given
        Long userId = 1L;
        UserProfileUpdateRequest request =
                UserProfileUpdateRequest.builder()
                        .regionalGrade(Grade.B)
                        .nationalGrade(Grade.B)
                        .birth("19980925")
                        .gender(Gender.MALE)
                        .profileImageUrl("http://localhost:8080/profile.jpg")
                        .districtId(2L)
                        .build();

        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .build();

        RegionDistrict oldDistrict = mock(RegionDistrict.class);
        RegionDistrict newDistrict = mock(RegionDistrict.class);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname("oldNickname")
                .tag("AB12")
                .regionalGrade(Grade.D)
                .nationalGrade(Grade.D)
                .birth(LocalDate.of(1998, 9, 25).atStartOfDay())
                .birthVisible(true)
                .gender(Gender.MALE)
                .profileImageUrl("http://old-profile-image.com")
                .regionDistrict(oldDistrict)
                .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(regionService.findDistrictsById(2L)).thenReturn(Optional.of(newDistrict));

        // when
        userProfileService.updateMyProfile(userId, request);

        // then
        assertThat(profile.getRegionalGrade()).isEqualTo(Grade.B);
        assertThat(profile.getNationalGrade()).isEqualTo(Grade.B);
        assertThat(profile.getBirth()).isEqualTo(LocalDate.of(1998, 9, 25).atStartOfDay());
        assertThat(profile.getGender()).isEqualTo(Gender.MALE);
        assertThat(profile.getProfileImageUrl()).isEqualTo("http://localhost:8080/profile.jpg");
        assertThat(profile.getRegionDistrict()).isEqualTo(newDistrict);

        // 요청에 없는 필드는 유지되는지 확인
        assertThat(profile.isBirthVisible()).isTrue();
        assertThat(profile.getTag()).isEqualTo("AB12");
        assertThat(profile.getNickname()).isEqualTo("oldNickname");
    }

    @Test
    @DisplayName("birth 형식 오류 테스트")
    void update_profile_throws_when_birth_format_invalid() {
        // given
        Long userId = 1L;

        User user = User.builder().id(userId).build();
        UserProfile profile = UserProfile.builder().user(user).build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .birth("invalid-date")
                .build();

        // when & then
        assertThatThrownBy(() -> userProfileService.updateMyProfile(userId, request))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    @DisplayName("사용자 닉네임 수정 성공 테스트")
    void update_nickname_success_test() {
        // given
        Long userId = 1L;

        User user = User.builder().id(userId).build();
        UserProfile profile = UserProfile.builder()
                .nickname("oldNickname")
                .tag("AB12")
                .tagChangedAt(LocalDateTime.now().minusDays(90))
                .user(user).build();

        UserProfileIdentityUpdateRequest request =
                UserProfileIdentityUpdateRequest.builder()
                        .nickname("newNickname")
                        .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // when
        userProfileService.updateNicknameAndTags(userId, request);

        // then
        assertThat(profile.getNickname()).isEqualTo("newNickname");
        assertThat(profile.getTag()).isEqualTo("AB12");
    }

    @Test
    @DisplayName("사용자 태그 수정 성공 테스트")
    void update_tag_success_test() {
        // given
        Long userId = 1L;

        User user = User.builder().id(userId).build();
        UserProfile profile = UserProfile.builder()
                .nickname("oldNickname")
                .tag("AB12")
                .tagChangedAt(LocalDateTime.now().minusDays(91))
                .user(user).build();

        UserProfileIdentityUpdateRequest request =
                UserProfileIdentityUpdateRequest.builder()
                        .tag("SON7")
                        .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.findByNicknameAndTag(profile.getNickname(), request.getTag()))
                .thenReturn(Optional.empty());

        // when
        userProfileService.updateNicknameAndTags(userId, request);

        // then
        assertThat(profile.getTag()).isEqualTo("SON7");
        assertThat(profile.getTagChangedAt()).isAfter(LocalDateTime.now().minusDays(90));
    }

    @Test
    @DisplayName("사용자 닉네임 + 태그 수정 성공 테스트")
    void update_nickname_and_tag_success_test() {
        // given
        Long userId = 1L;

        User user = User.builder().id(userId).build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname("oldNickname")
                .tag("AB12")
                .tagChangedAt(LocalDateTime.now().minusDays(91))
                .build();

        UserProfileIdentityUpdateRequest request =
                UserProfileIdentityUpdateRequest.builder()
                        .nickname("newNickname")
                        .tag("SON7")
                        .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.findByNicknameAndTag("newNickname", "SON7"))
                .thenReturn(Optional.empty());

        // when
        userProfileService.updateNicknameAndTags(userId, request);

        // then
        assertThat(profile.getNickname()).isEqualTo("newNickname");
        assertThat(profile.getTag()).isEqualTo("SON7");
        assertThat(profile.getTagChangedAt()).isAfter(LocalDateTime.now().minusDays(90));
    }

    @Test
    @DisplayName("사용자 닉네임 + 태그 중복 실패 테스트")
    void update_nickname_and_tag_duplicate_fail_test() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname("oldNickname")
                .tag("AB12")
                .tagChangedAt(LocalDateTime.now().minusDays(91))
                .build();

        UserProfile otherProfile = UserProfile.builder()
                .user(User.builder().id(2L).build())
                .nickname("otherNickname")
                .tag("SON7")
                .build();

        UserProfileIdentityUpdateRequest request =
                UserProfileIdentityUpdateRequest.builder()
                        .nickname("otherNickname")
                        .tag("SON7")
                        .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileRepository.findByNicknameAndTag(otherProfile.getNickname(), otherProfile.getTag()))
                .thenReturn(Optional.of(otherProfile));

        assertThatThrownBy(() -> userProfileService.updateNicknameAndTags(userId, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("태그 변경 90일 미경과 시, 태그 변경 실패 테스트")
    void update_tag_fail_test() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).build();
        LocalDateTime lastChangedAt = LocalDateTime.now().minusDays(30);

        UserProfile profile = UserProfile.builder()
                .nickname("oldNickname")
                .tag("AB12")
                .tagChangedAt(lastChangedAt)
                .user(user).build();

        UserProfileIdentityUpdateRequest request =
                UserProfileIdentityUpdateRequest.builder()
                        .tag("SON7")
                        .build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // when & then
        assertThatThrownBy(() -> userProfileService.updateNicknameAndTags(userId, request))
                .isInstanceOf(UnprocessableEntityException.class);
    }
}
