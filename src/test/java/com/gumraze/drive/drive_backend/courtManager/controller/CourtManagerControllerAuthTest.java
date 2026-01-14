package com.gumraze.drive.drive_backend.courtManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.common.exception.ForbiddenException;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.courtManager.dto.MatchRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.RoundRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 인증, 권한 관련 테스트
@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
public class CourtManagerControllerAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FreeGameService freeGameService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @Test
    @DisplayName("라운드/매치 부분 수정 PATCH - 토큰 없음이면 401")
    void updateRoundMatch_without_token_then_unauthorized() throws Exception {
        // given
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/round-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - organizer 아니면 403")
    void updateRoundMatch_when_not_organizer_then_forbidden() throws Exception {
        // given
        Long gameId = 1L;
        Long userId = 2L;       // organizer 아님

        when(jwtAccessTokenValidator.validateAndGetUserId("token")).thenReturn(Optional.of(userId));
        when(freeGameService.updateFreeGameRoundMatch(eq(userId), eq(gameId), any()))
                .thenThrow(new ForbiddenException("Organizer 권한이 없습니다."));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(1L, 2L))
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()))
                        .build();

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
        ;

    }
}
