package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.token.AccessTokenGenerator;

public class AuthServiceImpl implements AuthService {

    private final AccessTokenGenerator accessTokenGenerator;

    public AuthServiceImpl(AccessTokenGenerator accessTokenGenerator) {
        this.accessTokenGenerator = accessTokenGenerator;
    }

    @Override
    public OAuthLoginResult login(OAuthLoginRequestDto request) {
        // 사용자 식별(임시)
        Long userId = 1L;

        // 토큰 발급 (userId 기반)
        String accessToken = accessTokenGenerator.generateAccessToken(userId);

        return new OAuthLoginResult(userId, accessToken);
    }
}
