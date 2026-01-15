package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.service.RegionService;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.repository.UserGradeHistoryRepository;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateUserProfileUseCaseTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserProfileRepository userProfileRepository;

    @Mock
    RegionService regionService;

    @Mock
    UserNicknameProvider userNicknameProvider;

    @Mock
    UserGradeHistoryRepository userGradeHistoryRepository;

    @Mock
    UserProfileValidator validator = new UserProfileValidator();

    @InjectMocks
    UserProfileServiceImpl userProfileService;

    @Test
    @DisplayName("프로필 생성 시, 정상 요청이면 태그가 자동 생성되고 tagChangedAt이 설정됨")
    void create_profile_generates_tag_and_sets_changed_at() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request =
                UserProfileCreateRequest.builder()
                        .nickname("kim")
                        .districtId(1L)
                        .regionalGrade(Grade.D)
                        .nationalGrade(Grade.D)
                        .birth("19900101")
                        .gender(Gender.MALE)
                        .build();

        User user = User.builder()
                .id(userId)
                .build();

        when(userProfileRepository.existsById(userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        RegionDistrict district = mock(RegionDistrict.class);
        when(regionService.findDistrictsById(1L)).thenReturn(Optional.of(district));

        // when
        userProfileService.createProfile(userId, request);

        // then
        verify(userProfileRepository).save(
                argThat(profile ->
                        profile.getTag() != null
                                && profile.getTag().matches("^[A-Z0-9]{4}$")
                                && profile.getTagChangedAt() != null
                ));
    }

    @Test
    @DisplayName("프로필 생성 시, 닉네임이 null/blank이면 프로필 생성은 실패한다.")
    void create_profile_throws_when_nickname_is_null() {
        // given
        Long userId = 1L;
        UserProfileCreateRequest request =
                UserProfileCreateRequest.builder()
                        .nickname(null)
                        .districtId(1L)
                        .regionalGrade(Grade.D)
                        .nationalGrade(Grade.D)
                        .birth("19900101")
                        .gender(Gender.MALE)
                        .build();

        User user = User.builder()
                .id(userId)
                .build();

        RegionDistrict district = mock(RegionDistrict.class);


        when(userProfileRepository.existsById(userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(regionService.findDistrictsById(1L)).thenReturn(Optional.of(district));

        // when & then
        assertThatThrownBy(() -> userProfileService.createProfile(userId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 필수 입력 항목입니다.");

        verify(userProfileRepository, never()).save(any());
    }
}
