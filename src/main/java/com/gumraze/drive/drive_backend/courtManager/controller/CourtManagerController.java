package com.gumraze.drive.drive_backend.courtManager.controller;

import com.gumraze.drive.drive_backend.api.courtManager.CourtManagerApi;
import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.common.api.ResultCode;
import com.gumraze.drive.drive_backend.courtManager.dto.*;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CourtManagerController implements CourtManagerApi {

    private final FreeGameService freeGameService;

    @Override
    @PostMapping("/free-games")
    public ResponseEntity<ApiResponse<CreateFreeGameResponse>> createFreeGame(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid CreateFreeGameRequest request
    ) {

        // 서비스 호출
        CreateFreeGameResponse response = freeGameService.createFreeGame(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(ResultCode.CREATED, "자유게임 생성 성공", response));
    }

    @Override
    @GetMapping("/free-games/{gameId}")
    public ResponseEntity<ApiResponse<FreeGameDetailResponse>> getFreeGameDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId
    ) {
        FreeGameDetailResponse response = freeGameService.getFreeGameDetail(userId, gameId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ResultCode.OK, "자유게임 상세 조회 성공", response));
    }

    @Override
    @PatchMapping("/free-games/{gameId}")
    public ResponseEntity<ApiResponse<UpdateFreeGameResponse>> updateFreeGameInfo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @RequestBody @Valid UpdateFreeGameRequest request
    ) {
        UpdateFreeGameResponse response = freeGameService.updateFreeGameInfo(userId, gameId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ResultCode.OK, "자유게임 기본 정보 수정 성공", response));
    }

    @Override
    @GetMapping("/free-games/{gameId}/rounds-and-matches")
    public ResponseEntity<ApiResponse<FreeGameRoundMatchResponse>> getFreeGameRoundMatchResponse(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId
    ) {
        FreeGameRoundMatchResponse response = freeGameService.getFreeGameRoundMatchResponse(userId, gameId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ResultCode.OK, "자유게임 라운드 및 매치 정보 조회 성공", response));
    }

    @Override
    @PatchMapping("/free-games/{gameId}/rounds-and-matches")
    public ResponseEntity<ApiResponse<UpdateFreeGameRoundMatchResponse>> updateFreeGameRoundMatch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @RequestBody @Valid UpdateFreeGameRoundMatchRequest request
    ) {
        UpdateFreeGameRoundMatchResponse response =
                freeGameService.updateFreeGameRoundMatch(userId, gameId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ResultCode.OK, "자유게임 라운드 및 매치 정보 수정 성공", response));
    }

    @Override
    @GetMapping("/free-games/{gameId}/participants")
    public ResponseEntity<ApiResponse<FreeGameParticipantsResponse>> getFreeGameParticipants(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long gameId,
            @RequestParam(name = "include", required = false) String include
    ) {
        boolean includeStats = "stats".equalsIgnoreCase(include);
        FreeGameParticipantsResponse response =
                freeGameService.getFreeGameParticipants(userId, gameId, includeStats);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ResultCode.OK, "자유게임 참가자 목록 조회 성공", response));
    }
}
