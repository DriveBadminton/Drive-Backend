package com.gumraze.drive.drive_backend.auth.controller;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginResponseDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthRefreshTokenResponseDto;
import com.gumraze.drive.drive_backend.auth.service.AuthService;
import com.gumraze.drive.drive_backend.auth.service.OAuthLoginResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // OAuth 로그인 API
    @PostMapping("/login")
    public ResponseEntity<OAuthLoginResponseDto> login(
            @RequestBody OAuthLoginRequestDto request
    ) {
        OAuthLoginResult result = authService.login(request);

        // Refresh Token은 cookie로
        ResponseCookie refreshTokenCookie =
                ResponseCookie.from("refresh_token", result.refreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/auth")
                        .maxAge(Duration.ofDays(5))
                        .sameSite("Strict")
                        .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new OAuthLoginResponseDto(
                        result.userId(),
                        result.accessToken()
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<OAuthRefreshTokenResponseDto> refresh (
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh Token이 없습니다.");
        }

        OAuthLoginResult result = authService.refresh(refreshToken);

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from("refresh_token", result.refreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/auth")
                        .maxAge(Duration.ofDays(5))
                        .sameSite("Strict")
                        .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new OAuthRefreshTokenResponseDto(result.userId(), result.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie expiredCookie =
                ResponseCookie.from("refresh_token", "")
                        .httpOnly(true)
                        .secure(true)
                        .path("/auth")
                        .maxAge(0)
                        .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }
}
