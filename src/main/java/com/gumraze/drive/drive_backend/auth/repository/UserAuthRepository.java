package com.gumraze.drive.drive_backend.auth.repository;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;

import java.util.Optional;

public interface UserAuthRepository {

    Optional<Long> findUserId(AuthProvider provider, String providerUserId);

    void save(AuthProvider provider, OAuthUserInfo userInfo, Long userId);

    void updateProfile(AuthProvider provider, OAuthUserInfo userInfo);
}
