package com.gumraze.drive.drive_backend.auth.dto;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthLoginRequestDto {
    @Schema(description = "OAuth 공급자", example = "KAKAO")
    @NotNull(message = "OAuth Provider는 필수입니다.")
    private AuthProvider provider;
    @Schema(description = "인가 코드", example = "authorization-code-from-provider")
    @NotBlank(message = "Authorization Code는 필수입니다.")
    private String authorizationCode;
    @Schema(description = "Redirect URI", example = "https://example.com/oauth/callback")
    @NotBlank(message = "Redirect Uri는 필수입니다.")
    private String redirectUri;
}
