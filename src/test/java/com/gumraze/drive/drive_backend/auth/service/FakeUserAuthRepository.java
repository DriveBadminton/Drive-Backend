package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;
import com.gumraze.drive.drive_backend.auth.repository.UserAuthRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeUserAuthRepository implements UserAuthRepository {

    private final Map<String, Long> userIdByKey = new HashMap<>();
    private final Map<String, OAuthUserInfo> infoByKey = new HashMap<>();

    @Override
    public Optional<Long> findUserId(AuthProvider provider, String providerUserId) {
        return Optional.ofNullable(
                userIdByKey.get(provider + ":" + providerUserId)
        );
    }

    /**
     * Store an association between an external provider user and a local user and save the provider's OAuth user info.
     *
     * Persists a mapping from the provider's user identifier to the given local userId and records the provided OAuthUserInfo for later retrieval.
     *
     * @param provider the external authentication provider
     * @param userInfo the provider's OAuth user information; its providerUserId is used as the external identifier
     * @param userId the local user identifier to associate with the provider user
     */
    @Override
    public void save(AuthProvider provider, OAuthUserInfo userInfo, Long userId) {
        String key = provider + ":" + userInfo.getProviderUserId();
        userIdByKey.put(key, userId);
        infoByKey.put(key, userInfo);
    }

    @Override
    public void updateProfile(AuthProvider provider, OAuthUserInfo userInfo) {
        String key = provider + ":" + userInfo.getProviderUserId();
        infoByKey.put(key, userInfo);
    }

    public Optional<OAuthUserInfo> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return Optional.ofNullable(infoByKey.get(provider + ":" + providerUserId));
    }
}