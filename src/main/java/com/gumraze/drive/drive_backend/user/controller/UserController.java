package com.gumraze.drive.drive_backend.user.controller;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.common.api.ResultCode;
import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateRequest;
import com.gumraze.drive.drive_backend.user.dto.UserProfileCreateResponseDto;
import com.gumraze.drive.drive_backend.user.dto.UserProfilePrefillResponseDto;
import com.gumraze.drive.drive_backend.user.dto.UserProfileResponseDto;
import com.gumraze.drive.drive_backend.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "사용자 프로필 API")
public class UserController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    @Operation(
            summary = "내 프로필 상태 조회",
            description = "Access Token 기반으로 사용자 정보를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserProfileResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> me(
            Authentication authentication
    ) {
        // 인증 정보에서 userId 조회
        Long userId = (Long) authentication.getPrincipal();

        // userId로 프로필 조회
        UserProfileResponseDto profile = userProfileService.getMyProfile(userId);

        ResultCode code = ResultCode.OK;

        // 프로필이 존재하면, 프로필 조회 성공
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "내 프로필 조회 성공", profile));
    }

    @PostMapping("/me/profile")
    @Operation(
            summary = "프로필 생성",
            description = "닉네임/지역/등급을 입력해 프로필을 생성하고 계정을 ACTIVE로 전환합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = UserProfileCreateRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "nickname": "kim",
                                              "districtId": 1,
                                              "grade": "초심",
                                              "birth": "19980925"
                                              "gender": "MALE"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 생성 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 검증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserProfileCreateResponseDto>> createProfile(
            @RequestBody UserProfileCreateRequest request
    ) {
        // Spring Security가 저장해둔 인증된 사용자 정보를 가져오는 메서드
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) authentication.getPrincipal();
        userProfileService.createProfile(userId, request);

        UserProfileCreateResponseDto profile = new UserProfileCreateResponseDto(userId);

        ResultCode code = ResultCode.OK;

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "프로필 등록에 성공했습니다.", profile));
    }

    @GetMapping("/me/profile/prefill")
    @Operation(
            summary = "프로필 닉네임 프리필 조회",
            description = "제3자 로그인 닉네임이 있으면 suggestedNickname으로 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "success": true,
                                              "message": "제3자 로그인 닉네임 조회 성공",
                                              "data": {
                                                "suggestedNickname": "oauthNick",
                                                "hasOauthNickname": true
                                              },
                                              "errorCode": null,
                                              "timestamp": "2025-01-01T00:00:00"
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserProfilePrefillResponseDto>> prefillProfile() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) authentication.getPrincipal();
        UserProfilePrefillResponseDto body = userProfileService.getProfilePrefill(userId);

        ResultCode code = ResultCode.OK;

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "제3자 로그인 닉네임 조회 성공", body));
    }
}
