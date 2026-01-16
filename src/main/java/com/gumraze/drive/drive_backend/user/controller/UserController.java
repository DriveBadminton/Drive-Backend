package com.gumraze.drive.drive_backend.user.controller;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.common.api.ResultCode;
import com.gumraze.drive.drive_backend.common.exception.NotFoundException;
import com.gumraze.drive.drive_backend.user.dto.*;
import com.gumraze.drive.drive_backend.user.service.UserProfileService;
import com.gumraze.drive.drive_backend.user.service.UserSearchService;
import com.gumraze.drive.drive_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "사용자 프로필 API")
public class UserController {

    private final UserProfileService userProfileService;
    private final UserSearchService userSearchService;
    private final UserService userService;

    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Page<UserSearchResponse>>> searchUsers(
            @RequestParam String nickname,
            @RequestParam(required = false) String tag,
            Pageable pageable
    ) {
        Page<UserSearchResponse> body;

        if (tag == null || tag.isBlank()) {
            body = userSearchService.searchByNickname(nickname, pageable);
        } else {
            UserSearchResponse found = userSearchService.searchByNicknameAndTag(nickname, tag)
                    .orElseThrow(() -> new NotFoundException("유저가 없습니다."));
            body = new PageImpl<>(List.of(found), pageable, 1);
        }

        ResultCode code = ResultCode.OK;
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "유저 검색 성공", body));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserMeResponse>> me(
            @AuthenticationPrincipal Long userId
    ) {
        UserMeResponse body = userService.getUserMe(userId);

        ResultCode code = ResultCode.OK;
        // 프로필이 존재하면, 프로필 조회 성공
        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "내 프로필 조회 성공", body));
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
                                              "regionalGrade": "초심",
                                              "nationalGrade": "초심",
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
            Authentication authentication,
            @RequestBody UserProfileCreateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        userProfileService.createProfile(userId, request);

        ResultCode code = ResultCode.OK;
        UserProfileCreateResponseDto body = new UserProfileCreateResponseDto(userId);

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "프로필 등록에 성공했습니다.", body));
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
    public ResponseEntity<ApiResponse<UserProfilePrefillResponseDto>> prefillProfile(
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();

        UserProfilePrefillResponseDto body = userProfileService.getProfilePrefill(userId);

        ResultCode code = ResultCode.OK;

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "제3자 로그인 닉네임 조회 성공", body));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileResponseDto>> getMyProfile(
            @AuthenticationPrincipal Long userId
    ) {
        UserProfileResponseDto body = userProfileService.getMyProfile(userId);

        ResultCode code = ResultCode.OK;

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "내 프로필 조회 성공", body));
    }
}