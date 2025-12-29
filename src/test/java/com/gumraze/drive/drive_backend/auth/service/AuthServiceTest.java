package com.gumraze.drive.drive_backend.auth.service;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenGenerator;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.auth.token.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {
    private AuthService authService;
    private JwtAccessTokenGenerator jwtAccessTokenGenerator;
    private JwtProperties properties;
    private FakeOAuthClient fakeOAuthClient;
    private FakeUserAuthRepository userAuthRepository;
    private FakeUserRepository userRepository;
    private RefreshTokenService refreshTokenService;
    private FakeOAuthClientResolver oAuthClientResolver;
    private OAuthAllowedProvidersProperties allowedProvidersProperties;

    // 테스트 실행되기 전에 항상 실행되는 메서드
    @BeforeEach
    void setUp() {
        properties = new JwtProperties(
                "test-secret-key-test-secret-key-test-secret-key",
                1800000L
        );

        jwtAccessTokenGenerator = new JwtAccessTokenGenerator(properties);
        fakeOAuthClient = new FakeOAuthClient("oauth-user-123");
        oAuthClientResolver = new FakeOAuthClientResolver();
        oAuthClientResolver.register(AuthProvider.DUMMY, fakeOAuthClient);
        userAuthRepository = new FakeUserAuthRepository();
        userRepository = new FakeUserRepository();
        refreshTokenService = new FakeRefreshTokenService();

        OAuthAllowedProvidersProperties allowedProps = new OAuthAllowedProvidersProperties();
        allowedProps.setAllowedProviders(List.of(AuthProvider.DUMMY, AuthProvider.KAKAO));

        authService = new AuthServiceImpl(
                jwtAccessTokenGenerator,
                userAuthRepository,
                userRepository,
                refreshTokenService,
                oAuthClientResolver,
                allowedProps
        );
    }

    @Test
    @DisplayName("OAuth 로그인을 하면 결과 객체를 반환한다.")
    void login_returns_result_when_oauth_login() {
        // given
        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.DUMMY)
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
                .provider(AuthProvider.DUMMY)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result.accessToken()).isNotBlank();
    }

    @Test
    @DisplayName("accessToken은 사용자의 식별자(userId)를 포함한다.")
    void access_token_contains_user_identifier() {
        // given
        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.DUMMY)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        JwtAccessTokenValidator validator = new JwtAccessTokenValidator(properties);

        Long userIdFromToken = validator.validateAndGetUserId(result.accessToken()).orElseThrow();

        assertThat(userIdFromToken).isEqualTo(result.userId());
    }

    @Test
    @DisplayName("OAuth 로그인 시 사용자 식별이 먼저 수행된다.")
    void oauth_login_identifies_user() {
        // given
        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.DUMMY)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result.userId()).isNotNull();
    }

    @Test
    @DisplayName("OAuth 로그인 시 OAuthClient를 호출한다.")
    void oauth_login_calls_oauth_client() {
        // given
        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.DUMMY)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(fakeOAuthClient.isCalled()).isTrue();
    }

    @Test
    @DisplayName("이미 가입된 사용자는 providerUserId로 기존 userId를 반환한다.")
    void returns_existing_user_id_when_user_already_registered() {

        // given
        userAuthRepository.save(
                AuthProvider.DUMMY,
                "oauth-user-123",
                10L
        );

        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.DUMMY)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result.userId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("신규 사용자는 userId를 생성하고 user_auth에 저장한다.")
    void creates_new_user_when_not_registered_yet() {
        // given
        FakeUserAuthRepository userAuthRepository =
            new FakeUserAuthRepository();

        FakeOAuthClient fakeOAuthClient =
                new FakeOAuthClient("oauth-user-123");

        FakeUserRepository userRepository =
                new FakeUserRepository();

        FakeOAuthClientResolver oAuthClientResolver =
                new FakeOAuthClientResolver();
        oAuthClientResolver.register(AuthProvider.DUMMY, fakeOAuthClient);

        OAuthAllowedProvidersProperties allowedProps = new OAuthAllowedProvidersProperties();
        allowedProps.setAllowedProviders(List.of(AuthProvider.DUMMY, AuthProvider.KAKAO));

        AuthService authService = new AuthServiceImpl(
                jwtAccessTokenGenerator,
                userAuthRepository,
                userRepository,
                refreshTokenService,
                oAuthClientResolver,
                allowedProps
        );

        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.DUMMY)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(result.userId()).isNotNull();

        assertThat(
                userAuthRepository.findUserId(
                        AuthProvider.DUMMY,
                        "oauth-user-123"
                )
        ).isPresent();      // Optional 안에 값이 존재하는지 여부를 알려주는 메서드
    }

    @Test
    @DisplayName("신규 사용자일 경우 UserRepository를 통해 사용자를 생성한다.")
    void creates_user_through_user_repository_when_new_user() {
        // given
        FakeUserAuthRepository userAuthRepository =
            new FakeUserAuthRepository();

        FakeOAuthClientResolver oAuthClientResolver =
                new FakeOAuthClientResolver();
        oAuthClientResolver.register(AuthProvider.DUMMY, fakeOAuthClient);

        OAuthAllowedProvidersProperties allowedProps = new OAuthAllowedProvidersProperties();
        allowedProps.setAllowedProviders(List.of(AuthProvider.DUMMY, AuthProvider.KAKAO));

        AuthService authService = new AuthServiceImpl(
                jwtAccessTokenGenerator,
                userAuthRepository,
                userRepository,
                refreshTokenService,
                oAuthClientResolver,
                allowedProps
        );

        OAuthLoginRequestDto request = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.DUMMY)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when
        OAuthLoginResult result = authService.login(request);

        // then
        assertThat(userRepository.isCreateCalled()).isTrue();
    }

    @Test
    @DisplayName("구글 로그인 시 실패 Test")
    void fails_DUMMY_login() {
        // given: 구글 로그인으로 요청
        OAuthLoginRequestDto requestDto = OAuthLoginRequestDto.builder()
                .provider(AuthProvider.GOOGLE)
                .authorizationCode("test-code")
                .redirectUri("https://test.com")
                .build();

        // when: 허용되지 않는 provider 라면 예외 발생
        assertThatThrownBy(() -> authService.login(requestDto))
                // then: 예외 발생
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 provider");
    }
}
