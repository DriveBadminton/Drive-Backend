package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.repository.UserAuthRepository;
import com.gumraze.drive.drive_backend.auth.token.AccessTokenGenerator;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AccessTokenGenerator accessTokenGenerator;
    private final OAuthClient oauthClient;
    private final UserAuthRepository userAuthRepository;
    private final UserRepository userRepository;

    public AuthServiceImpl(
            AccessTokenGenerator accessTokenGenerator,
            OAuthClient oauthClient,
            UserAuthRepository userAuthRepository,
            UserRepository userRepository

    ) {
        this.accessTokenGenerator = accessTokenGenerator;
        this.oauthClient = oauthClient;
        this.userAuthRepository = userAuthRepository;
        this.userRepository = userRepository;
    }

    @Override
    public OAuthLoginResult login(OAuthLoginRequestDto request) {
        // OAuth Provider 사용자 식별
        String providerUserId = oauthClient.getProviderUserId(
                request.getAuthorizationCode(),
                request.getRedirectUri()
        );

        // 우리 서비스의 사용자 식별 (임시)
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

        // 액세스 토큰 발급 (userId 기반)
        String accessToken = accessTokenGenerator.generateAccessToken(userId);

        return new OAuthLoginResult(userId, accessToken);
    }
}
