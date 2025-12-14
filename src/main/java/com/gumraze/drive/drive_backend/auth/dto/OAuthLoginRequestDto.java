package com.gumraze.drive.drive_backend.auth.dto;

import com.gumraze.drive.drive_backend.auth.constants.AuthProvider;
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
    @NotNull(message = "OAuth Provider는 필수입니다.")
    private AuthProvider provider;
    @NotBlank(message = "Authorization Code는 필수입니다.")
    private String authorizationCode;
    @NotBlank(message = "Redirect Uri는 필수입니다.")
    private String redirectUri;
}
