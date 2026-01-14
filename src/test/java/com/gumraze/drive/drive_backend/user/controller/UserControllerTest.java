package com.gumraze.drive.drive_backend.user.controller;

import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.user.constants.UserStatus;
import com.gumraze.drive.drive_backend.user.dto.UserMeResponse;
import com.gumraze.drive.drive_backend.user.service.UserProfileService;
import com.gumraze.drive.drive_backend.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.nullValue;
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
    @Autowired
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
}
