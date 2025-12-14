package com.gumraze.drive.drive_backend.auth.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 서비스의 usecase의 실행 결과를 표현하는 모델

@Getter
@AllArgsConstructor
public class OAuthLoginResult {
    private final String accessToken;
}
