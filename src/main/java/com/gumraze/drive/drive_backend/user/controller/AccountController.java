package com.gumraze.drive.drive_backend.user.controller;

import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.user.dto.AccountStatusResponseDto;
import com.gumraze.drive.drive_backend.user.repository.UserProfileRepository;
import com.gumraze.drive.drive_backend.user.service.UserService;
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
public class AccountController {

    private final UserService userService;
    private final UserProfileRepository userProfileRepository;

    @GetMapping("/status")
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
