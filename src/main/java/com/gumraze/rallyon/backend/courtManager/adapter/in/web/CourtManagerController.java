package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.api.courtManager.FreeGameCommandApi;
import com.gumraze.rallyon.backend.api.courtManager.FreeGameParticipantApi;
import com.gumraze.rallyon.backend.api.courtManager.FreeGameQueryApi;
import com.gumraze.rallyon.backend.api.courtManager.PublicFreeGameApi;
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
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewResponse;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantDetailQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameParticipantsQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetFreeGameRoundsAndMatchesQuery;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.GetPublicFreeGameDetailQuery;
import com.gumraze.rallyon.backend.courtManager.dto.AddFreeGameParticipantRequest;
import com.gumraze.rallyon.backend.courtManager.dto.AddFreeGameParticipantResponse;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantDetailResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameParticipantsResponse;
import com.gumraze.rallyon.backend.courtManager.dto.FreeGameRoundMatchResponse;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/free-games")
@RequiredArgsConstructor
public class CourtManagerController implements
        FreeGameCommandApi,
        FreeGameQueryApi,
        FreeGameParticipantApi,
        PublicFreeGameApi {

    private final CreateFreeGameUseCase createFreeGameUseCase;
    private final CreateFreeGameAssignmentPreviewUseCase createFreeGameAssignmentPreviewUseCase;
    private final GetFreeGameDetailUseCase getFreeGameDetailUseCase;
    private final UpdateFreeGameInfoUseCase updateFreeGameInfoUseCase;
    private final GetFreeGameRoundsAndMatchesUseCase getFreeGameRoundsAndMatchesUseCase;
    private final UpdateFreeGameRoundsAndMatchesUseCase updateFreeGameRoundsAndMatchesUseCase;
    private final AddFreeGameParticipantUseCase addFreeGameParticipantUseCase;
    private final GetFreeGameParticipantsUseCase getFreeGameParticipantsUseCase;
    private final GetFreeGameParticipantDetailUseCase getFreeGameParticipantDetailUseCase;
    private final GetPublicFreeGameDetailUseCase getPublicFreeGameDetailUseCase;
    private final CreateFreeGameCommandMapper createFreeGameCommandMapper;
    private final CreateFreeGameAssignmentPreviewCommandMapper createFreeGameAssignmentPreviewCommandMapper;
    private final UpdateFreeGameInfoCommandMapper updateFreeGameInfoCommandMapper;
    private final UpdateFreeGameRoundsAndMatchesCommandMapper updateFreeGameRoundsAndMatchesCommandMapper;
    private final AddFreeGameParticipantCommandMapper addFreeGameParticipantCommandMapper;

    @Override
    @PostMapping
    public ResponseEntity<CreateFreeGameResponse> createFreeGame(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody @Valid CreateFreeGameRequest request
    ) {
        CreateFreeGameCommand command = createFreeGameCommandMapper.toCommand(request);
        UUID gameId = createFreeGameUseCase.create(accountId, command);
        return ResponseEntity.created(URI.create("/free-games/" + gameId))
                .body(new CreateFreeGameResponse(gameId));
    }

    @Override
    @PostMapping("/assignment-previews")
    public ResponseEntity<CreateFreeGameAssignmentPreviewResponse> createFreeGameAssignmentPreview(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody @Valid CreateFreeGameAssignmentPreviewRequest request
    ) {
        CreateFreeGameAssignmentPreviewCommand command = createFreeGameAssignmentPreviewCommandMapper.toCommand(request);
        createFreeGameAssignmentPreviewUseCase.create(command);
        return ResponseEntity.ok().build();
    }

    @Override
    @GetMapping("/{gameId}")
    public ResponseEntity<FreeGameDetailResponse> getFreeGameDetail(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId
    ) {
        return ResponseEntity.ok(
                getFreeGameDetailUseCase.get(new GetFreeGameDetailQuery(accountId, gameId))
        );
    }

    @Override
    @PatchMapping("/{gameId}")
    public ResponseEntity<UpdateFreeGameResponse> updateFreeGameInfo(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @RequestBody @Valid UpdateFreeGameRequest request
    ) {
        return ResponseEntity.ok(
                updateFreeGameInfoUseCase.update(updateFreeGameInfoCommandMapper.toCommand(accountId, gameId, request))
        );
    }

    @Override
    @GetMapping("/{gameId}/rounds-and-matches")
    public ResponseEntity<FreeGameRoundMatchResponse> getFreeGameRoundMatchResponse(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId
    ) {
        return ResponseEntity.ok(
                getFreeGameRoundsAndMatchesUseCase.get(new GetFreeGameRoundsAndMatchesQuery(accountId, gameId))
        );
    }

    @Override
    @PatchMapping("/{gameId}/rounds-and-matches")
    public ResponseEntity<Void> updateFreeGameRoundMatch(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @RequestBody @Valid UpdateFreeGameRoundMatchRequest request
    ) {
        updateFreeGameRoundsAndMatchesUseCase.update(
                updateFreeGameRoundsAndMatchesCommandMapper.toCommand(accountId, gameId, request)
        );
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{gameId}/participants")
    public ResponseEntity<AddFreeGameParticipantResponse> addFreeGameParticipant(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @RequestBody @Valid AddFreeGameParticipantRequest request
    ) {
        UUID participantId = addFreeGameParticipantUseCase.add(
                accountId,
                gameId,
                addFreeGameParticipantCommandMapper.toCommand(request)
        );
        return ResponseEntity.created(URI.create("/free-games/" + gameId + "/participants/" + participantId))
                .body(new AddFreeGameParticipantResponse(participantId));
    }

    @Override
    @GetMapping("/{gameId}/participants")
    public ResponseEntity<FreeGameParticipantsResponse> getFreeGameParticipants(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @RequestParam(name = "include", required = false) String include
    ) {
        boolean includeStats = "stats".equalsIgnoreCase(include);
        return ResponseEntity.ok(
                getFreeGameParticipantsUseCase.get(new GetFreeGameParticipantsQuery(accountId, gameId, includeStats))
        );
    }

    @Override
    @GetMapping("/{gameId}/participants/{participantId}")
    public ResponseEntity<FreeGameParticipantDetailResponse> getFreeGameParticipantDetail(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @PathVariable UUID participantId
    ) {
        return ResponseEntity.ok(
                getFreeGameParticipantDetailUseCase.get(
                        new GetFreeGameParticipantDetailQuery(accountId, gameId, participantId)
                )
        );
    }

    @Override
    @GetMapping("/share/{shareCode}")
    public ResponseEntity<FreeGameDetailResponse> getPublicFreeGameDetail(@PathVariable String shareCode) {
        return ResponseEntity.ok(
                getPublicFreeGameDetailUseCase.get(new GetPublicFreeGameDetailQuery(shareCode))
        );
    }
}
