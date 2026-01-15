package com.gumraze.drive.drive_backend.user.controller;

import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserMeResponse;
import com.gumraze.drive.drive_backend.user.dto.UserSearchResponse;
import com.gumraze.drive.drive_backend.user.service.UserProfileService;
import com.gumraze.drive.drive_backend.user.service.UserSearchService;
import com.gumraze.drive.drive_backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @MockitoBean
    private UserProfileService userProfileService;

    @MockitoBean
    private UserSearchService userSearchService;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("PENDING 사용자가 /users/me 조회 시 status만 반환한다.")
    void get_me_returns_pending_user_status() throws Exception {
        // given: 사용자는 현재 PENDING 상태임.
        UserMeResponse responseDto = UserMeResponse.builder()
                .status(UserStatus.PENDING)
                .build();

        // stub
        when(userService.getUserMe(1L))
                .thenReturn(responseDto);
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when: /users/me로 GET 요청을 보냄.
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer token"))
                // then: 응답은 status만 포함되어야 함.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(UserStatus.PENDING.name()))
                .andExpect(jsonPath("$.data.profileImageUrl").value(nullValue()))
                .andExpect(jsonPath("$.data.nickname").value(nullValue()));
    }

    @Test
    @DisplayName("ACTIVE 사용자가 /users/me 조회 시 프로필 정보를 반환한다.")
    void get_user_me_return_profile_when_active() throws Exception {
        // given
        UserMeResponse response = UserMeResponse.builder()
                .status(UserStatus.ACTIVE)
                .nickname("테스트 닉네임")
                .profileImageUrl("http://profile-image.com")
                .build();

        when(userService.getUserMe(1L)).thenReturn(response);
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        mockMvc.perform(get("/users/me")
                .header("Authorization", "Bearer token")
                .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value(UserStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.data.nickname").value("테스트 닉네임"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("http://profile-image.com"));
    }

    @Test
    @DisplayName("토큰이 없으면 401을 반환한다.")
    void get_user_me_returns_unauthorized_when_token_is_missing() throws Exception {
        mockMvc.perform(get("/users/me")
                        .accept("application/json"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("닉네임으로 사용자 검색")
    void search_user_by_nickname() throws Exception {
        // given
        String nickname = "kim";

        UserSearchResponse response = UserSearchResponse.builder()
                .userId(1L)
                .nickname(nickname)
                .tag("AB12")
                .profileImageUrl(null)
                .build();

        when(userSearchService.searchByNickname(nickname))
                .thenReturn(List.of(response));
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then
        mockMvc.perform(get("/users")
                        .param("nickname", nickname)
                        .header("Authorization", "Bearer token")
                        .accept("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value(1L))
                .andExpect(jsonPath("$.data[0].nickname").value(nickname))
                .andExpect(jsonPath("$.data[0].tag").value("AB12"))
                .andExpect(jsonPath("$.data[0].profileImageUrl").isEmpty());
    }

    @Test
    @DisplayName("닉네임과 태그로 사용자 검색")
    void search_user_by_nickname_and_tag() throws Exception {
        // given
        String nickname = "kim";
        String tag = "AB12";

        UserSearchResponse response =
                UserSearchResponse.builder()
                        .userId(1L)
                        .nickname(nickname)
                        .tag(tag)
                        .profileImageUrl(null)
                        .build();

        when(userSearchService.searchByNicknameAndTag(nickname, tag))
                .thenReturn(response);
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then
        mockMvc.perform(get("/users")
                        .param("nickname", nickname)
                        .param("tag", tag)
                        .header("Authorization", "Bearer token")
                        .accept("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].userId").value(1L))
                .andExpect(jsonPath("$.data[0].nickname").value(nickname))
                .andExpect(jsonPath("$.data[0].tag").value(tag))
                .andExpect(jsonPath("$.data[0].profileImageUrl").isEmpty());
    }

    @Test
    @DisplayName("nickname 파라미터 누락 시 400 반환")
    void search_user_missing_nickname_returns_400() throws Exception {
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer token")
                        .accept("application/json")
                )
                .andExpect(status().isBadRequest());
        verifyNoInteractions(userSearchService);
    }
}
