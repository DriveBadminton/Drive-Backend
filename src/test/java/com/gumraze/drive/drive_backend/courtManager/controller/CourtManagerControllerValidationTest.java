package com.gumraze.drive.drive_backend.courtManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.courtManager.dto.MatchRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.RoundRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import org.junit.jupiter.api.BeforeEach;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 요청 값 검증 test
@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
public class CourtManagerControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FreeGameService freeGameService;

    @MockitoBean
    private JwtAccessTokenValidator jwtAccessTokenValidator;

    @BeforeEach
    void setUp() {
        when(jwtAccessTokenValidator.validateAndGetUserId("token")).thenReturn(Optional.of(1L));
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - rounds 누락이면 400")
    void updateRoundMatch_without_rounds_then_bad_request() throws Exception {
        // given
        // 라운드 누락
        String body = "{}";

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - roundNumber 누락이면 400")
    void updateRoundMatch_without_roundNumber_then_bad_request() throws Exception {
        // given
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(null)  // 누락
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

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - matches 누락이면 400")
    void updateRoundMatch_without_matches_then_bad_request() throws Exception {
        // given
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(null)  // match 누락
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - teamAIds 누락이면 400")
    void updateRoundMatch_without_teamAIds_then_bad_request() throws Exception {
        // given
        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(null)
                                                        .teamBIds(List.of(3L, 4L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 - teamBIds 누락이면 400")
    void updateRoundMatch_without_teamBIds_then_bad_request() throws Exception {
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
                                                        .teamBIds(null)
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

}
