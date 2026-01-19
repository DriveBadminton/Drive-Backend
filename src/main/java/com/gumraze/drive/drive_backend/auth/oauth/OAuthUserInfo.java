package com.gumraze.drive.drive_backend.auth.oauth;

import com.gumraze.drive.drive_backend.user.constants.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

// provider에 관계없이 동일한 형태로 서비스 계층에 전달하기 위한 표준 OAuth 레코드

@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class OAuthUserInfo {
    String providerUserId;
    String email;
    String nickname;
    String profileImageUrl;
    String thumbnailImageUrl;
    Gender gender;
    String ageRange;
    String birthday;
    Boolean emailVerified;
    Boolean phoneNumberVerified;
}
