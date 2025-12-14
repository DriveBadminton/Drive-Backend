package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;

public class AuthServiceImpl implements AuthService {
    @Override
    public OAuthLoginResult login(OAuthLoginRequestDto request) {
        return new OAuthLoginResult("dummy-access-token");
    }
}
