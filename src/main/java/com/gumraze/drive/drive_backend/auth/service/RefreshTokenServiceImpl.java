package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.entity.RefreshToken;
import com.gumraze.drive.drive_backend.auth.repository.RefreshTokenRepository;
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

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenGenerator refreshTokenGenerator;
    private final JpaUserRepository jpaUserRepository;

    @Override
    public String rotate(Long userId) {
        // 사용자 id 조회
        User user = jpaUserRepository.findById(userId).orElseThrow();

        // 기존 Refresh Token 삭제
        refreshTokenRepository.deleteByUser(user);

        // 새로운 Refresh Token 생성
        String token = refreshTokenGenerator.generatePlainToken();

        // 저장
        RefreshToken refreshToken = new RefreshToken(
                user,
                refreshTokenGenerator.hash(token),
                LocalDateTime.now().plusDays(5)
        );

        refreshTokenRepository.save(refreshToken);

        return token;
    }
}
