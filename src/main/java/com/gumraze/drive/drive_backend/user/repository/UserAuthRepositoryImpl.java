package com.gumraze.drive.drive_backend.user.repository;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;
import com.gumraze.drive.drive_backend.auth.repository.UserAuthRepository;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.entity.UserAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserAuthRepositoryImpl implements UserAuthRepository {

    private final JpaUserAuthRepository jpaUserAuthRepository;
    private final JpaUserRepository jpaUserRepository;


    /**
     * Finds the internal user ID associated with the specified external authentication provider and provider user ID.
     *
     * @param provider the external authentication provider to match
     * @param providerUserId the user identifier assigned by the external provider
     * @return an Optional containing the internal user ID if a matching UserAuth exists, empty otherwise
     */
    @Override
    public Optional<Long> findUserId(AuthProvider provider, String providerUserId) {
        return jpaUserAuthRepository
                .findByProviderAndProviderUserId(provider, providerUserId)
                .map(userAuth -> userAuth.getUser().getId());
    }

    /**
     * Create and persist a UserAuth association linking an existing user to an OAuth provider.
     *
     * Builds a new UserAuth for the user identified by {@code userId}, populates it with data
     * from {@code userInfo}, and saves it.
     *
     * @param provider the external authentication provider
     * @param userInfo contains provider user identifier and profile data to store on the UserAuth
     * @param userId the internal ID of the existing User to link
     */
    @Override
    public void save(AuthProvider provider, OAuthUserInfo userInfo, Long userId) {
        User user = jpaUserRepository
                .findById(userId)
                .orElseThrow();

        UserAuth userAuth = UserAuth.builder()
                .user(user)
                .provider(provider)
                .providerUserId(userInfo.providerUserId())
                .build();

        userAuth.updateFromOAuth(userInfo);
        jpaUserAuthRepository.save(userAuth);
    }

    /**
     * Update the stored UserAuth profile for the given provider and external user if a matching entry exists.
     *
     * @param provider the external authentication provider
     * @param userInfo OAuth user information (includes the provider user ID and profile attributes used to update the stored record)
     */
    @Override
    public void updateProfile(AuthProvider provider, OAuthUserInfo userInfo) {
        // 동일 provider와 id가 있으면 최신 프로필로 갱신
        jpaUserAuthRepository.findByProviderAndProviderUserId(provider, userInfo.providerUserId())
                .ifPresent(userAuth -> userAuth.updateFromOAuth(userInfo));

    }
}