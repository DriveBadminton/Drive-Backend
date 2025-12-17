package com.gumraze.drive.drive_backend.auth.oauth.google.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        int expiresIn,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("scope")
        String scope,

        @JsonProperty("id_token")
        String idToken,

        @JsonProperty("refresh_token")
        String refreshToken
) {
}
