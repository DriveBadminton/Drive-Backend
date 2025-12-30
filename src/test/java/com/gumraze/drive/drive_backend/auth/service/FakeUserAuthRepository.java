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

    /**
     * Retrieve the user id associated with the given authentication provider and provider-specific user id.
     *
     * @param provider the authentication provider
     * @param providerUserId the user id assigned by the provider
     * @return an Optional containing the user id for the given provider and providerUserId, or empty if no mapping exists
     */
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
        String key = provider + ":" + userInfo.providerUserId();
        userIdByKey.put(key, userId);
        infoByKey.put(key, userInfo);
    }

    /**
     * Update the stored OAuthUserInfo for the given provider and provider user ID.
     *
     * Overwrites any existing entry for the provider + providerUserId composite key, or creates a new entry if none exists.
     *
     * @param provider the authentication provider
     * @param userInfo the OAuth user information whose providerUserId is used as the storage key
     */
    @Override
    public void updateProfile(AuthProvider provider, OAuthUserInfo userInfo) {
        String key = provider + ":" + userInfo.providerUserId();
        infoByKey.put(key, userInfo);
    }

    /**
     * Retrieve stored OAuth user information for a provider-specific user ID (test only).
     *
     * @param provider the authentication provider
     * @param providerUserId the provider-specific user identifier
     * @return an Optional containing the stored OAuthUserInfo for the given provider and providerUserId, or empty if none exists
     */
    public Optional<OAuthUserInfo> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return Optional.ofNullable(infoByKey.get(provider + ":" + providerUserId));
    }
}