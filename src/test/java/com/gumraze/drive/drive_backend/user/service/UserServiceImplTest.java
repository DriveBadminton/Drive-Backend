package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.user.constants.UserRole;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserMeResponse;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserProfile;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserProfileRepository userProfileRepository;

    @InjectMocks
    UserServiceImpl userServiceImpl;

    @Test
    @DisplayName("사용자가 없으면 예외가 발생한다.")
    void get_user_me_throws_when_user_not_found() {
        // given
        Long userId = 1L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServiceImpl.getUserMe(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("PENDING이면 프로필 없이 응답한다.")
    void get_user_me_returns_status_only_when_not_active() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .status(UserStatus.PENDING)
                .role(UserRole.USER)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        UserMeResponse response = userServiceImpl.getUserMe(userId);

        // then
        assertThat(response.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(response.getNickname()).isNull();
        assertThat(response.getProfileImageUrl()).isNull();
    }

    @Test
    @DisplayName("ACTIVE이면 프로필 정보를 포함한다.")
    void get_user_me_returns_profile_when_active() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        RegionDistrict district = mock(RegionDistrict.class);
        UserProfile profile = UserProfile.builder()
                        .id(userId)
                        .nickname("테스트 닉네임")
                        .regionDistrict(district)
                        .build();
        profile.setProfileImageUrl("https://example.com/image.png");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        // when
        UserMeResponse response = userServiceImpl.getUserMe(userId);

        // then
        assertThat(response.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(response.getNickname()).isEqualTo("테스트 닉네임");
        assertThat(response.getProfileImageUrl()).isEqualTo("https://example.com/image.png");
    }
}
