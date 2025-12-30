package com.gumraze.drive.drive_backend.auth.controller;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginResponseDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthRefreshTokenResponseDto;
import com.gumraze.drive.drive_backend.auth.service.AuthService;
import com.gumraze.drive.drive_backend.auth.service.OAuthLoginResult;
import com.gumraze.drive.drive_backend.auth.token.JwtProperties;
import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import com.gumraze.drive.drive_backend.common.api.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "OAuth 로그인/토큰 관련 API")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties properties;

    // OAuth 로그인 API
    @PostMapping("/login")
    @Operation(
            summary = "OAuth 로그인",
            description = "외부 OAuth 공급자로 로그인 후 서비스용 Access/Refresh 토큰을 발급합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OAuthLoginRequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "provider": "DUMMY",
                                              "authorizationCode": "auth-code",
                                              "redirectUri": "https://test.com"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = OAuthLoginResponseDto.class))
            )
    })
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

    @PostMapping("/refresh")
    @Operation(
            summary = "Access 토큰 리프레시",
            description = "Refresh Token으로 새로운 Access/Refresh 토큰을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리프레시 성공",
                    content = @Content(schema = @Schema(implementation = OAuthRefreshTokenResponseDto.class))
            )
    })
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

    @PostMapping("/logout")
    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
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
