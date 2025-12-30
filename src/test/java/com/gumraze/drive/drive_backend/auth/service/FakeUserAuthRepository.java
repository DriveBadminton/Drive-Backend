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

    @Override
    public void save(AuthProvider provider, OAuthUserInfo userInfo, Long userId) {
        String key = provider + ":" + userInfo.providerUserId();
        userIdByKey.put(key, userId);
        infoByKey.put(key, userInfo);
    }

    @Override
    public void updateProfile(AuthProvider provider, OAuthUserInfo userInfo) {
        String key = provider + ":" + userInfo.providerUserId();
        infoByKey.put(key, userInfo);
    }

    // 테스트에서만 사용하는 조회 메서드
    public Optional<OAuthUserInfo> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId) {
        return Optional.ofNullable(infoByKey.get(provider + ":" + providerUserId));
    }
}
