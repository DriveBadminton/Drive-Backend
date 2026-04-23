package com.gumraze.rallyon.backend.api.courtManager;

import com.gumraze.rallyon.backend.api.common.ApiAuthValidationResponses;
import com.gumraze.rallyon.backend.api.common.ApiBearerAuth;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewJobResponse;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameAssignmentPreviewRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.dto.CompleteFreeGameMatchRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRequest;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameResponse;
import com.gumraze.rallyon.backend.courtManager.dto.UpdateFreeGameRoundMatchRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@ApiBearerAuth
public interface FreeGameCommandApi {

    @ApiAuthValidationResponses
    ResponseEntity<CreateFreeGameResponse> createFreeGame(
            UUID accountId,
            CreateFreeGameRequest request
    );

    @ApiAuthValidationResponses
    ResponseEntity<CreateFreeGameAssignmentPreviewJobResponse> createFreeGameAssignmentPreview(
            UUID accountId,
            CreateFreeGameAssignmentPreviewRequest request
    );

    @PatchMapping("/{gameId}")
    ResponseEntity<UpdateFreeGameResponse> updateFreeGameInfo(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @RequestBody @Valid UpdateFreeGameRequest request
    );

    @PatchMapping("/{gameId}/rounds-and-matches")
    ResponseEntity<Void> updateFreeGameRoundMatch(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @RequestBody @Valid UpdateFreeGameRoundMatchRequest request
    );

    @PostMapping("/{gameId}/start")
    ResponseEntity<UpdateFreeGameResponse> startFreeGame(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId
    );

    @PostMapping("/{gameId}/rounds/{roundNumber}/matches/{courtNumber}/start")
    ResponseEntity<Void> startFreeGameMatch(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @PathVariable Integer roundNumber,
            @PathVariable Integer courtNumber
    );

    @PostMapping("/{gameId}/rounds/{roundNumber}/matches/{courtNumber}/complete")
    ResponseEntity<Void> completeFreeGameMatch(
            @AuthenticationPrincipal UUID accountId,
            @PathVariable UUID gameId,
            @PathVariable Integer roundNumber,
            @PathVariable Integer courtNumber,
            @RequestBody @Valid CompleteFreeGameMatchRequest request
    );
}
