package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.entity.RefreshToken;
import com.gumraze.drive.drive_backend.auth.repository.JpaRefreshTokenRepository;
import com.gumraze.drive.drive_backend.auth.token.JwtProperties;
import com.gumraze.drive.drive_backend.auth.token.RefreshTokenGenerator;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JpaUserRepository jpaUserRepository;
    private final JwtProperties properties;

    @Override
    public String rotate(Long userId) {
        // 사용자 id 조회
        User user = jpaUserRepository.findById(userId).orElseThrow();

        // 기존 Refresh Token 삭제
        jpaRefreshTokenRepository.deleteByUser(user);

        // 새로운 Refresh Token 생성
        String token = refreshTokenGenerator.generatePlainToken();

        // 저장
        RefreshToken refreshToken = new RefreshToken(
                user,
                refreshTokenGenerator.hash(token),
                LocalDateTime.now().plusHours(properties.refreshToken().expirationHours())
        );

        jpaRefreshTokenRepository.save(refreshToken);

        return token;
    }

    @Override
    public Long validateAndGetUserId(String token) {
        String tokenHash = refreshTokenGenerator.hash(token);

        // Refresh Token 조회
        RefreshToken refreshToken = jpaRefreshTokenRepository
                .findByTokenHash(tokenHash)
                .orElseThrow();

        if (refreshToken.getExpiredAt().isBefore(LocalDateTime.now())) {
            jpaRefreshTokenRepository.delete(refreshToken); // 만료 토큰 정리
            throw new RuntimeException("Refresh Token이 만료되었습니다.");
        }

        return refreshToken.getUser().getId();
    }

    @Override
    public void deleteByPlainToken(String token) {
        String tokenHash = refreshTokenGenerator.hash(token);

        // 토큰이 있으면 삭제
        jpaRefreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(jpaRefreshTokenRepository::delete);
    }
}
