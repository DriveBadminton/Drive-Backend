package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.common.exception.ForbiddenException;
import com.gumraze.rallyon.backend.courtManager.application.port.in.AddFreeGameParticipantUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.CreateFreeGameAssignmentPreviewUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.CreateFreeGameUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameParticipantDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameParticipantsUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetFreeGameRoundsAndMatchesUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.GetPublicFreeGameDetailUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.UpdateFreeGameInfoUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.UpdateFreeGameRoundsAndMatchesUseCase;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.UpdateFreeGameRoundsAndMatchesCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetPublicFreeGameDetailQuery;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.authenticatedUser;
import static com.gumraze.rallyon.backend.courtManager.controller.support.CourtManagerControllerFixtures.freeGameDetailResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerAuthTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CreateFreeGameUseCase createFreeGameUseCase;
    @MockitoBean private CreateFreeGameAssignmentPreviewUseCase createFreeGameAssignmentPreviewUseCase;
    @MockitoBean private GetFreeGameDetailUseCase getFreeGameDetailUseCase;
    @MockitoBean private UpdateFreeGameInfoUseCase updateFreeGameInfoUseCase;
    @MockitoBean private GetFreeGameRoundsAndMatchesUseCase getFreeGameRoundsAndMatchesUseCase;
    @MockitoBean private UpdateFreeGameRoundsAndMatchesUseCase updateFreeGameRoundsAndMatchesUseCase;
    @MockitoBean private AddFreeGameParticipantUseCase addFreeGameParticipantUseCase;
    @MockitoBean private GetFreeGameParticipantsUseCase getFreeGameParticipantsUseCase;
    @MockitoBean private GetFreeGameParticipantDetailUseCase getFreeGameParticipantDetailUseCase;
    @MockitoBean private GetPublicFreeGameDetailUseCase getPublicFreeGameDetailUseCase;
    @MockitoBean private CreateFreeGameCommandMapper createFreeGameCommandMapper;
    @MockitoBean private CreateFreeGameAssignmentPreviewCommandMapper createFreeGameAssignmentPreviewCommandMapper;
    @MockitoBean private UpdateFreeGameInfoCommandMapper updateFreeGameInfoCommandMapper;
    @MockitoBean private UpdateFreeGameRoundsAndMatchesCommandMapper updateFreeGameRoundsAndMatchesCommandMapper;
    @MockitoBean private AddFreeGameParticipantCommandMapper addFreeGameParticipantCommandMapper;
    @MockitoBean private CreateFreeGameAssignmentPreviewResponseMapper createFreeGameAssignmentPreviewResponseMapper;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("자유게임 생성 시 토큰이 없으면 401")
    void createFreeGame_without_token() throws Exception {
        CreateFreeGameRequest request = new CreateFreeGameRequest(
                "자유게임 1",
                null,
                GradeType.NATIONAL,
                2,
                3,
                null,
                null,
                null,
                List.of(new CreateFreeGameRequest.ParticipantRequest("p1", null, "참가자", Gender.MALE, Grade.ROOKIE, 20)),
                List.of()
        );

        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("자유게임 상세 조회 시 토큰이 없으면 401")
    void getFreeGameDetail_without_token() throws Exception {
        mockMvc.perform(get("/free-games/{gameId}", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("라운드/매치 수정 시 organizer 아니면 403")
    void updateRoundMatch_when_not_organizer_then_forbidden() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UpdateFreeGameRoundMatchRequest request = new UpdateFreeGameRoundMatchRequest(
                List.of(
                        new RoundRequest(
                                1,
                                List.of(
                                        new MatchRequest(
                                                1,
                                                List.of(UUID.randomUUID(), UUID.randomUUID()),
                                                List.of(UUID.randomUUID(), UUID.randomUUID())
                                        )
                                )
                        )
                )
        );
        UpdateFreeGameRoundsAndMatchesCommand command = new UpdateFreeGameRoundsAndMatchesCommand(accountId, gameId, List.of());

        when(updateFreeGameRoundsAndMatchesCommandMapper.toCommand(
                eq(accountId),
                eq(gameId),
                any(UpdateFreeGameRoundMatchRequest.class)
        )).thenReturn(command);
        when(updateFreeGameRoundsAndMatchesUseCase.update(eq(command)))
                .thenThrow(new ForbiddenException("Organizer 권한이 없습니다."));

        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .with(authenticatedUser(accountId))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("참가자 상세 조회 시 organizer 아니면 403")
    void getParticipantDetail_when_not_organizer_then_forbidden() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        when(getFreeGameParticipantDetailUseCase.get(new GetFreeGameParticipantDetailQuery(accountId, gameId, participantId)))
                .thenThrow(new ForbiddenException("Organizer 권한이 없습니다."));

        mockMvc.perform(get("/free-games/{gameId}/participants/{participantId}", gameId, participantId)
                        .with(authenticatedUser(accountId))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("공개 상세 조회는 토큰 없이도 접근 가능하다")
    void getPublicFreeGameDetail_without_token() throws Exception {
        String shareCode = "public-share-code";
        FreeGameDetailResponse response = freeGameDetailResponse(UUID.randomUUID(), UUID.randomUUID());
        when(getPublicFreeGameDetailUseCase.get(new GetPublicFreeGameDetailQuery(shareCode))).thenReturn(response);

        mockMvc.perform(get("/free-games/share/{shareCode}", shareCode)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(response.gameId().toString()));
    }

    @Test
    @DisplayName("코트 배정 프리뷰 생성 시 토큰이 없으면 401")
    void createFreeGameAssignmentPreview_withoutToken_returnsUnauthorized() throws Exception {
        // given: 유효한 프리뷰 생성 요청 준비
        CreateFreeGameAssignmentPreviewRequest request = new CreateFreeGameAssignmentPreviewRequest(
                List.of(
                        new CreateFreeGameAssignmentPreviewRequest.ParticipantRequest(
                                "p1",
                                "서승재",
                                Gender.MALE,
                                20,
                                Grade.SS,
                                1
                        ),
                        new CreateFreeGameAssignmentPreviewRequest.ParticipantRequest(
                                "p2",
                                "김원호",
                                Gender.MALE,
                                20,
                                Grade.SS,
                                1
                        )
                ),
                List.of(
                        new CreateFreeGameAssignmentPreviewRequest.RoundRequest(
                                1,
                                List.of(
                                        new CreateFreeGameAssignmentPreviewRequest.CourtRequest(
                                                1,
                                                Arrays.asList("p1", null, null, null)
                                        )
                                )
                        )
                ),
                null,
                new CreateFreeGameAssignmentPreviewRequest.PreferencesRequest(
                        CreateFreeGameAssignmentPreviewRequest.PartnerPolicy.PREFER_PARTNERS,
                        CreateFreeGameAssignmentPreviewRequest.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
                )
        );

        // when: 인증 없이 프리뷰 생성 요청 수행
        mockMvc.perform(post("/free-games/assignment-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PROBLEM_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                // then: 401 반환 검증
                .andExpect(status().isUnauthorized());
    }
}
