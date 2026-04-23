package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.common.exception.NotFoundException;
import com.gumraze.rallyon.backend.common.exception.ServiceUnavailableException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.*;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.*;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.*;
import com.gumraze.rallyon.backend.courtManager.application.port.in.result.SubmitFreeGameAssignmentPreviewJobResult;
import com.gumraze.rallyon.backend.courtManager.constants.AssignmentPreviewJobStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchRecordMode;
import com.gumraze.rallyon.backend.courtManager.constants.MatchResult;
import com.gumraze.rallyon.backend.courtManager.constants.MatchStatus;
import com.gumraze.rallyon.backend.courtManager.constants.MatchWinnerTeam;
import com.gumraze.rallyon.backend.courtManager.constants.RoundStatus;
import com.gumraze.rallyon.backend.courtManager.dto.*;
import com.gumraze.rallyon.backend.security.config.SecurityConfig;
import com.gumraze.rallyon.backend.user.constants.Gender;
import com.gumraze.rallyon.backend.user.constants.Grade;
import com.gumraze.rallyon.backend.user.constants.GradeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private CreateFreeGameUseCase createFreeGameUseCase;
    @MockitoBean private SubmitFreeGameAssignmentPreviewUseCase submitFreeGameAssignmentPreviewUseCase;
    @MockitoBean private GetFreeGameAssignmentPreviewStatusUseCase getFreeGameAssignmentPreviewStatusUseCase;
    @MockitoBean private GetFreeGameDetailUseCase getFreeGameDetailUseCase;
    @MockitoBean private UpdateFreeGameInfoUseCase updateFreeGameInfoUseCase;
    @MockitoBean private StartFreeGameUseCase startFreeGameUseCase;
    @MockitoBean private StartFreeGameMatchUseCase startFreeGameMatchUseCase;
    @MockitoBean private CompleteFreeGameMatchUseCase completeFreeGameMatchUseCase;
    @MockitoBean private GetFreeGameRoundsAndMatchesUseCase getFreeGameRoundsAndMatchesUseCase;
    @MockitoBean private UpdateFreeGameRoundsAndMatchesUseCase updateFreeGameRoundsAndMatchesUseCase;
    @MockitoBean private AddFreeGameParticipantUseCase addFreeGameParticipantUseCase;
    @MockitoBean private GetFreeGameParticipantsUseCase getFreeGameParticipantsUseCase;
    @MockitoBean private GetFreeGameParticipantDetailUseCase getFreeGameParticipantDetailUseCase;
    @MockitoBean private GetPublicFreeGameDetailUseCase getPublicFreeGameDetailUseCase;

    @MockitoBean private CreateFreeGameCommandMapper createFreeGameCommandMapper;
    @MockitoBean private CreateFreeGameAssignmentPreviewCommandMapper createFreeGameAssignmentPreviewCommandMapper;
    @MockitoBean private AssignmentPreviewJobResponseMapper assignmentPreviewJobResponseMapper;
    @MockitoBean private UpdateFreeGameInfoCommandMapper updateFreeGameInfoCommandMapper;
    @MockitoBean private UpdateFreeGameRoundsAndMatchesCommandMapper updateFreeGameRoundsAndMatchesCommandMapper;
    @MockitoBean private AddFreeGameParticipantCommandMapper addFreeGameParticipantCommandMapper;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("자유게임 생성 요청을 create use case로 전달한다")
    void createFreeGame_withValidRequest_returnsCreated() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        CreateFreeGameRequest request = new CreateFreeGameRequest(
                "자유게임1",
                null,
                GradeType.NATIONAL,
                2,
                3,
                "2026-04-01T15:10",
                "잠실 배드민턴장",
                null,
                List.of(),
                List.of()
        );
        CreateFreeGameCommand command = new CreateFreeGameCommand(
                "자유게임1",
                null,
                GradeType.NATIONAL,
                2,
                3,
                "2026-04-01T15:10",
                "잠실 배드민턴장",
                null,
                List.of(),
                List.of()
        );

        given(createFreeGameCommandMapper.toCommand(request)).willReturn(command);
        given(createFreeGameUseCase.create(accountId, command)).willReturn(gameId);

        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(accountId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/free-games/" + gameId))
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));

        verify(createFreeGameCommandMapper).toCommand(request);
        verify(createFreeGameUseCase).create(accountId, command);
    }

    @Test
    @DisplayName("코트 배정 프리뷰 생성 시 응답 본문을 반환한다")
    void createFreeGameAssignmentPreview_withValidRequest_returnsPreviewResponse() throws Exception {
        // given: 유효한 프리뷰 생성 요청과 변환 결과 준비
        UUID accountId = UUID.randomUUID();
        CreateFreeGameAssignmentPreviewRequest request = previewRequest();
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();
        SubmitFreeGameAssignmentPreviewJobResult result =
                new SubmitFreeGameAssignmentPreviewJobResult(
                        UUID.randomUUID(),
                        AssignmentPreviewJobStatus.QUEUED,
                        1000
                );
        CreateFreeGameAssignmentPreviewJobResponse response =
                new CreateFreeGameAssignmentPreviewJobResponse(
                        result.jobId(),
                        result.status().name(),
                        result.pollAfterMs()
                );

        given(createFreeGameAssignmentPreviewCommandMapper.toCommand(request)).willReturn(command);
        given(submitFreeGameAssignmentPreviewUseCase.submit(accountId, command)).willReturn(result);
        given(assignmentPreviewJobResponseMapper.toSubmitResponse(result)).willReturn(response);

        // when: 프리뷰 생성 요청 수행
        mockMvc.perform(post("/free-games/assignment-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(accountId))
                        .content(objectMapper.writeValueAsString(request)))
                // then: 프리뷰 응답 본문 반환 검증
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(result.jobId().toString()))
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andExpect(jsonPath("$.pollAfterMs").value(1000));

        verify(createFreeGameAssignmentPreviewCommandMapper).toCommand(request);
        verify(submitFreeGameAssignmentPreviewUseCase).submit(accountId, command);
        verify(assignmentPreviewJobResponseMapper).toSubmitResponse(result);
    }

    @Test
    @DisplayName("코트 배정 프리뷰 job 상태 조회 시 완료 응답 본문을 반환한다")
    void getFreeGameAssignmentPreviewStatus_withOwnedJob_returnsStatusResponse() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        LocalDateTime submittedAt = LocalDateTime.of(2026, 4, 13, 14, 0);
        LocalDateTime startedAt = submittedAt.plusSeconds(1);
        LocalDateTime completedAt = startedAt.plusSeconds(2);
        var result = new com.gumraze.rallyon.backend.courtManager.application.port.in.result.GetFreeGameAssignmentPreviewJobStatusResult(
                jobId,
                AssignmentPreviewJobStatus.SUCCEEDED,
                new com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult(
                        List.of(
                                new com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult.Round(
                                        1,
                                        List.of(
                                                new com.gumraze.rallyon.backend.courtManager.application.port.in.result.CreateFreeGameAssignmentPreviewResult.Court(
                                                        1,
                                                        Arrays.asList(1L, 2L, null, null)
                                                )
                                        )
                                )
                        ),
                        List.of()
                ),
                null,
                submittedAt,
                startedAt,
                completedAt
        );
        GetFreeGameAssignmentPreviewJobResponse response =
                new GetFreeGameAssignmentPreviewJobResponse(
                        jobId,
                        "SUCCEEDED",
                        new CreateFreeGameAssignmentPreviewResponse(
                                List.of(
                                        new CreateFreeGameAssignmentPreviewResponse.RoundResponse(
                                                1,
                                                List.of(
                                                        new CreateFreeGameAssignmentPreviewResponse.CourtResponse(
                                                                1,
                                                                Arrays.asList(1L, 2L, null, null)
                                                        )
                                                )
                                        )
                                ),
                                List.of()
                        ),
                        null,
                        submittedAt,
                        startedAt,
                        completedAt
                );

        given(getFreeGameAssignmentPreviewStatusUseCase.getStatus(accountId, jobId)).willReturn(result);
        given(assignmentPreviewJobResponseMapper.toStatusResponse(result)).willReturn(response);

        mockMvc.perform(get("/free-games/assignment-previews/{jobId}", jobId)
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.preview.rounds[0].roundNumber").value(1))
                .andExpect(jsonPath("$.preview.rounds[0].courts[0].slots[0]").value(1));

        verify(getFreeGameAssignmentPreviewStatusUseCase).getStatus(accountId, jobId);
        verify(assignmentPreviewJobResponseMapper).toStatusResponse(result);
    }

    @Test
    @DisplayName("자유게임 상세 조회 성공")
    void getFreeGameDetail_withExistingGame_returnsOk() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        FreeGameDetailResponse response = freeGameDetailResponse(accountId, gameId);

        when(getFreeGameDetailUseCase.get(new GetFreeGameDetailQuery(accountId, gameId))).thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}", gameId)
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.title").value("자유게임"));
    }

    @Test
    @DisplayName("자유게임 기본 정보 수정 성공")
    void updateFreeGameInfo_withValidRequest_returnsOk() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UpdateFreeGameRequest request = new UpdateFreeGameRequest(
                "수정된 자유게임",
                MatchRecordMode.WINNER_ONLY,
                GradeType.REGIONAL,
                "2026-04-02T18:20",
                null,
                null
        );
        UpdateFreeGameInfoCommand command = new UpdateFreeGameInfoCommand(
                accountId,
                gameId,
                request.title(),
                request.matchRecordMode(),
                request.gradeType(),
                request.scheduledAt(),
                request.location(),
                request.managerIds()
        );
        UpdateFreeGameResponse response = new UpdateFreeGameResponse(gameId);

        when(updateFreeGameInfoCommandMapper.toCommand(eq(accountId), eq(gameId), any(UpdateFreeGameRequest.class)))
                .thenReturn(command);
        when(updateFreeGameInfoUseCase.update(command)).thenReturn(response);

        mockMvc.perform(patch("/free-games/{gameId}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(accountId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @DisplayName("자유게임 시작 요청 성공")
    void startFreeGame_returnsOk() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        StartFreeGameCommand command = new StartFreeGameCommand(accountId, gameId);
        given(startFreeGameUseCase.start(command)).willReturn(new UpdateFreeGameResponse(gameId));

        mockMvc.perform(post("/free-games/{gameId}/start", gameId)
                        .with(authenticatedUser(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));

        verify(startFreeGameUseCase).start(command);
    }

    @Test
    @DisplayName("자유게임 매치 시작 요청 성공")
    void startFreeGameMatch_returnsNoContent() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        mockMvc.perform(post("/free-games/{gameId}/rounds/{roundNumber}/matches/{courtNumber}/start", gameId, 1, 2)
                        .with(authenticatedUser(accountId)))
                .andExpect(status().isNoContent());

        verify(startFreeGameMatchUseCase).start(new StartFreeGameMatchCommand(accountId, gameId, 1, 2));
    }

    @Test
    @DisplayName("자유게임 매치 종료 요청 성공")
    void completeFreeGameMatch_returnsNoContent() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        CompleteFreeGameMatchRequest request = new CompleteFreeGameMatchRequest(
                MatchWinnerTeam.TEAM_A,
                null,
                null
        );

        mockMvc.perform(post("/free-games/{gameId}/rounds/{roundNumber}/matches/{courtNumber}/complete", gameId, 1, 2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(accountId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(completeFreeGameMatchUseCase).complete(
                new CompleteFreeGameMatchCommand(
                        accountId,
                        gameId,
                        1,
                        2,
                        MatchWinnerTeam.TEAM_A,
                        null,
                        null
                )
        );
    }

    @Test
    @DisplayName("자유게임 라운드/매치 조회 성공")
    void getFreeGameRoundMatch_withExistingGame_returnsOk() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID teamA1 = UUID.randomUUID();
        UUID teamA2 = UUID.randomUUID();
        UUID teamB1 = UUID.randomUUID();
        UUID teamB2 = UUID.randomUUID();

        FreeGameRoundMatchResponse response = new FreeGameRoundMatchResponse(
                gameId,
                List.of(
                        new FreeGameRoundResponse(
                                1,
                                RoundStatus.NOT_STARTED,
                                List.of(
                                        new FreeGameMatchResponse(
                                                1L,
                                                List.of(teamA1, teamA2),
                                                List.of(teamB1, teamB2),
                                                MatchStatus.NOT_STARTED,
                                                MatchResult.NULL,
                                                null,
                                                null,
                                                true
                                        )
                                )
                        )
                )
        );

        when(getFreeGameRoundsAndMatchesUseCase.get(new GetFreeGameRoundsAndMatchesQuery(accountId, gameId)))
                .thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}/rounds-and-matches", gameId)
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.rounds[0].matches[0].courtNumber").value(1L))
                .andExpect(jsonPath("$.rounds[0].matches[0].teamAIds[0]").value(teamA1.toString()));
    }

    @Test
    @DisplayName("라운드/매치 수정 PATCH 성공")
    void updateRoundsAndMatches_withValidRequest_returnsOk() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID teamA1 = UUID.randomUUID();
        UUID teamA2 = UUID.randomUUID();
        UUID teamB1 = UUID.randomUUID();
        UUID teamB2 = UUID.randomUUID();

        UpdateFreeGameRoundMatchRequest request = new UpdateFreeGameRoundMatchRequest(
                List.of(
                        new RoundRequest(
                                1,
                                List.of(
                                        new MatchRequest(
                                                1,
                                                List.of(teamA1, teamA2),
                                                List.of(teamB1, teamB2)
                                        )
                                )
                        )
                )
        );
        UpdateFreeGameRoundsAndMatchesCommand command = new UpdateFreeGameRoundsAndMatchesCommand(
                accountId,
                gameId,
                List.of(new UpdateFreeGameRoundsAndMatchesCommand.Round(
                        1,
                        List.of(new UpdateFreeGameRoundsAndMatchesCommand.Match(
                                1,
                                List.of(teamA1, teamA2),
                                List.of(teamB1, teamB2)
                        ))
                ))
        );

        when(updateFreeGameRoundsAndMatchesCommandMapper.toCommand(
                eq(accountId),
                eq(gameId),
                any(UpdateFreeGameRoundMatchRequest.class)
        )).thenReturn(command);

        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(accountId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(updateFreeGameRoundsAndMatchesUseCase).update(command);
    }

    @Test
    @DisplayName("참가자 추가 성공")
    void addFreeGameParticipant_withValidRequest_returnsCreated() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        AddFreeGameParticipantRequest request = new AddFreeGameParticipantRequest(
                null,
                "참가자",
                Gender.MALE,
                Grade.ROOKIE,
                20
        );
        AddFreeGameParticipantCommand command = new AddFreeGameParticipantCommand(
                null,
                "참가자",
                Gender.MALE,
                Grade.ROOKIE,
                20
        );

        when(addFreeGameParticipantCommandMapper.toCommand(request)).thenReturn(command);
        when(addFreeGameParticipantUseCase.add(accountId, gameId, command)).thenReturn(participantId);

        mockMvc.perform(post("/free-games/{gameId}/participants", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(accountId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/free-games/" + gameId + "/participants/" + participantId))
                .andExpect(jsonPath("$.participantId").value(participantId.toString()));
    }

    @Test
    @DisplayName("자유게임 참가자 목록 조회 성공")
    void getFreeGameParticipantsWithStats_withExistingGame_returnsOk() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID participantAccountId = UUID.randomUUID();
        FreeGameParticipantResponse participant = participantResponse(UUID.randomUUID(), participantAccountId, "KimA");
        FreeGameParticipantsResponse response =
                participantsResponse(gameId, List.of(participantResponseWithStats(participant, 3, 2, 1, 1)));

        when(getFreeGameParticipantsUseCase.get(new GetFreeGameParticipantsQuery(accountId, gameId, true)))
                .thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}/participants", gameId)
                        .queryParam("include", "stats")
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants[0].accountId").value(participantAccountId.toString()))
                .andExpect(jsonPath("$.participants[0].assignedMatchCount").value(3));
    }

    @Test
    @DisplayName("자유게임 참가자 상세 조회 성공")
    void getFreeGameParticipantDetail_withExistingParticipant_returnsOk() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        UUID participantAccountId = UUID.randomUUID();
        FreeGameParticipantDetailResponse response =
                participantDetailResponse(gameId, participantId, participantAccountId, "KimA");

        when(getFreeGameParticipantDetailUseCase.get(new GetFreeGameParticipantDetailQuery(accountId, gameId, participantId)))
                .thenReturn(response);

        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participantId").value(participantId.toString()))
                .andExpect(jsonPath("$.accountId").value(participantAccountId.toString()));
    }

    @Test
    @DisplayName("공개 자유게임 상세 조회 성공")
    void getPublicFreeGameDetail_withExistingGame_returnsOk() throws Exception {
        String shareCode = "public-share-code";
        UUID organizerId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        FreeGameDetailResponse response = freeGameDetailResponse(organizerId, gameId);

        when(getPublicFreeGameDetailUseCase.get(new GetPublicFreeGameDetailQuery(shareCode))).thenReturn(response);

        mockMvc.perform(get("/free-games/share/{shareCode}", shareCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @DisplayName("참가자 상세 조회 시 존재하지 않는 participantId면 실패")
    void getFreeGameParticipantDetail_withUnknownParticipant_returnsNotFound() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        when(getFreeGameParticipantDetailUseCase.get(new GetFreeGameParticipantDetailQuery(accountId, gameId, participantId)))
                .thenThrow(new NotFoundException("존재하지 않는 참가자입니다. participantId: " + participantId));

        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("존재하지 않는 참가자입니다. participantId: " + participantId));
    }

    @Test
    @DisplayName("코트 배정 프리뷰 AI 생성 시 AI 서비스를 사용할 수 없으면 503을 반환한다")
    void createFreeGameAssignmentPreview_whenAiServiceUnavailable_returnsServiceUnavailable() throws Exception {
        // given: 유효한 프리뷰 생성 요청과 command 반환 결과를 준비
        UUID accountId = UUID.randomUUID();
        CreateFreeGameAssignmentPreviewRequest request = previewRequest();
        CreateFreeGameAssignmentPreviewCommand command = previewCommand();


        given(createFreeGameAssignmentPreviewCommandMapper.toCommand(request))
                .willReturn(command);
        given(submitFreeGameAssignmentPreviewUseCase.submit(accountId, command))
                .willThrow(new ServiceUnavailableException("AI 코트 배정 프리뷰를 현재 생성할 수 없습니다."));

        // when: 프리뷰 생성 요청을 수행한다.
        mockMvc.perform(post("/free-games/assignment-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(accountId))
                        .content(objectMapper.writeValueAsString(request)))
                // then: 503 problem detail 응답을 반환한다.
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value("AI 코트 배정 프리뷰를 현재 생성할 수 없습니다."));

    }

    // helper method
    private CreateFreeGameAssignmentPreviewRequest previewRequest() {
        return new CreateFreeGameAssignmentPreviewRequest(
                List.of(
                        new CreateFreeGameAssignmentPreviewRequest.ParticipantRequest(
                                1L,
                                "서승재",
                                Gender.MALE,
                                20,
                                Grade.S,
                                1
                        ),
                        new CreateFreeGameAssignmentPreviewRequest.ParticipantRequest(
                                2L,
                                "김원호",
                                Gender.MALE,
                                20,
                                Grade.A,
                                0
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewRequest.RoundRequest(
                                1,
                                List.of(
                                        new CreateFreeGameAssignmentPreviewRequest.CourtRequest(
                                                1,
                                                Arrays.asList(1L, null, null, null)
                                        )
                                )
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewRequest.PartnerPairRequest(1L, 2L)
                ),
                new CreateFreeGameAssignmentPreviewRequest.PreferencesRequest(
                        CreateFreeGameAssignmentPreviewRequest.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewRequest.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
    }

    private CreateFreeGameAssignmentPreviewCommand previewCommand() {
        return new CreateFreeGameAssignmentPreviewCommand(
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.Participant(
                                1L,
                                "서승재",
                                Gender.MALE,
                                20,
                                Grade.S,
                                1
                        ),
                        new CreateFreeGameAssignmentPreviewCommand.Participant(
                                2L,
                                "김원호",
                                Gender.MALE,
                                20,
                                Grade.A,
                                0
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.Round(
                                1,
                                List.of(
                                        new CreateFreeGameAssignmentPreviewCommand.Court(
                                                1,
                                                Arrays.asList(1L, null, null, null)
                                        )
                                )
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewCommand.PartnerPairs(1L, 2L)
                ),
                new CreateFreeGameAssignmentPreviewCommand.Preferences(
                        CreateFreeGameAssignmentPreviewCommand.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewCommand.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );
    }

}
