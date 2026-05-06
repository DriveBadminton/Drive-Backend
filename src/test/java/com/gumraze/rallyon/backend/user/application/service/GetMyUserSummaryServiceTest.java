package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.user.application.port.in.LoadUserOnboardingStatusUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyUserSummaryQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadAccountAuthProviderPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.gumraze.rallyon.backend.support.UuidTestFixtures.uuid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyUserSummaryServiceTest {

    @Mock
    private LoadUserOnboardingStatusUseCase loadUserOnboardingStatusUseCase;

    @Mock
    private LoadUserProfilePort loadUserProfilePort;

    @Mock
    private LoadAccountAuthProviderPort loadAccountAuthProviderPort;

    @Test
    @DisplayName("PENDING 사용자는 프로필 조회 없이 status만 반환한다")
    void get_summary_returns_status_only_for_pending_user() {
        var accountId = uuid(1);
        GetMyUserSummaryService service = new GetMyUserSummaryService(
                loadUserOnboardingStatusUseCase,
                loadUserProfilePort,
                loadAccountAuthProviderPort
        );

        when(loadUserOnboardingStatusUseCase.load(accountId)).thenReturn(UserStatus.PENDING);
        when(loadAccountAuthProviderPort.loadLatestAuthProvider(accountId)).thenReturn(Optional.empty());

        UserMeResponse result = service.get(new GetMyUserSummaryQuery(accountId));

        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.status()).isEqualTo(UserStatus.PENDING);
        assertThat(result.nickname()).isNull();
        assertThat(result.provider()).isNull();
        verify(loadUserProfilePort, never()).loadByAccountId(accountId);
        verify(loadAccountAuthProviderPort).loadLatestAuthProvider(accountId);
    }

    @Test
    @DisplayName("ACTIVE 사용자는 프로필 정보를 포함한다")
    void get_summary_returns_profile_for_active_user() {
        var accountId = uuid(1);
        UserProfile profile = UserProfile.create(
                accountId,
                "테스트 닉네임",
                null,
                null,
                null,
                LocalDateTime.of(1998, 9, 25, 0, 0),
                null,
                "AB12",
                LocalDateTime.now()
        );
        profile.changeProfileImageUrl("https://example.com/profile.png");

        GetMyUserSummaryService service = new GetMyUserSummaryService(
                loadUserOnboardingStatusUseCase,
                loadUserProfilePort,
                loadAccountAuthProviderPort
        );

        when(loadUserOnboardingStatusUseCase.load(accountId)).thenReturn(UserStatus.ACTIVE);
        when(loadUserProfilePort.loadByAccountId(accountId)).thenReturn(Optional.of(profile));
        when(loadAccountAuthProviderPort.loadLatestAuthProvider(accountId)).thenReturn(Optional.of(AuthProvider.KAKAO));

        UserMeResponse result = service.get(new GetMyUserSummaryQuery(accountId));

        assertThat(result.accountId()).isEqualTo(accountId);
        assertThat(result.status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.nickname()).isEqualTo("테스트 닉네임");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/profile.png");
        assertThat(result.provider()).isEqualTo(AuthProvider.KAKAO);
    }
}
