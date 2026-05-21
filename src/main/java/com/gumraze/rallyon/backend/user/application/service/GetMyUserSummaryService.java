package com.gumraze.rallyon.backend.user.application.service;

import com.gumraze.rallyon.backend.user.application.port.in.GetMyUserSummaryUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.LoadUserOnboardingStatusUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.query.GetMyUserSummaryQuery;
import com.gumraze.rallyon.backend.user.application.port.out.LoadAccountAuthProviderPort;
import com.gumraze.rallyon.backend.user.application.port.out.LoadUserProfilePort;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetMyUserSummaryService implements GetMyUserSummaryUseCase {

    private final LoadUserOnboardingStatusUseCase loadUserOnboardingStatusUseCase;
    private final LoadUserProfilePort loadUserProfilePort;
    private final LoadAccountAuthProviderPort loadAccountAuthProviderPort;

    @Override
    public UserMeResponse get(GetMyUserSummaryQuery query) {
        var status = loadUserOnboardingStatusUseCase.load(query.accountId());

        UserProfile profile = null;
        if (status == UserStatus.ACTIVE) {
            profile = loadUserProfilePort.loadByAccountId(query.accountId()).orElse(null);
        }

        var provider = loadAccountAuthProviderPort.loadLatestAuthProvider(query.accountId()).orElse(null);

        return new UserMeResponse(
                query.accountId(),
                status,
                profile != null ? profile.getProfileImageUrl() : null,
                profile != null ? profile.getNickname() : null,
                provider
        );
    }
}
