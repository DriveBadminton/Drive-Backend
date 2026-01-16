package com.gumraze.drive.drive_backend.user.service;

import com.gumraze.drive.drive_backend.region.entity.RegionDistrict;
import com.gumraze.drive.drive_backend.region.entity.RegionProvince;
import com.gumraze.drive.drive_backend.region.repository.RegionDistrictRepository;
import com.gumraze.drive.drive_backend.region.repository.RegionProvinceRepository;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.UserRole;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserProfileResponseDto;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetMyProfileDetailUseCaseTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RegionDistrictRepository regionDistrictRepository;

    @Mock
    private RegionProvinceRepository regionProvinceRepository;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @Test
    @DisplayName("내 프로필 상세 조회 성공 테스트")
    void get_my_profile_detail_success() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .status(UserStatus.ACTIVE)
                .role(UserRole.USER)
                .build();

        RegionProvince regionProvince = mock(RegionProvince.class);
        RegionDistrict regionDistrict = mock(RegionDistrict.class);



        UserProfile profile = UserProfile.builder()
                .id(1L)
                .user(user)
                .nickname("테스트 닉네임")
                .profileImageUrl("http://profile-image.com")
                .birth(LocalDateTime.now())
                .birthVisible(true)
                .regionDistrict(regionDistrict)
                .regionalGrade(Grade.D)
                .nationalGrade(Grade.D)
                .gender(Gender.MALE)
                .tag("AB12")
                .tagChangedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(regionProvince.getName()).thenReturn("테스트 시/도");
        when(regionDistrict.getName()).thenReturn("테스트 구");
        when(regionDistrict.getProvince()).thenReturn(regionProvince);

        // when
        UserProfileResponseDto result = userProfileService.getMyProfile(userId);

        // then
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.getNickname()).isEqualTo("테스트 닉네임");
        assertThat(result.getTag()).isEqualTo("AB12");
        assertThat(result.getProfileImageUrl()).isEqualTo("http://profile-image.com");
        assertThat(result.getBirth()).isNotNull();
        assertThat(result.isBirthVisible()).isTrue();
        assertThat(result.getGender()).isEqualTo(Gender.MALE);
        assertThat(result.getRegionalGrade()).isEqualTo(Grade.D);
        assertThat(result.getNationalGrade()).isEqualTo(Grade.D);
        assertThat(result.getDistrictName()).isEqualTo("테스트 구");
        assertThat(result.getProvinceName()).isEqualTo("테스트 시/도");
    }
}
