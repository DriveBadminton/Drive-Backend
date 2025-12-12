package com.gumraze.drive.drive_backend.courtmanager.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gumraze.drive.drive_backend.courtmanager.dto.CourtTournamentResponse;
import com.gumraze.drive.drive_backend.courtmanager.dto.CreateCourtTournamentRequest;
import com.gumraze.drive.drive_backend.courtmanager.service.CourtTournamentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/court-manager/tournaments")
@Tag(name = "Court Manager", description = "Manage court tournaments and participants")
public class CourtTournamentController {

    private final CourtTournamentService courtTournamentService;

    public CourtTournamentController(CourtTournamentService courtTournamentService) {
        this.courtTournamentService = courtTournamentService;
    }

    @PostMapping
    @Operation(
        summary = "Create court tournament",
        description = "등록된 사용자가 공개/비공개 여부와 참가자 명단을 입력해 새로운 코트 매니저 대회를 생성합니다."
    )
    @ApiResponse(responseCode = "201", description = "Tournament created")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Authentication required")
    public ResponseEntity<CourtTournamentResponse> createTournament(
        @Valid @RequestBody CreateCourtTournamentRequest request,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        Long organizerId = extractUserId(principal);
        CourtTournamentResponse response = courtTournamentService.createTournament(request, organizerId);
        return ResponseEntity.created(URI.create("/api/court-manager/tournaments/" + response.id())).body(response);
    }

    private Long extractUserId(OAuth2User principal) {
        if (principal == null) {
            throw new IllegalArgumentException("Authentication required to create tournament");
        }
        Object attribute = principal.getAttribute("userId");
        if (attribute instanceof Long longValue) {
            return longValue;
        }
        if (attribute instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (attribute != null) {
            try {
                return Long.parseLong(attribute.toString());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Invalid userId attribute format", ex);
            }
        }
        throw new IllegalArgumentException("Authenticated user does not contain userId attribute");
    }
}
