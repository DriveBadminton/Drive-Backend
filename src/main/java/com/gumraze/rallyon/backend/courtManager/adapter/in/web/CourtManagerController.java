package com.gumraze.rallyon.backend.courtManager.adapter.in.web;

import com.gumraze.rallyon.backend.api.courtManager.FreeGameCommandApi;
import com.gumraze.rallyon.backend.api.courtManager.FreeGameParticipantApi;
import com.gumraze.rallyon.backend.api.courtManager.FreeGameQueryApi;
import com.gumraze.rallyon.backend.api.courtManager.PublicFreeGameApi;
import com.gumraze.rallyon.backend.courtManager.application.port.in.*;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CompleteFreeGameMatchCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameAssignmentPreviewCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.CreateFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.StartFreeGameCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.command.StartFreeGameMatchCommand;
import com.gumraze.rallyon.backend.courtManager.application.port.in.query.*;
import com.gumraze.rallyon.backend.courtManager.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
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
    private final SubmitFreeGameAssignmentPreviewUseCase submitFreeGameAssignmentPreviewUseCase;
    private final GetFreeGameAssignmentPreviewStatusUseCase getFreeGameAssignmentPreviewStatusUseCase;
    private final GetFreeGameDetailUseCase getFreeGameDetailUseCase;
    private final UpdateFreeGameInfoUseCase updateFreeGameInfoUseCase;
    private final StartFreeGameUseCase startFreeGameUseCase;
    private final StartFreeGameMatchUseCase startFreeGameMatchUseCase;
    private final CompleteFreeGameMatchUseCase completeFreeGameMatchUseCase;
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
    private final AssignmentPreviewJobResponseMapper assignmentPreviewJobResponseMapper;

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
    public ResponseEntity<CreateFreeGameAssignmentPreviewJobResponse> createFreeGameAssignmentPreview(
            @AuthenticationPrincipal UUID accountId,
            @RequestBody @Valid CreateFreeGameAssignmentPreviewRequest request
    ) {
        CreateFreeGameAssignmentPreviewCommand command =
                createFreeGameAssignmentPreviewCommandMapper.toCommand(request);
        return ResponseEntity.accepted().body(
                assignmentPreviewJobResponseMapper.toSubmitResponse(
                        submitFreeGameAssignmentPreviewUseCase.submit(accountId, command)
                )
        );
    }

    @Override
    @GetMapping("/assignment-previews/{jobId}")
    public ResponseEntity<GetFreeGameAssignmentPreviewJobResponse> getFreeGameAssignmentPreviewStatus(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(
                assignmentPreviewJobResponseMapper.toStatusResponse(
                        getFreeGameAssignmentPreviewStatusUseCase.getStatus(accountId, jobId)
                )
        );
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
    @PostMapping("/{gameId}/start")
    public ResponseEntity<UpdateFreeGameResponse> startFreeGame(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId
    ) {
        return ResponseEntity.ok(
                startFreeGameUseCase.start(new StartFreeGameCommand(accountId, gameId))
        );
    }

    @Override
    @PostMapping("/{gameId}/rounds/{roundNumber}/matches/{courtNumber}/start")
    public ResponseEntity<Void> startFreeGameMatch(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @PathVariable Integer roundNumber,
            @PathVariable Integer courtNumber
    ) {
        startFreeGameMatchUseCase.start(
                new StartFreeGameMatchCommand(accountId, gameId, roundNumber, courtNumber)
        );
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{gameId}/rounds/{roundNumber}/matches/{courtNumber}/complete")
    public ResponseEntity<Void> completeFreeGameMatch(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @PathVariable Integer roundNumber,
            @PathVariable Integer courtNumber,
            @RequestBody @Valid CompleteFreeGameMatchRequest request
    ) {
        completeFreeGameMatchUseCase.complete(
                new CompleteFreeGameMatchCommand(
                        accountId,
                        gameId,
                        roundNumber,
                        courtNumber,
                        request.winnerTeam(),
                        request.teamAScore(),
                        request.teamBScore()
                )
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
