package com.gumraze.drive.drive_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class OAuthLoginResponseDto {
    /**
     * 우리 서비스 API 접근 권한을 나타내는 JWT Token
     */
    private String accessToken;
}
