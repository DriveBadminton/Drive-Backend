package com.gumraze.drive.drive_backend.auth.oauth.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserResponse(
        Long id,

        @JsonProperty("connected_at")
        String connectedAt,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {

    public record KakaoAccount(
            @JsonProperty("profile_needs_agreement")
            Boolean profileNeedsAgreement,

            Profile profile,

            @JsonProperty("email_needs_agreement")
            Boolean emailNeedsAgreement,

            @JsonProperty("is_email_valid")
            Boolean isEmailValid,

            String email,

            String gender,

            @JsonProperty("age_range")
            String ageRange,

            String birthday
    ) {}

    public record Profile(
            String nickname,

            @JsonProperty("thumbnail_image_url")
            String thumbnailImageUrl,

            @JsonProperty("profile_image_url")
            String profileImageUrl,

            @JsonProperty("is_default_image")
            Boolean isDefaultImage
    ) {}
}
