package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;

public interface AuthService {

    OAuthLoginResult login(OAuthLoginRequestDto request);

    OAuthLoginResult refresh(String refreshToken);
}
