package com.gumraze.drive.drive_backend.api.courtManager;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.courtManager.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "CourtManager", description = "코트 매니저 API")
public interface CourtManagerApi {

    @Operation(
            summary = "자유게임 생성",
            description = "새로운 자유게임을 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "자유게임 생성 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<CreateFreeGameResponse>> createFreeGame(
            Long userId,
            CreateFreeGameRequest request
    );

    @Operation(
            summary = "자유게임 상세 조회",
            description = "자유게임의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "자유게임 상세 조회 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "자유게임을 찾을 수 없습니다.",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<FreeGameDetailResponse>> getFreeGameDetail(
            Long userId,
            Long gameId
    );

    @Operation(
            summary = "자유게임 기본 정보 수정",
            description = "자유게임의 기본 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "자유게임 기본 정보 수정 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<UpdateFreeGameResponse>> updateFreeGameInfo(
            Long userId,
            Long gameId,
            UpdateFreeGameRequest request
    );

    @Operation(
            summary = "자유게임 라운드 및 매치 조회",
            description = "자유게임 라운드 및 매치 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "자유게임 라운드 및 매치 정보 조회 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "자유게임을 찾을 수 없습니다.",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<FreeGameRoundMatchResponse>> getFreeGameRoundMatchResponse(
            Long userId,
            Long gameId
    );

    @Operation(
            summary = "자유게임 라운드 및 매치 수정",
            description = "자유게임 라운드 및 매치 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "자유게임 라운드 및 매치 정보 수정 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<UpdateFreeGameRoundMatchResponse>> updateFreeGameRoundMatch(
            Long userId,
            Long gameId,
            UpdateFreeGameRoundMatchRequest request
    );

    @Operation(
            summary = "자유게임 참가자 목록 조회",
            description = "자유게임 참가자 목록을 조회합니다. include=stats인 경우 매치 집계 정보를 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "자유게임 참가자 목록 조회 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "자유게임을 찾을 수 없습니다.",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<FreeGameParticipantsResponse>> getFreeGameParticipants(
            Long userId,
            Long gameId,
            String include
    );
}
