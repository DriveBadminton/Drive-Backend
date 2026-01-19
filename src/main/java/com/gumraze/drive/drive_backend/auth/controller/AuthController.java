package com.gumraze.drive.drive_backend.auth.controller;

import com.gumraze.drive.drive_backend.api.auth.AuthApi;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginResponseDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthRefreshTokenResponseDto;
import com.gumraze.drive.drive_backend.auth.service.AuthService;
import com.gumraze.drive.drive_backend.auth.service.OAuthLoginResult;
import com.gumraze.drive.drive_backend.auth.token.JwtProperties;
import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.common.api.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final JwtProperties properties;

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<OAuthLoginResponseDto>> login(
            @RequestBody OAuthLoginRequestDto request
    ) {
        OAuthLoginResult result = authService.login(request);

        // Refresh Token은 cookie로
        ResponseCookie refreshTokenCookie =
                ResponseCookie.from("refresh_token", result.refreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/auth")
                        .maxAge(Duration.ofHours(properties.refreshToken().expirationHours()))
                        .sameSite("Strict")
                        .build();

        OAuthLoginResponseDto response = new OAuthLoginResponseDto(result.userId(), result.accessToken());

        ResultCode code = ResultCode.OAUTH_LOGIN_SUCCESS;

        return ResponseEntity
                .status(code.httpStatus())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(code, response));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<OAuthRefreshTokenResponseDto>> refresh (
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh Token이 없습니다.");
        }

        OAuthLoginResult result = authService.refresh(refreshToken);

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from("refresh_token", result.refreshToken())
                        .httpOnly(true)
                        .secure(true)
                        .path("/auth")
                        .maxAge(Duration.ofHours(properties.refreshToken().expirationHours()))
                        .sameSite("Strict")
                        .build();

        OAuthRefreshTokenResponseDto response = new OAuthRefreshTokenResponseDto(result.userId(), result.accessToken());

        ResultCode code = ResultCode.OAUTH_REFRESH_TOKEN_SUCCESS;

        return ResponseEntity
                .status(code.httpStatus())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success(code, response));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
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

        ResultCode code = ResultCode.LOGOUT_SUCCESS;

        return ResponseEntity
                .status(code.httpStatus())
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(ApiResponse.success(code));
    }
}