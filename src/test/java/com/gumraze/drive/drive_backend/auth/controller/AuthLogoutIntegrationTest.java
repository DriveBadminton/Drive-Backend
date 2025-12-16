package com.gumraze.drive.drive_backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.repository.JpaRefreshTokenRepository;
import com.gumraze.drive.drive_backend.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.access-token.secret=THIS_IS_A_TEST_SECRET_KEY_THAT_IS_LONG_ENOUGH_32BYTES_MINIMUM",
        "jwt.access-token.expiration-ms=3600000"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthLogoutIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("로그아웃시 Refresh Token 삭제 및 쿠키 만료 테스트")
    void logout_deletes_refresh_token_and_expires_cookie() throws Exception {
        // given: 로그인해서 refresh token을 가진 사용자가
        // 로그인
        OAuthLoginRequestDto request = new OAuthLoginRequestDto(
                AuthProvider.KAKAO,
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

        Cookie refreshTokenCookie =
                loginResult.getResponse().getCookie("refresh_token");

        // 쿠기에 리프레시 토큰이 존재하는지 확인
        assertThat(refreshTokenCookie).isNotNull();

        // when: /auth/logout을 호출하면
        // 로그아웃 호출
        MvcResult logoutResult = mockMvc.perform(
                post("/auth/logout")
                        .cookie(refreshTokenCookie)
        )
                .andExpect(status().isOk())
                .andReturn();

        // then: refresh token은 DB에서 삭제되고, refresh token 쿠키는 만료된다.
        // 쿠키 만료 확인
        Cookie expiredRefreshTokenCookie =
                logoutResult.getResponse().getCookie("refresh_token");

        assertThat(expiredRefreshTokenCookie).isNotNull();
        assertThat(expiredRefreshTokenCookie.getMaxAge()).isZero();
        assertThat(expiredRefreshTokenCookie.getValue()).isEmpty();
    }
}
