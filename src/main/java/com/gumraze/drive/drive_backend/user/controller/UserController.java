package com.gumraze.drive.drive_backend.user.controller;

import com.gumraze.drive.drive_backend.api.user.UserApi;
import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.common.api.ResultCode;
import com.gumraze.drive.drive_backend.common.exception.NotFoundException;
import com.gumraze.drive.drive_backend.user.dto.*;
import com.gumraze.drive.drive_backend.user.entity.UserProfileUpdateRequest;
import com.gumraze.drive.drive_backend.user.service.UserProfileService;
import com.gumraze.drive.drive_backend.user.service.UserSearchService;
import com.gumraze.drive.drive_backend.user.service.UserService;
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
public class UserController implements UserApi {

    private final UserProfileService userProfileService;
    private final UserSearchService userSearchService;
    private final UserService userService;

    @GetMapping
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

    @Override
    @PostMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileCreateResponseDto>> createProfile(
            Authentication authentication,
            @RequestBody UserProfileCreateRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        userProfileService.createProfile(userId, request);

        ResultCode code = ResultCode.CREATED;
        UserProfileCreateResponseDto body = new UserProfileCreateResponseDto(userId);

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "프로필 등록에 성공했습니다.", body));
    }

    @GetMapping("/me/profile/prefill")
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

    @PatchMapping("/me/profile")
    public ResponseEntity<ApiResponse<Void>> updateMyProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserProfileUpdateRequest request
    ) {
        userProfileService.updateMyProfile(userId, request);
        ResultCode code = ResultCode.OK;

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "내 프로필 수정 성공", null));
    }

    @PatchMapping("/me/profile/identity")
    public ResponseEntity<ApiResponse<Void>> updateIdentity(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserProfileIdentityUpdateRequest request
    ) {
        userProfileService.updateNicknameAndTags(userId, request);
        ResultCode code = ResultCode.OK;

        return ResponseEntity
                .status(code.httpStatus())
                .body(ApiResponse.success(code, "닉네임/태그 변경 성공", null));
    }
}