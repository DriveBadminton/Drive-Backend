package com.gumraze.drive.drive_backend.user.controller;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.user.dto.AccountStatusResponseDto;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import com.gumraze.drive.drive_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/account")
@Tag(name = "Account", description = "계정 상태 관련 API")
public class AccountController {

    private final UserService userService;
    private final UserProfileRepository userProfileRepository;

    @GetMapping("/status")
    @Operation(
            summary = "계정 상태 조회",
            description = "로그인한 사용자의 계정 상태와 프로필 보유 여부를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = AccountStatusResponseDto.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AccountStatusResponseDto>> accountStatus() {
        // 인증된 사용자 계정 상태와 프로필 등록 여부를 조회
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        Long userId = (Long) authentication.getPrincipal();

        return userService.findById(userId)
                .map(user -> {
                    boolean hasProfile = userProfileRepository.existsByUserId(userId);

                    AccountStatusResponseDto response = new AccountStatusResponseDto(
                            userId,
                            user.getStatus(),
                            hasProfile
                    );

                    return ResponseEntity.ok(ApiResponse.success(
                            "",
                            response
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
