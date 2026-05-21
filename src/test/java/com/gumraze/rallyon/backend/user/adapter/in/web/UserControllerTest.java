package com.gumraze.rallyon.backend.user.adapter.in.web;

import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.security.config.SecurityConfig;
import com.gumraze.rallyon.backend.user.application.port.in.CreateMyProfileUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.GetMyProfileDefaultsUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.GetMyProfileUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.GetMyUserSummaryUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.SearchUsersUseCase;
import com.gumraze.rallyon.backend.user.application.port.in.UpdateMyProfileUseCase;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.UserStatus;
import com.gumraze.rallyon.backend.user.dto.UserMeResponse;
import com.gumraze.rallyon.backend.user.dto.UserProfileDefaultsResponse;
import com.gumraze.rallyon.backend.user.dto.UserProfileResponseDto;
import com.gumraze.rallyon.backend.user.dto.UserProfileUpdateRequest;
import com.gumraze.rallyon.backend.user.dto.UserSearchResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private SearchUsersUseCase searchUsersUseCase;

    @MockitoBean
    private GetMyUserSummaryUseCase getMyUserSummaryUseCase;

    @MockitoBean
    private CreateMyProfileUseCase createMyProfileUseCase;

    @MockitoBean
    private GetMyProfileDefaultsUseCase getMyProfileDefaultsUseCase;

    @MockitoBean
    private GetMyProfileUseCase getMyProfileUseCase;

    @MockitoBean
    private UpdateMyProfileUseCase updateMyProfileUseCase;

    @Test
    @DisplayName("PENDING 사용자가 /users/me 조회 시 status만 반환한다")
    void get_me_returns_pending_user_status() throws Exception {
        UUID accountId = UUID.randomUUID();
        UserMeResponse response = new UserMeResponse(accountId, UserStatus.PENDING, null, null, null);

        when(getMyUserSummaryUseCase.get(any())).thenReturn(response);

        mockMvc.perform(get("/users/me")
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.status").value(UserStatus.PENDING.name()))
                .andExpect(jsonPath("$.profileImageUrl").value(nullValue()))
                .andExpect(jsonPath("$.nickname").value(nullValue()))
                .andExpect(jsonPath("$.provider").value(nullValue()));
    }

    @Test
    @DisplayName("ACTIVE 사용자가 /users/me 조회 시 프로필 정보를 반환한다")
    void get_user_me_return_profile_when_active() throws Exception {
        UUID accountId = UUID.randomUUID();
        UserMeResponse response = new UserMeResponse(
                accountId,
                UserStatus.ACTIVE,
                "http://profile-image.com",
                "테스트 닉네임",
                AuthProvider.GOOGLE
        );

        when(getMyUserSummaryUseCase.get(any())).thenReturn(response);

        mockMvc.perform(get("/users/me")
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.status").value(UserStatus.ACTIVE.name()))
                .andExpect(jsonPath("$.nickname").value("테스트 닉네임"))
                .andExpect(jsonPath("$.profileImageUrl").value("http://profile-image.com"))
                .andExpect(jsonPath("$.provider").value(AuthProvider.GOOGLE.name()));
    }

    @Test
    @DisplayName("토큰이 없으면 401을 반환한다")
    void get_user_me_returns_unauthorized_when_token_is_missing() throws Exception {
        mockMvc.perform(get("/users/me")
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("닉네임으로 사용자 검색")
    void search_user_by_nickname() throws Exception {
        String nickname = "kim";
        UserSearchResponse response = new UserSearchResponse(
                UUID.randomUUID(),
                nickname,
                "AB12",
                null
        );

        Page<UserSearchResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(searchUsersUseCase.search(any())).thenReturn(page);

        mockMvc.perform(get("/users")
                        .param("nickname", nickname)
                        .param("page", "0")
                        .param("size", "20")
                        .with(authenticatedUser(UUID.randomUUID()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].accountId").value(response.accountId().toString()))
                .andExpect(jsonPath("$.content[0].nickname").value(nickname))
                .andExpect(jsonPath("$.content[0].tag").value("AB12"));
    }

    @Test
    @DisplayName("닉네임과 태그로 사용자 검색")
    void search_user_by_nickname_and_tag() throws Exception {
        String nickname = "kim";
        String tag = "AB12";

        UserSearchResponse response = new UserSearchResponse(
                UUID.randomUUID(),
                nickname,
                tag,
                null
        );

        Page<UserSearchResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);
        when(searchUsersUseCase.search(any())).thenReturn(page);

        mockMvc.perform(get("/users")
                        .param("nickname", nickname)
                        .param("tag", tag)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tag").value(tag));
    }

    @Test
    @DisplayName("nickname 파라미터 누락 시 400에러 반환")
    void search_user_missing_nickname_returns_400() throws Exception {
        mockMvc.perform(get("/users")
                        .with(authenticatedUser(UUID.randomUUID()))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        verifyNoInteractions(searchUsersUseCase);
    }

    @Test
    @DisplayName("프로필 생성 성공 시 accountId를 반환한다")
    void create_my_profile_success() throws Exception {
        UUID accountId = UUID.randomUUID();
        var request = """
                {
                  "nickname": "테스트 닉네임",
                  "districtId": "%s",
                  "regionalGrade": "D",
                  "nationalGrade": "C",
                  "birth": "20000101",
                  "gender": "MALE"
                }
                """.formatted(UUID.randomUUID());

        doNothing().when(createMyProfileUseCase).create(any());

        mockMvc.perform(post("/users/me/profile")
                        .with(authenticatedUser(accountId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/users/me/profile"))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()));
    }

    @Test
    @DisplayName("내 프로필 상세조회 성공")
    void get_my_profile_detail_success() throws Exception {
        UUID accountId = UUID.randomUUID();
        UserProfileResponseDto response = new UserProfileResponseDto(
                UserStatus.ACTIVE,
                "테스트 닉네임",
                "AB12",
                "http://profile-image.com",
                Gender.MALE,
                LocalDateTime.of(2000, 1, 1, 0, 0),
                true,
                Grade.D,
                Grade.D,
                "테스트 구",
                "테스트 시/도",
                LocalDateTime.of(2022, 1, 1, 0, 0),
                LocalDateTime.of(2022, 1, 1, 0, 0),
                LocalDateTime.of(2022, 1, 1, 0, 0)
        );

        when(getMyProfileUseCase.get(any())).thenReturn(response);

        mockMvc.perform(get("/users/me/profile")
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.nickname").value("테스트 닉네임"))
                .andExpect(jsonPath("$.tag").value("AB12"));
    }

    @Test
    @DisplayName("기본 프로필 수정 성공")
    void update_my_profile_success() throws Exception {
        UUID accountId = UUID.randomUUID();
        UserProfileUpdateRequest request = new UserProfileUpdateRequest(
                "newNickname",
                "SON7",
                Grade.D,
                Grade.D,
                null,
                true,
                null,
                null,
                Gender.MALE
        );

        doNothing().when(updateMyProfileUseCase).update(any());

        mockMvc.perform(patch("/users/me/profile")
                        .with(authenticatedUser(accountId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("프로필 초기값 조회 성공")
    void get_profile_defaults_success() throws Exception {
        UUID accountId = UUID.randomUUID();
        UserProfileDefaultsResponse response = new UserProfileDefaultsResponse("kakao-player", true);

        when(getMyProfileDefaultsUseCase.get(any())).thenReturn(response);

        mockMvc.perform(get("/users/me/profile/defaults")
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedNickname").value("kakao-player"))
                .andExpect(jsonPath("$.hasSuggestedNickname").value(true));
    }

    private RequestPostProcessor authenticatedUser(UUID accountId) {
        return authentication(new UsernamePasswordAuthenticationToken(
                accountId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
    }
}
