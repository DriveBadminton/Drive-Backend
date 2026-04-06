package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

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
import com.gumraze.rallyon.backend.courtManager.dto.AddFreeGameParticipantRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourtManagerController.class)
@Import(SecurityConfig.class)
class CourtManagerControllerValidationTest {

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
    @DisplayName("자유게임 생성 시 title 누락이면 400")
    void createFreeGame_without_title() throws Exception {
        CreateFreeGameRequest request = new CreateFreeGameRequest(
                null,
                null,
                GradeType.NATIONAL,
                2,
                3,
                null,
                null,
                List.of(),
                List.of(),
                List.of()
        );

        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("자유게임 생성 시 scheduledAt 누락이면 400")
    void createFreeGame_without_scheduled_at() throws Exception {
        CreateFreeGameRequest request = new CreateFreeGameRequest(
                "자유게임",
                null,
                GradeType.NATIONAL,
                2,
                3,
                null,
                null,
                List.of(),
                List.of(),
                List.of()
        );

        mockMvc.perform(post("/free-games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("코트 배정 프리뷰 생성 시 preferences 누락이면 400")
    void createAssignmentPreview_without_preferences_then_bad_request() throws Exception {
        CreateFreeGameAssignmentPreviewRequest request = new CreateFreeGameAssignmentPreviewRequest(
                List.of(validParticipantRequest()),
                List.of(validRoundRequest()),
                null,
                null
        );

        mockMvc.perform(post("/free-games/assignment-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("코트 배정 프리뷰 생성 시 participants가 비어 있으면 400")
    void createAssignmentPreview_without_participants_then_bad_request() throws Exception {
        CreateFreeGameAssignmentPreviewRequest request = new CreateFreeGameAssignmentPreviewRequest(
                List.of(),
                List.of(validRoundRequest()),
                null,
                validPreferencesRequest()
        );

        mockMvc.perform(post("/free-games/assignment-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("코트 배정 프리뷰 생성 시 slots 길이가 4가 아니면 400")
    void createAssignmentPreview_with_invalid_slot_size_then_bad_request() throws Exception {
        CreateFreeGameAssignmentPreviewRequest request = new CreateFreeGameAssignmentPreviewRequest(
                List.of(validParticipantRequest()),
                List.of(
                        new CreateFreeGameAssignmentPreviewRequest.RoundRequest(
                                1,
                                List.of(
                                        new CreateFreeGameAssignmentPreviewRequest.CourtRequest(
                                                1,
                                                Arrays.asList("p1", null, null)
                                        )
                                )
                        )
                ),
                null,
                validPreferencesRequest()
        );

        mockMvc.perform(post("/free-games/assignment-previews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("라운드/매치 수정 시 rounds 누락이면 400")
    void updateRoundMatch_without_rounds_then_bad_request() throws Exception {
        UUID gameId = UUID.randomUUID();

        mockMvc.perform(patch("/free-games/{gameId}/rounds-and-matches", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("참가자 추가 시 gender 누락이면 400")
    void addParticipant_without_gender_then_bad_request() throws Exception {
        UUID gameId = UUID.randomUUID();
        AddFreeGameParticipantRequest request = new AddFreeGameParticipantRequest(
                null,
                "참가자",
                null,
                Grade.ROOKIE,
                20
        );

        mockMvc.perform(post("/free-games/{gameId}/participants", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("참가자 추가 시 이름 형식이 잘못되면 400")
    void addParticipant_with_invalid_name_then_bad_request() throws Exception {
        UUID gameId = UUID.randomUUID();
        AddFreeGameParticipantRequest request = new AddFreeGameParticipantRequest(
                null,
                "1234",
                Gender.MALE,
                Grade.ROOKIE,
                20
        );

        mockMvc.perform(post("/free-games/{gameId}/participants", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(authenticatedUser(UUID.randomUUID()))
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private CreateFreeGameAssignmentPreviewRequest.ParticipantRequest validParticipantRequest() {
        return new CreateFreeGameAssignmentPreviewRequest.ParticipantRequest(
                "p1",
                "서승재",
                Gender.MALE,
                20,
                Grade.S,
                1
        );
    }

    private CreateFreeGameAssignmentPreviewRequest.RoundRequest validRoundRequest() {
        return new CreateFreeGameAssignmentPreviewRequest.RoundRequest(
                1,
                List.of(
                        new CreateFreeGameAssignmentPreviewRequest.CourtRequest(
                                1,
                                Arrays.asList("p1", null, null, null)
                        )
                )
        );
    }

    private CreateFreeGameAssignmentPreviewRequest.PreferencesRequest validPreferencesRequest() {
        return new CreateFreeGameAssignmentPreviewRequest.PreferencesRequest(
                CreateFreeGameAssignmentPreviewRequest.PartnerPolicy.PREFER_PARTNERS,
                CreateFreeGameAssignmentPreviewRequest.ExistingAssignmentPolicy.FILL_EMPTY_SLOTS
        );
    }
}
