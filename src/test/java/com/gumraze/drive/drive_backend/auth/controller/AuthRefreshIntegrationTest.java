package com.gumraze.drive.drive_backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.entity.RefreshToken;
import com.gumraze.drive.drive_backend.auth.oauth.OAuthClient;
import com.gumraze.drive.drive_backend.auth.oauth.ProviderAwareOAuthClient;
import com.gumraze.drive.drive_backend.auth.repository.JpaRefreshTokenRepository;
import com.gumraze.drive.drive_backend.auth.token.RefreshTokenGenerator;
import com.gumraze.drive.drive_backend.user.entity.User;
import com.gumraze.drive.drive_backend.user.repository.JpaUserRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.access-token.secret=THIS_IS_A_TEST_SECRET_KEY_THAT_IS_LONG_ENOUGH_32BYTES_MINIMUM",
        "jwt.access-token.expiration-ms=3600000"
})
@AutoConfigureMockMvc
@Import(AuthRefreshIntegrationTest.TestOAuthClients.class)
@ActiveProfiles("test")
@Transactional
class AuthRefreshIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Autowired
    JpaUserRepository jpaUserRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RefreshTokenGenerator refreshTokenGenerator;

    @TestConfiguration
    static class TestOAuthClients {
        @Bean
        @Primary
        OAuthClient kakaoOAuthClient() {
            return new TestOAuthClient(AuthProvider.KAKAO, "kakao-user-123");
        }

        @Bean
        OAuthClient googleOAuthClient() {
            return new TestOAuthClient(AuthProvider.GOOGLE, "google-user-123");
        }
    }

    private record TestOAuthClient(AuthProvider provider, String userId)
            implements OAuthClient, ProviderAwareOAuthClient {
        @Override
        public String getProviderUserId(String authorizationCode, String redirectUri) {
            return userId;
        }

        @Override
        public AuthProvider supports() {
            return provider;
        }
    }

    @Test
    @DisplayName("Refresh 성공으로 access와 refresh token 재발급")
    void refresh_success_returns_new_access_and_refresh_token() throws Exception {
        // given
        // 로그인 요청
        OAuthLoginRequestDto request =
                new OAuthLoginRequestDto(
                        AuthProvider.KAKAO,
                        "auth-code",
                        "https://test.com"
                );

        MvcResult loginResult = mockMvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andReturn();

        // refresh token 쿠키 추출
        var refreshTokenCookie = loginResult.getResponse().getCookie("refresh_token");

        // when
        // refresh 요청
        MvcResult refreshResult = mockMvc.perform(
                post("/auth/refresh")
                        .cookie(refreshTokenCookie)
        )
                .andExpect(status().isOk())
                .andReturn();

        // then
        // access token이 body(data)에 존재
        String responseBody = refreshResult.getResponse().getContentAsString();
        var dataNode = objectMapper.readTree(responseBody).get("data");

        assertThat(dataNode).isNotNull();
        assertThat(dataNode.get("accessToken").asText()).isNotBlank();
        assertThat(dataNode.get("userId").asLong()).isNotNull();

        // 새로운 refresh token 쿠키가 내려옴
        Cookie newRefreshTokenCookie = refreshResult.getResponse().getCookie("refresh_token");
        assertThat(newRefreshTokenCookie).isNotNull();
        assertThat(newRefreshTokenCookie.getValue()).isNotEqualTo(refreshTokenCookie.getValue());
    }

    @Test
    @DisplayName("Refresh Token 재사용 시 실패 테스트")
    void refresh_with_old_refresh_token_should_fail() throws Exception {
        // given
        // 로그인 수행
        OAuthLoginRequestDto request = new OAuthLoginRequestDto(
                AuthProvider.GOOGLE,
                "auth_code",
                "https://test.com"
        );

        MvcResult loginResult = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andReturn();

        // refresh token A 발급
        var refreshTokenA = loginResult.getResponse().getCookie("refresh_token");

        // refresh token A가 존재하는지 확인
        assertThat(refreshTokenA).isNotNull();

        // /auth/refresh 호출하여 refresh token B 발급
        MvcResult refreshResult = mockMvc.perform(
                        post("/auth/refresh")
                                .cookie(refreshTokenA)
                )
                .andExpect(status().isOk())
                .andReturn();

        // refreshTokenB 발급
        var refreshTokenB = refreshResult.getResponse().getCookie("refresh_token");

        // refreshTokenB가 존재하면서, A와 일치하지 않는지 확인
        assertThat(refreshTokenB).isNotNull();
        assertThat(refreshTokenB.getValue()).isNotEqualTo(refreshTokenA.getValue());

        // when
        // 이전 refresh token A로 다시 /auth/refresh 호출
        MvcResult refreshWithRefreshTokenA = mockMvc.perform(
                        post("/auth/refresh")
                                .cookie(refreshTokenA)
                )
                // then
                // 실패 및 새로운 accessToken 발급 안됨.
                .andExpect(status().is5xxServerError())
                .andReturn();
    }

    @Test
    @DisplayName("만료된 Refresh Token 처리")
    void expired_refresh_token_should_be_deleted() throws Exception {
        // given
        // 사용자 생성
        Long userId = userRepository.createUser();
        User user = userRepository.findById(userId).orElseThrow();

        // 만료된 refresh token 생성
        String plainToken = refreshTokenGenerator.generatePlainToken();
        String tokenHash = refreshTokenGenerator.hash(plainToken);
        RefreshToken expiredRefreshToken = new RefreshToken(
                user,
                tokenHash,
                LocalDateTime.now().minusDays(1) // 이미 만료
        );

        // refresh token 저장
        jpaRefreshTokenRepository.save(expiredRefreshToken);

        Cookie expiredRefreshTokenCookie = new Cookie("refresh_token", plainToken);

        // when
        mockMvc.perform(
                post("/auth/refresh")
                        .cookie(expiredRefreshTokenCookie)
        )
                // then
                .andExpect(status().is5xxServerError());
        // then(DB 정리 확인)
        Optional<RefreshToken> refreshToken = jpaRefreshTokenRepository.findByTokenHash(tokenHash);

        assertThat(refreshToken).isEmpty();
    }
}
