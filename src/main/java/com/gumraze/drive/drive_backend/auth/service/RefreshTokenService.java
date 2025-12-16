package com.gumraze.drive.drive_backend.auth.service;

public interface RefreshTokenService {
    // 로그인 성공 시 호출되며, 기존 토큰은 폐기되고 새로운 Refresh Token이 발급됨.
    // 이후 DB에 저장
    String rotate(Long userId);

    Long validateAndGetUserId(String token);

    void deleteByPlainToken(String token);
}
