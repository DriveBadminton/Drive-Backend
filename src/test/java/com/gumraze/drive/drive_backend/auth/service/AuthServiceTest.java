package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.token.AccessTokenGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {
    private AccessTokenGenerator accessTokenGenerator = new AccessTokenGenerator();
    private AuthService authService = new AuthServiceImpl(accessTokenGenerator);

    @Test
    @DisplayName("OAuth 로그인을 하면 결과 객체를 반환한다.")
    void login_returns_result_when_oauth_login() {
        // given
        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(null) // 지금은 중요하지 않음.
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("로그인 결과에는 accessToken이 포함된다.")
    void login_result_contains_access_token() {
        // given
        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(null)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result.getAccessToken()).isNotBlank();
    }

    @Test
    @DisplayName("로그인할 때마다 accessToken은 새로 발급된다.")
    void access_token_is_newly_issued_each_time() {
        // given
        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(null)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result1 = authService.login(request);
        OAuthLoginResult result2 = authService.login(request);

        // then
        assertThat(result1.getAccessToken())
                .isNotEqualTo(result2.getAccessToken());
    }
}