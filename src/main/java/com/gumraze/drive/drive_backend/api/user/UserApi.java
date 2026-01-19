package com.gumraze.drive.drive_backend.api.user;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.user.dto.*;
import com.gumraze.drive.drive_backend.user.entity.UserProfileUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@Tag(name = "Users", description = "사용자 API")
public interface UserApi {

    @Operation(
            summary = "사용자 검색",
            description = "닉네임으로 사용자를 검색하고, tag가 있으면 태그 조건을 함께 적용합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "유저 검색 성공",
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "유저가 없습니다.",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<Page<UserSearchResponse>>> searchUsers(
            String nickname,
            String tag,
            Pageable pageable
    );

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 프로필 조회 성공",
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "접근 권한 없음",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<UserMeResponse>> me(
            Long userId
    );

    @Operation(
            summary = "프로필 생성",
            description = "닉네임/지역/등급을 입력해 프로필을생성하고 계정을 ACTIVE로 전환합니다;."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "프로필 생성 성공",
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
    ResponseEntity<ApiResponse<UserProfileCreateResponseDto>>
    createProfile(
            Authentication authentication,
            UserProfileCreateRequest request
    );

    @Operation(
            summary = "프로필 닉네임 프리필 조회",
            description = "제3자 로그인 닉네임이 있으면 suggestedNickname으로 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<UserProfilePrefillResponseDto>> prefillProfile(
            Authentication authentication
    );

    @Operation(
            summary = "내 프로필 상세 조회",
            description = "현재 로그인한 사용자의 프로필 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 프로필 조회 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자의 프로필을 찾을 수 없습니다.",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<UserProfileResponseDto>> getMyProfile(
            Long userId
    );

    @Operation(
            summary = "내 프로필 수정",
            description = "내 프로필 정보를 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "내 프로필 수정 성공",
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자의 프로필을 찾을 수 없습니다.",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<Void>> updateMyProfile(
            Long userId,
            UserProfileUpdateRequest request
    );

    @Operation(
            summary = "닉네임/태그 변경",
            description = "닉네임과 태그를 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "닉네임/태그 변경 성공",
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자의 프로필을 찾을 수 없습니다.",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 닉네임과 태그입니다.",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = "요청을 처리할 수 없습니다.",
                    content = @Content
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    ResponseEntity<ApiResponse<Void>> updateIdentity(
            Long userId,
            UserProfileIdentityUpdateRequest request
    );
}
