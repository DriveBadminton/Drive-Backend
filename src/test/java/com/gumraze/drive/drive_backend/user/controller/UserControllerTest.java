package com.gumraze.drive.drive_backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserMeResponse;
import com.gumraze.drive.drive_backend.user.dto.UserProfileIdentityUpdateRequest;
import com.gumraze.drive.drive_backend.user.dto.UserProfileResponseDto;
import com.gumraze.drive.drive_backend.user.dto.UserSearchResponse;
import com.gumraze.drive.drive_backend.user.entity.UserProfileUpdateRequest;
import com.gumraze.drive.drive_backend.user.service.UserProfileService;
import com.gumraze.drive.drive_backend.user.service.UserSearchService;
import com.gumraze.drive.drive_backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    @Autowired
    private ObjectMapper objectMapper;

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

        Pageable pageable = PageRequest.of(0, 20);
        Page<UserSearchResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(userSearchService.searchByNickname(eq(nickname), any(Pageable.class)))
                .thenReturn(page);
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then
        mockMvc.perform(get("/users")
                        .param("nickname", nickname)
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(1L))
                .andExpect(jsonPath("$.data.content[0].nickname").value(nickname))
                .andExpect(jsonPath("$.data.content[0].tag").value("AB12"))
                .andExpect(jsonPath("$.data.content[0].profileImageUrl").value(nullValue()))
        ;
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
                .thenReturn(Optional.of(response));
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then
        mockMvc.perform(get("/users")
                        .param("nickname", nickname)
                        .param("tag", tag)
                        .header("Authorization", "Bearer token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].userId").value(1L))
                .andExpect(jsonPath("$.data.content[0].nickname").value(nickname))
                .andExpect(jsonPath("$.data.content[0].tag").value(tag))
                .andExpect(jsonPath("$.data.content[0].profileImageUrl").value(nullValue()));
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

    @Test
    @DisplayName("내 프로필 상세조회 성공 테스트")
    void get_my_profile_detail_success() throws Exception {
        // given
        Long userId = 1L;

        UserProfileResponseDto response =
                UserProfileResponseDto.builder()
                        .status(UserStatus.ACTIVE)
                        .nickname("테스트 닉네임")
                        .tag("AB12")
                        .profileImageUrl("http://profile-image.com")
                        .birth(LocalDateTime.of(2000, 1, 1, 0, 0))
                        .birthVisible(true)
                        .gender(Gender.MALE)
                        .regionalGrade(Grade.D)
                        .nationalGrade(Grade.D)
                        .districtName("테스트 구")
                        .provinceName("테스트 시/도")
                        .tagChangedAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                        .createdAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                        .updatedAt(LocalDateTime.of(2022, 1, 1, 0, 0))
                        .build();

        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(userId));
        when(userProfileService.getMyProfile(userId))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/users/me/profile")
                        .header("Authorization", "Bearer token")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(jsonPath("$.data.nickname").value("테스트 닉네임"))
                .andExpect(jsonPath("$.data.tag").value("AB12"))
                .andExpect(jsonPath("$.data.profileImageUrl").value("http://profile-image.com"))
                .andExpect(jsonPath("$.data.birthVisible").value(true))
                .andExpect(jsonPath("$.data.gender").value("MALE"))
                .andExpect(jsonPath("$.data.regionalGrade").value("D급"))
                .andExpect(jsonPath("$.data.nationalGrade").value("D급"))
                .andExpect(jsonPath("$.data.districtName").value("테스트 구"))
                .andExpect(jsonPath("$.data.provinceName").value("테스트 시/도"));
    }

    @Test
    @DisplayName("기본 프로필 수정 성공")
    void update_my_profile_success() throws Exception {
        // given
        Long userId = 1L;
        UserProfileUpdateRequest request =
                UserProfileUpdateRequest.builder()
                        .birthVisible(true)
                        .gender(Gender.MALE)
                        .regionalGrade(Grade.D)
                        .nationalGrade(Grade.D)
                        .build();

        String body = objectMapper.writeValueAsString(request);
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(userId));
        doNothing().when(userProfileService)
                .updateMyProfile(eq(userId), any(UserProfileUpdateRequest.class));


        mockMvc.perform(patch("/users/me/profile")
                          .header("Authorization", "Bearer token")
                          .contentType(MediaType.APPLICATION_JSON)
                          .accept(MediaType.APPLICATION_JSON)
                          .content(body))
                  .andExpect(status().isOk())
                  .andExpect(jsonPath("$.success").value(true))
                  .andExpect(jsonPath("$.code").value("OK"))
                  .andExpect(jsonPath("$.message").value("내 프로필 수정 성공"));
    }

    @Test
    @DisplayName("닉네임/태그 변경 성공 테스트")
    void update_identity_success() throws Exception {
        // given
        Long userId = 1L;

        UserProfileIdentityUpdateRequest request =
                UserProfileIdentityUpdateRequest.builder()
                        .nickname("newNickname")
                        .tag("SON7")
                        .build();

        String body = objectMapper.writeValueAsString(request);

        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(userId));
        doNothing().when(userProfileService)
                .updateNicknameAndTags(eq(userId), any(UserProfileIdentityUpdateRequest.class));

        // when & then
        mockMvc.perform(patch("/users/me/profile/identity")
                .header("Authorization", "Bearer token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.message").value("닉네임/태그 변경 성공"));
    }
}
