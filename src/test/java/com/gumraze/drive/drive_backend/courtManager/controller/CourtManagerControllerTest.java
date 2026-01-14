package com.gumraze.drive.drive_backend.courtManager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gumraze.drive.drive_backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.drive.drive_backend.common.exception.NotFoundException;
import com.gumraze.drive.drive_backend.config.SecurityConfig;
import com.gumraze.drive.drive_backend.courtManager.constants.*;
import com.gumraze.drive.drive_backend.courtManager.dto.*;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import com.gumraze.drive.drive_backend.user.constants.Gender;
import com.gumraze.drive.drive_backend.user.constants.Grade;
import com.gumraze.drive.drive_backend.user.constants.GradeType;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(3)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // when: /free-games로 POST 요청을 보냄
        // then: 201과 응답 바디 구조/값이 일치하는지 확인
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.gameId").value(101));
    }

    @Test
    @DisplayName("자유게임 생성 시, title 누락")
    void createFreeGame_without_title() throws Exception {
        // given: 자유 게임 생성 시, title 누락
        // request 객체 생성
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title(null)
                .courtCount(2)
                .roundCount(3)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // 유효한 token
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when: 자유 게임 생성 호출
        // then: VALIDATION_ERROR 발생
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, courtCount 누락")
    void createFreeGame_without_courtCount() throws Exception {
        // given: 자유게임 생성 시, courtCount 누락
        // request
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("테스트 게임")
                .roundCount(3)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // 유효한 토큰
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then: 자유게임 생성 호출 시 VALIDATION_ERROR 발생
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, roundCount 누락 테스트")
    void createFreeGame_without_roundCount() throws Exception {
        // given: 자유게임 생성 시, roundCount 누락
        // request
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("테스트 게임")
                .courtCount(2)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // 유효한 토큰
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then: 자유게임 생성 호출 시 VALIDATION_ERROR 발생
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, courtCount, roundCount 누락 테스트")
    void createFreeGame_without_courtCount_and_roundCount() throws Exception {
        // given: 자유게임 생성 시, courtCount, roundCount 누락
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .build();

        String body = objectMapper.writeValueAsString(request);

        // jwt 검증
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then: 자유게임 생성 호출 시 VALIDATION_ERROR 발생
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, participant가 존재할 때 최소 필수 항목이 존재하면 성공 테스트")
    void createFreeGame_with_participant() throws Exception {
        // given: 자유게임 생성 시, 참가자가 존재할 때 -> 최소 필드 값이 존재하면 성공
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .courtCount(2)
                .gradeType(GradeType.NATIONAL)
                .roundCount(3)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("참가자 1")
                                        .gender(Gender.MALE)
                                        .grade(Grade.ROOKIE)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        String body = objectMapper.writeValueAsString(request);

        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("CREATED"))
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, participant가 존재할 때 최소 필수 항목 없는 경우 실패 테스트")
    void createFreeGame_with_participant_without_gender() throws Exception {
        // given: 참가자는 있지만, gender가 없는 경우
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .courtCount(2)
                .roundCount(3)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("참가자 1")
                                        .grade(Grade.ROOKIE)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        String body = objectMapper.writeValueAsString(request);

        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, 사용자 인증 누락 실패 테스트")
    void createFreeGame_without_token() throws Exception {
        // given: 참가자는 있지만, gender가 없는 경우
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임 1")
                .courtCount(2)
                .roundCount(3)
                .participants(
                        List.of(
                                ParticipantCreateRequest.builder()
                                        .originalName("참가자 1")
                                        .gender(Gender.MALE)
                                        .grade(Grade.ROOKIE)
                                        .ageGroup(20)
                                        .build()
                        )
                )
                .build();

        String body = objectMapper.writeValueAsString(request);

        // 비어있는 토큰 설정
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, GradeType이 비어있으면 실패 테스트")
    void createFreeGame_without_gradeType() throws Exception {
        // given: 자유게임 생성 시 gradeType이 비어있을 경우
        CreateFreeGameRequest request = CreateFreeGameRequest.builder()
                .title("자유게임")
                .gradeType(null)
                .courtCount(2)
                .roundCount(3)
                .build();

        // 유효한 토큰
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // mapping
        String body = objectMapper.writeValueAsString(request);

        // when & then: 자유게임 생성 시 400 에러 발생
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("자유게임 생성 시, GradeType에 틀린 값이 입력되면 실패 테스트")
    void createFreeGame_with_invalid_gradeType() throws Exception {
        // given: GradeType에 틀린 값이 입력될 경우
        String body = """
                {
                  "title": "자유게임",
                  "gradeType": "REGION",
                  "courtCount": 2,
                  "roundCount": 3
                }
                """;

        // 유효 토큰 설정
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(1L));

        // when & then: 자유게임 생성 시 400 에러 발생
        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        ;
    }

    @Test
    @DisplayName("자유게임 상세 조회 성공 테스트")
    void getFreeGameDetail_success() throws Exception {
        // given
        Long userId = 99L;
        Long gameId = 1L;
        FreeGameDetailResponse response = buildFreeGameDetailResponse(userId, gameId);

        when(freeGameService.getFreeGameDetail(userId, gameId)).thenReturn(response);
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(userId));

        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.gameId").value(1))
                .andExpect(jsonPath("$.data.title").value("자유게임"))
                .andExpect(jsonPath("$.data.gameType").value("FREE"))
                .andExpect(jsonPath("$.data.gameStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.matchRecordMode").value("STATUS_ONLY"))
                .andExpect(jsonPath("$.data.gradeType").value("NATIONAL"))
                .andExpect(jsonPath("$.data.courtCount").value(2))
                .andExpect(jsonPath("$.data.roundCount").value(2))
                .andExpect(jsonPath("$.data.organizerId").value(99))
        ;
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 인증 실패")
    void getFreeGameDetail_without_token() throws Exception {
        // given
        Long userId = 99L;
        Long gameId = 1L;
        FreeGameDetailResponse response = buildFreeGameDetailResponse(gameId, userId);

        when(freeGameService.getFreeGameDetail(userId, gameId)).thenReturn(response);
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.empty());

        // when & then
        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        ;
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 존재하지 않는 gameId면 실패")
    void getFreeGameDetail_withUnknownGameId_returnError() throws Exception {
        // given
        Long userId = 99L;
        Long gameId = 1L;
        when(freeGameService.getFreeGameDetail(userId, gameId))
                .thenThrow(new NotFoundException("게임이 존재하지 않습니다."));
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(userId));

        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .header("Authorization", "Bearer token"))
                .andDo(print()) // 응답 로그 출력
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("게임이 존재하지 않습니다."));
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 성공 테스트")
    void updateFreeGameInfo_success() throws Exception {
        // given: 정상적인 요청 입력
        Long gameId = 80L;
        Long userId = 1L;

        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 자유게임")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();
        String body = objectMapper.writeValueAsString(request);
        UpdateFreeGameResponse response = UpdateFreeGameResponse.builder()
                .gameId(gameId)
                .build();

        // stub
        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(userId));
        when(freeGameService.updateFreeGameInfo(anyLong(), anyLong(), any(UpdateFreeGameRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/free-games/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.gameId").value(80))
        ;
    }

    @Test
    @DisplayName("자유게임 수정 시, token 검증")
    void updateFreeGameInfo_without_token() throws Exception {
        // given
        Long gameId = 80L;
        UpdateFreeGameRequest request = UpdateFreeGameRequest.builder()
                .title("수정된 자유게임")
                .matchRecordMode(MatchRecordMode.RESULT)
                .gradeType(GradeType.REGIONAL)
                .build();

        String body = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(patch("/free-games/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        ;
    }

    @Test
    @DisplayName("자유게임 라운드/매치 조회 성공 테스트")
    void getFreeGameRoundMatch_success() throws Exception {
        // given
        Long userId = 1L;
        Long gameId = 2L;

        FreeGameMatchResponse match =
                FreeGameMatchResponse.builder()
                        .courtNumber(1L)
                        .teamAIds(List.of(201L, 202L))
                        .teamBIds(List.of(203L, 204L))
                        .matchStatus(MatchStatus.NOT_STARTED)
                        .matchResult(MatchResult.NULL)
                        .isActive(true)
                        .build();

        FreeGameRoundResponse round =
                FreeGameRoundResponse.builder()
                        .roundNumber(1L)
                        .roundStatus(RoundStatus.NOT_STARTED)
                        .matches(List.of(match))
                        .build();

        FreeGameRoundMatchResponse response =
                FreeGameRoundMatchResponse.builder()
                        .gameId(gameId)
                        .rounds(List.of(round))
                        .build();

        when(freeGameService.getFreeGameRoundMatchResponse(userId, gameId))
                .thenReturn(response);

        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(userId));

        // when & then
        mockMvc.perform(get("/free-games/{gameId}/rounds-and-matches", gameId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.gameId").value(2L))
                .andExpect(jsonPath("$.data.rounds[0].roundNumber").value(1L))
                .andExpect(jsonPath("$.data.rounds[0].matches[0].courtNumber").value(1L))
                .andExpect(jsonPath("$.data.rounds[0].matches[0].teamAIds[0]").value(201L))
                .andExpect(jsonPath("$.data.rounds[0].matches[0].teamAIds[1]").value(202L))
                .andExpect(jsonPath("$.data.rounds[0].matches[0].teamBIds[0]").value(203L))
                .andExpect(jsonPath("$.data.rounds[0].matches[0].teamBIds[1]").value(204L))
                .andExpect(jsonPath("$.data.rounds[0].matches[0].matchStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data.rounds[0].matches[0].matchResult").value("NULL"))
                .andExpect(jsonPath("$.data.rounds[0].matches[0].isActive").value(true))
        ;
    }

    @Test
    @DisplayName("라운드/매치 부분 수정 PATCH 성공")
    void updateRoundsAndMatches_patch_success() throws Exception {
        // given
        Long userId = 1L;
        Long gameId = 1L;

        when(jwtAccessTokenValidator.validateAndGetUserId("token"))
                .thenReturn(Optional.of(userId));

        when(freeGameService.updateFreeGameRoundMatch(anyLong(), anyLong(), any()))
                .thenReturn(new UpdateFreeGameRoundMatchResponse(gameId));

        UpdateFreeGameRoundMatchRequest request =
                UpdateFreeGameRoundMatchRequest.builder()
                        .rounds(List.of(
                                RoundRequest.builder()
                                        .roundNumber(1)
                                        .matches(List.of(
                                                MatchRequest.builder()
                                                        .courtNumber(1)
                                                        .teamAIds(List.of(101L, 102L))
                                                        .teamBIds(List.of(103L, 104L))
                                                        .build()
                                        ))
                                        .build()
                        ))
                        .build();

        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

    }

    /*
     * Test Helper Method
     */
    private FreeGameDetailResponse buildFreeGameDetailResponse(Long organizerId, Long gameId) {
        return FreeGameDetailResponse.builder()
                .gameId(gameId)
                .title("자유게임")
                .gameType(GameType.FREE)
                .gameStatus(GameStatus.NOT_STARTED)
                .matchRecordMode(MatchRecordMode.STATUS_ONLY)
                .gradeType(GradeType.NATIONAL)
                .courtCount(2)
                .roundCount(2)
                .organizerId(organizerId)
                .build();
    }
}