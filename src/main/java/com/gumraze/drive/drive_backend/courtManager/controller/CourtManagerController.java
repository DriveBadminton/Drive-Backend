package com.gumraze.drive.drive_backend.courtManager.controller;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.common.api.ResultCode;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameRequest;
import com.gumraze.drive.drive_backend.courtManager.dto.CreateFreeGameResponse;
import com.gumraze.drive.drive_backend.courtManager.dto.FreeGameDetailResponse;
import com.gumraze.drive.drive_backend.courtManager.service.FreeGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CourtManagerController {

    private final FreeGameService freeGameService;

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
}
