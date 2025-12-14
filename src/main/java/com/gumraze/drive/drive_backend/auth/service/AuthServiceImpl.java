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
        String accessToken = accessTokenGenerator.generateAccessToken();
        return new OAuthLoginResult(accessToken);
    }
}
