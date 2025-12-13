package com.gumraze.drive.drive_backend.user;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gumraze.drive.drive_backend.user.dto.UserProfileResponseDto;
import com.gumraze.drive.drive_backend.user.service.OauthUserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final OauthUserService oauthUserService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        Long userId = Optional.ofNullable(principal)
            .map(p -> p.<Object>getAttribute("userId"))
            .map(UserController::toLong)
            .orElse(null);

        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        return oauthUserService.findById(userId)
            .map(UserProfileResponseDto::from)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static Long toLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        return value != null ? Long.valueOf(value.toString()) : null;
    }
}
