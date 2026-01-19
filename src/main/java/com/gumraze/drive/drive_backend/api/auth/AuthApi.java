package com.gumraze.drive.drive_backend.api.auth;

import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginRequestDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthLoginResponseDto;
import com.gumraze.drive.drive_backend.auth.dto.OAuthRefreshTokenResponseDto;
import com.gumraze.drive.drive_backend.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "OAuth 로그인/토큰 관련 API")
public interface AuthApi {

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
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류가 발생했습니다.",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<OAuthLoginResponseDto>> login(
            OAuthLoginRequestDto request
    );

    @Operation(
            summary = "Access 토큰 리프레시",
            description = "Refresh Token으로 새로운 Access/Refresh 토큰을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "리프레시 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "요청 검증 실패",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류가 발생했습니다.",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<OAuthRefreshTokenResponseDto>> refresh(
            @Parameter(
                    name = "refresh_token",
                    description = "Refresh Token cookie",
                    in = ParameterIn.COOKIE,
                    required = false
            )
            String refreshToken
    );

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류가 발생했습니다.",
                    content = @Content
            )
    })
    ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(
                    name = "refresh_token",
                    description = "Refresh Token cookie",
                    in = ParameterIn.COOKIE,
                    required = false
            )
            String refreshToken
    );
}
