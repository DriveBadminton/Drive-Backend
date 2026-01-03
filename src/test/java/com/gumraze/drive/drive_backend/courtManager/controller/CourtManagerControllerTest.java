package com.gumraze.drive.drive_backend.courtManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FreeGameService freeGameService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @Test()
    @DisplayName("최소 필수값으로 자유게임 생성 성공")
    void createFreeGame_success() throws Exception {
        // given: 최소 필수값만 포함된 생성 요청을 준비함.
        CreateFreeGameResponse response = new CreateFreeGameResponse();
        response.setGameId(101L);           // 게임 Id를 101으로 설정

        // 서비스 응답을 stub
        when(freeGameService.createFreeGame(anyLong(), any()))
            .thenReturn(response);

        // 토큰 검증을 stub
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // request 객체 생성
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임1")
                .courtCount(2)
                .roundCount(3)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // when: /games로 POST 요청을 보냄
        // then: 201과 응답 바디 구조/값이 일치하는지 확인
        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.gameId").value(101));
    }
}