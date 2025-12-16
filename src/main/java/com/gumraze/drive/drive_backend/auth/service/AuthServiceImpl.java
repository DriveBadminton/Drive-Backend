package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.repository.UserAuthRepository;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenGenerator;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final JwtAccessTokenGenerator jwtAccessTokenGenerator;
    private final OAuthClient oauthClient;
    private final UserAuthRepository userAuthRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(
            JwtAccessTokenGenerator jwtAccessTokenGenerator,
            OAuthClient oauthClient,
            UserAuthRepository userAuthRepository,
            UserRepository userRepository, RefreshTokenService refreshTokenService

    ) {
        this.jwtAccessTokenGenerator = jwtAccessTokenGenerator;
        this.oauthClient = oauthClient;
        this.userAuthRepository = userAuthRepository;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public OAuthLoginResult login(OAuthLoginRequestDto request) {
        // OAuth Provider 사용자 식별
        String providerUserId = oauthClient.getProviderUserId(
                request.getAuthorizationCode(),
                request.getRedirectUri()
        );

        // 우리 서비스의 사용자 확인
        Long userId = userAuthRepository
                .findUserId(
                        request.getProvider(),
                        providerUserId
                )
                .orElseGet(() -> {
                    // 신규 사용자 생성
                    Long newUserId = userRepository.createUser();

                    // 신규 사용자 저장
                    userAuthRepository.save(
                            request.getProvider(),
                            providerUserId,
                            newUserId
                    );
                    return newUserId;
                });

        // Access 토큰 발급 (userId 기반)
        String accessToken = jwtAccessTokenGenerator.generateAccessToken(userId);

        // Refresh Token 발급(회전)
        String refreshToken = refreshTokenService.rotate(userId);

        return new OAuthLoginResult(
                userId,
                accessToken,
                refreshToken
        );
    }

    @Override
    public OAuthLoginResult refresh(String refreshToken) {
        // Refresh Token으로 UserId 조회
        Long userId = refreshTokenService.validateAndGetUserId(refreshToken);

        // 새로운 AccessToken, RefreshToken 발급
        String newAccessToken = jwtAccessTokenGenerator.generateAccessToken(userId);
        String newRefreshToken = refreshTokenService.rotate(userId);

        return new OAuthLoginResult(userId, newAccessToken, newRefreshToken);
    }
}
