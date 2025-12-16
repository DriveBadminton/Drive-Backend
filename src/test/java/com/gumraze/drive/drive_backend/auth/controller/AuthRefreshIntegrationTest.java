package com.gumraze.drive.drive_backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthRefreshTokenResponseDto;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.access-token.secret=THIS_IS_A_TEST_SECRET_KEY_THAT_IS_LONG_ENOUGH_32BYTES_MINIMUM",
        "jwt.access-token.expiration-ms=3600000"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthRefreshIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @DisplayName("Refresh 성공으로 access와 refresh token 재발급")
    void refresh_success_returns_new_access_and_refresh_token() throws Exception {
        // given
        // 로그인 요철
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
        // access token이 body에 존재
        String responseBody = refreshResult.getResponse().getContentAsString();
        OAuthRefreshTokenResponseDto response =
                objectMapper.readValue(
                        responseBody,
                        OAuthRefreshTokenResponseDto.class
                );

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.userId()).isNotNull();

        // 새로운 refresh token 쿠키가 내려옴
        Cookie newRefreshTokenCookie = refreshResult.getResponse().getCookie("refresh_token");
        assertThat(newRefreshTokenCookie).isNotNull();
        assertThat(newRefreshTokenCookie.getValue()).isNotEqualTo(refreshTokenCookie.getValue());
    }
}
