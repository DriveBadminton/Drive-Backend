package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClientResolver;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthUserInfo;
import com.gumraze.drive.drive_backend.auth.repository.UserAuthRepository;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenGenerator;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final JwtAccessTokenGenerator jwtAccessTokenGenerator;
    private final UserAuthRepository userAuthRepository;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final OAuthClientResolver oAuthClientResolver;
    private final OAuthAllowedProvidersProperties allowedProviders;

    public AuthServiceImpl(
            JwtAccessTokenGenerator jwtAccessTokenGenerator,
            UserAuthRepository userAuthRepository,
            UserRepository userRepository,
            RefreshTokenService refreshTokenService,
            OAuthClientResolver oAuthClientResolver,
            OAuthAllowedProvidersProperties allowedProviders) {
        this.jwtAccessTokenGenerator = jwtAccessTokenGenerator;
        this.userAuthRepository = userAuthRepository;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.oAuthClientResolver = oAuthClientResolver;
        this.allowedProviders = allowedProviders;
    }

    @Override
    public OAuthLoginResult login(OAuthLoginRequestDto request) {
        if (!allowedProviders.getAllowedProviders().contains(request.getProvider())) {
            throw new IllegalArgumentException("허용되지 않는 provider" + request.getProvider());
        }

        // OAuth Provider 사용자 식별 + 프로필 정보
        OAuthUserInfo userInfo = oAuthClientResolver
                .resolve(request.getProvider())
                .getOAuthUserInfo(request.getAuthorizationCode(), request.getRedirectUri());

        // 우리 서비스의 사용자 확인
        Long userId = userAuthRepository
                .findUserId(
                        request.getProvider(),
                        userInfo.providerUserId()
                )
                .orElseGet(() -> {
                    // 신규 사용자 생성
                    Long newUserId = userRepository.createUser();

                    // 신규 사용자 저장
                    userAuthRepository.save(
                            request.getProvider(),
                            userInfo,
                            newUserId
                    );
                    return newUserId;
                });

        // 기존 사용자라면 프로필 갱신 -> 사용자의 최신 프로필이 업데이트 되면 해당 업데이트된 프로필을 가져옴
        userAuthRepository.updateProfile(
                request.getProvider(),
                userInfo
        );

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

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.deleteByPlainToken(refreshToken);
    }

}
